/*
 * Copyright © 2025 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.tci.imagebuild;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.tci.concurrent.TCIExecutorServiceHolder;
import software.xdev.tci.imagebuild.cache.async.TCICacheSaveRegistry;
import software.xdev.tci.imagebuild.config.BuildImageHandlerConfig;
import software.xdev.testcontainers.imagebuilder.AbstractImageFromDockerfile;
import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;
import software.xdev.testcontainers.imagebuilder.buildxnative.NativeAdvancedImageFromDockerfile;


public class DefaultBuildImageHandler implements BuildImageHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBuildImageHandler.class);
	
	protected static final Pattern DOCKER_IMAGE_SANITIZATION_PATTERN =
		Pattern.compile("[^A-Za-z0-9-_]");
	
	protected final BuildImageHandlerConfig config;
	
	public DefaultBuildImageHandler()
	{
		this(BuildImageHandlerConfig.instance());
	}
	
	public DefaultBuildImageHandler(final BuildImageHandlerConfig config)
	{
		this.config = config;
	}
	
	@Override
	public String nativeImage(
		final String dockerImage,
		final Duration timeout,
		final UnaryOperator<NativeAdvancedImageFromDockerfile> configure)
	{
		LOG.info("Starting build of (native) image {}", dockerImage);
		
		final String sanitizeDockerImageName = this.sanitizeDockerImageName(dockerImage);
		final NativeAdvancedImageFromDockerfile builder =
			new NativeAdvancedImageFromDockerfile(dockerImage, this.config.deleteOnExit());
		
		this.configureAbstract(builder, sanitizeDockerImageName);
		
		this.templateCacheConfigValue(this.config.cacheFrom(), sanitizeDockerImageName)
			.ifPresent(builder::withCacheFrom);
		
		if(this.config.saveCacheInBackground())
		{
			builder.withCreateTransferFilesCache(true);
		}
		else
		{
			this.configureCacheTo(sanitizeDockerImageName, builder);
		}
		
		final NativeAdvancedImageFromDockerfile customizedBuilder = configure.apply(builder);
		
		final String builtImageName = this.buildImage(customizedBuilder, timeout);
		
		if(this.config.saveCacheInBackground() && this.config.cacheTo().isPresent())
		{
			LOG.info("Rebuilding image {} in background to save cache", builtImageName);
			TCICacheSaveRegistry.instance().add(
				CompletableFuture.runAsync(
					() -> {
						try
						{
							this.buildImage(
								this.configureCacheTo(
									sanitizeDockerImageName,
									customizedBuilder.copyForExactRebuild(dockerImage + "-cache")
										// Don't load it into docker because it's not needed there
										.withLoad(false)),
								timeout);
						}
						catch(final Exception ex)
						{
							LOG.warn("Rebuilding {} to save cache failed", builtImageName, ex);
						}
						finally
						{
							try
							{
								builder.cleanCreatedTransferFilesCache();
							}
							catch(final Exception ex)
							{
								LOG.warn("Failed to clean created transfer file cache for {}", builtImageName, ex);
							}
						}
					},
					TCIExecutorServiceHolder.instance()));
		}
		
		return builtImageName;
	}
	
	protected NativeAdvancedImageFromDockerfile configureCacheTo(
		final String sanitizeDockerImageName,
		final NativeAdvancedImageFromDockerfile builder)
	{
		this.templateCacheConfigValue(this.config.cacheTo(), sanitizeDockerImageName)
			.ifPresent(builder::withCacheTo);
		return builder;
	}
	
	private Optional<String> templateCacheConfigValue(
		final Optional<String> configValue,
		final String sanitizeDockerImageName)
	{
		return configValue
			.map(s -> s.replace("$image", sanitizeDockerImageName))
			.map(s -> s.replace("§image", sanitizeDockerImageName));
	}
	
	@Override
	public String image(
		final String dockerImage,
		final Duration timeout,
		final UnaryOperator<AdvancedImageFromDockerFile> configure)
	{
		LOG.info("Starting build of image {}", dockerImage);
		
		final String sanitizeDockerImageName = this.sanitizeDockerImageName(dockerImage);
		final AdvancedImageFromDockerFile builder =
			new AdvancedImageFromDockerFile(dockerImage, this.config.deleteOnExit());
		
		this.configureAbstract(builder, sanitizeDockerImageName);
		
		return this.buildImage(configure.apply(builder), timeout);
	}
	
	protected String buildImage(
		final AbstractImageFromDockerfile<?> builder,
		final Duration timeout)
	{
		LOG.info("Building image {}...", builder.getDockerImageName());
		final String builtImageName = builder.build(timeout);
		LOG.info("Built image {}", builtImageName);
		
		return builtImageName;
	}
	
	protected <I extends AbstractImageFromDockerfile<I>> void configureAbstract(
		final I builder, final String sanitizeDockerImageName)
	{
		builder.withLoggerForBuild(
			LoggerFactory.getLogger(this.config.loggerForBuildPrefix() + sanitizeDockerImageName));
	}
	
	protected String sanitizeDockerImageName(final String dockerImage)
	{
		return DOCKER_IMAGE_SANITIZATION_PATTERN.matcher(dockerImage).replaceAll("_");
	}
}
