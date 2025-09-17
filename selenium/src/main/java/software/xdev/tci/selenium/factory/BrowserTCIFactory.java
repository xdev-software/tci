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
package software.xdev.tci.selenium.factory;

import static software.xdev.tci.envperf.EnvironmentPerformance.cpuSlownessFactor;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.bidi.log.LogLevel;
import org.openqa.selenium.remote.http.ClientConfig;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import software.xdev.tci.concurrent.TCIExecutorServiceHolder;
import software.xdev.tci.factory.prestart.PreStartableTCIFactory;
import software.xdev.tci.factory.prestart.config.PreStartConfig;
import software.xdev.tci.misc.ContainerMemory;
import software.xdev.tci.selenium.BrowserTCI;
import software.xdev.tci.selenium.containers.SeleniumBrowserWebDriverContainer;
import software.xdev.tci.selenium.factory.config.BrowserTCIFactoryConfig;
import software.xdev.tci.serviceloading.TCIServiceLoaderHolder;
import software.xdev.testcontainers.selenium.containers.recorder.SeleniumRecordingContainer;


public class BrowserTCIFactory extends PreStartableTCIFactory<SeleniumBrowserWebDriverContainer, BrowserTCI>
{
	protected final String browserName;
	
	public BrowserTCIFactory(final MutableCapabilities capabilities)
	{
		this(capabilities, TCIServiceLoaderHolder.instance().service(BrowserTCIFactoryConfig.class));
	}
	
	@SuppressWarnings({"resource", "checkstyle:MagicNumber"})
	public BrowserTCIFactory(final MutableCapabilities capabilities, final BrowserTCIFactoryConfig config)
	{
		super(
			(c, na) -> new BrowserTCI(c, na, capabilities)
				.withBidiEnabled(config.bidiEnabled())
				.withDeactivateCDPIfPossible(config.deactivateCdpIfPossible())
				.withClientConfig(ClientConfig.defaultConfig()
					.readTimeout(Duration.ofSeconds(60 + cpuSlownessFactor() * 10L)))
				.withWebDriverRetryCount(Math.max(Math.min(cpuSlownessFactor(), 5), 1))
				.withWebDriverRetrySec(25 + cpuSlownessFactor() * 5)
				.withBrowserConsoleLog(
					logBrowserConsoleConsumer(config.browserConsoleLogLevel()),
					config.browserConsoleLogLevel().logLevels()),
			() -> new SeleniumBrowserWebDriverContainer(capabilities)
				.withStartRecordingContainerManually(true)
				.withRecordingDirectory(config.dirForRecords())
				.withRecordingMode(config.recordingMode())
				// 2024-04 VNC is no longer required when recording
				.withDisableVNC(!config.vncEnabled())
				.withEnableNoVNC(config.vncEnabled())
				.withRecordingContainerSupplier(t -> new SeleniumRecordingContainer(t)
					.withFrameRate(10)
					.withLogConsumer(getLogConsumer("container.browserrecorder." + capabilities.getBrowserName()))
					.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(ContainerMemory.M512M)))
				// Without that a mount volume dialog shows up
				// https://github.com/testcontainers/testcontainers-java/issues/1670
				.withSharedMemorySize(ContainerMemory.M2G)
				.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(ContainerMemory.M1G))
				.withEnv("SE_SCREEN_WIDTH", "1600")
				.withEnv("SE_SCREEN_HEIGHT", "900")
				// By default after 5 mins the session is killed and you can't use the container anymore. Cool or?
				// https://github.com/SeleniumHQ/docker-selenium?tab=readme-ov-file#grid-url-and-session-timeout
				.withEnv("SE_NODE_SESSION_TIMEOUT", "3600")
				// Disable local tracing, as we don't need it
				// https://github.com/SeleniumHQ/docker-selenium/issues/2355
				.withEnv("SE_ENABLE_TRACING", "false")
				// Some (AWS) CPUs are completely overloaded with the default 15s timeout -> increase it
				.waitingFor(new WaitAllStrategy()
					.withStrategy(new LogMessageWaitStrategy()
						.withRegEx(".*(Started Selenium Standalone).*\n")
						.withStartupTimeout(Duration.ofSeconds(30 + 20L * cpuSlownessFactor())))
					.withStrategy(new HostPortWaitStrategy())
					.withStartupTimeout(Duration.ofSeconds(30 + 20L * cpuSlownessFactor()))),
			"selenium-" + capabilities.getBrowserName().toLowerCase(),
			"container.browserwebdriver." + capabilities.getBrowserName(),
			"Browser-" + capabilities.getBrowserName());
		this.browserName = capabilities.getBrowserName();
	}
	
	public BrowserTCIFactory(
		final BiFunction<SeleniumBrowserWebDriverContainer, String, BrowserTCI> infraBuilder,
		final Supplier<SeleniumBrowserWebDriverContainer> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName,
		final String name,
		final String browserName)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName, name);
		this.browserName = browserName;
	}
	
	@SuppressWarnings("java:S107")
	public BrowserTCIFactory(
		final BiFunction<SeleniumBrowserWebDriverContainer, String, BrowserTCI> infraBuilder,
		final Supplier<SeleniumBrowserWebDriverContainer> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName,
		final String name,
		final PreStartConfig config,
		final Timeouts timeouts,
		final String browserName)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName, name, config, timeouts);
		this.browserName = browserName;
	}
	
	protected static Consumer<String> logBrowserConsoleConsumer(final BrowserConsoleLogLevel level)
	{
		if(!level.active())
		{
			return null;
		}
		final Logger logger = LoggerFactory.getLogger("container.browser.console");
		if(!logger.isInfoEnabled())
		{
			return null;
		}
		return logger::info;
	}
	
	@Override
	protected void postProcessNew(final BrowserTCI infra)
	{
		// Start recording container here otherwise there is a lot of blank video
		final CompletableFuture<Void> cfStartRecorder = CompletableFuture.runAsync(
			() -> infra.getContainer().startRecordingContainer(),
			TCIExecutorServiceHolder.instance());
		
		// Docker needs a few milliseconds (usually less than 100) to reconfigure its networks
		// In the meantime existing connections might fail if we go on immediately
		// So let's wait a moment here until everything is fine
		Unreliables.retryUntilSuccess(
			10,
			TimeUnit.SECONDS,
			() -> infra.getWebDriver().getCurrentUrl());
		
		cfStartRecorder.join();
	}
	
	@Override
	public String getFactoryName()
	{
		return super.getFactoryName() + "-" + this.browserName;
	}
	
	public enum BrowserConsoleLogLevel
	{
		OFF(Set.of()),
		ERROR(Set.of(LogLevel.ERROR)),
		WARN(Set.of(LogLevel.WARNING, LogLevel.ERROR)),
		INFO(Set.of(LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR)),
		DEBUG(Set.of(LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR)),
		ALL(null);
		
		// Selenium's LogLevel can't be compared (order is random) -> use Set
		private final Set<LogLevel> logLevels;
		
		BrowserConsoleLogLevel(final Set<LogLevel> logLevels)
		{
			this.logLevels = logLevels;
		}
		
		public Set<LogLevel> logLevels()
		{
			return this.logLevels;
		}
		
		public boolean active()
		{
			return this.logLevels() == null || !this.logLevels().isEmpty();
		}
	}
}
