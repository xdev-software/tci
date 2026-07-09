package software.xdev.tci.imagebuild;

import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import software.xdev.tci.imagebuild.config.CreateImageHandlerConfig;
import software.xdev.testcontainers.imagebuilder.AbstractImageFromDockerfile;
import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;
import software.xdev.testcontainers.imagebuilder.buildxnative.NativeAdvancedImageFromDockerfile;


public class DefaultCreateImageHandler implements CreateImageHandler
{
	protected static final Pattern DOCKER_IMAGE_SANITIZATION_PATTERN =
		Pattern.compile("[^A-Za-z0-9-_]");
	
	protected final CreateImageHandlerConfig config;
	
	public DefaultCreateImageHandler()
	{
		this(CreateImageHandlerConfig.instance());
	}
	
	public DefaultCreateImageHandler(final CreateImageHandlerConfig config)
	{
		this.config = config;
	}
	
	@Override
	public NativeAdvancedImageFromDockerfile nativeImage(final String dockerImage)
	{
		final String sanitizeDockerImageName = this.sanitizeDockerImageName(dockerImage);
		final NativeAdvancedImageFromDockerfile builder =
			new NativeAdvancedImageFromDockerfile(dockerImage, this.config.deleteOnExit());
		
		this.configureAbstract(builder, sanitizeDockerImageName);
		
		this.config.cacheFrom()
			.map(s -> s.replace("$image", sanitizeDockerImageName))
			.map(s -> s.replace("§image", sanitizeDockerImageName))
			.ifPresent(builder::withCacheFrom);
		this.config.cacheTo()
			.map(s -> s.replace("$image", sanitizeDockerImageName))
			.map(s -> s.replace("§image", sanitizeDockerImageName))
			.ifPresent(builder::withCacheTo);
		
		return builder;
	}
	
	@Override
	public AdvancedImageFromDockerFile image(final String dockerImage)
	{
		final String sanitizeDockerImageName = this.sanitizeDockerImageName(dockerImage);
		final AdvancedImageFromDockerFile builder =
			new AdvancedImageFromDockerFile(dockerImage, this.config.deleteOnExit());
		
		return this.configureAbstract(builder, sanitizeDockerImageName);
	}
	
	protected <I extends AbstractImageFromDockerfile<I>> I configureAbstract(
		final I builder, final String sanitizeDockerImageName)
	{
		return builder.withLoggerForBuild(
			LoggerFactory.getLogger(this.config.loggerForBuildPrefix() + sanitizeDockerImageName));
	}
	
	protected String sanitizeDockerImageName(final String dockerImage)
	{
		return DOCKER_IMAGE_SANITIZATION_PATTERN.matcher(dockerImage).replaceAll("_");
	}
}
