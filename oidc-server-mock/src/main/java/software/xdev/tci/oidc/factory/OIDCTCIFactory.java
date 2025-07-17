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
package software.xdev.tci.oidc.factory;

import java.time.Duration;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.hc.core5.http.HttpStatus;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import software.xdev.tci.envperf.EnvironmentPerformance;
import software.xdev.tci.factory.prestart.PreStartableTCIFactory;
import software.xdev.tci.factory.prestart.config.PreStartConfig;
import software.xdev.tci.misc.ContainerMemory;
import software.xdev.tci.oidc.OIDCTCI;
import software.xdev.tci.oidc.containers.OIDCServerContainer;


public class OIDCTCIFactory extends PreStartableTCIFactory<OIDCServerContainer, OIDCTCI>
{
	private static final String DEFAULT_CONTAINER_LOGGER_NAME = "container.oidc";
	private static final String DEFAULT_CONTAINER_BASE_NAME = "oidc";
	private static final String DEFAULT_NAME = "OIDC";
	
	public OIDCTCIFactory(
		final BiFunction<OIDCServerContainer, String, OIDCTCI> infraBuilder,
		final Supplier<OIDCServerContainer> containerBuilder)
	{
		super(infraBuilder, containerBuilder, DEFAULT_CONTAINER_BASE_NAME, DEFAULT_CONTAINER_LOGGER_NAME,
			DEFAULT_NAME);
	}
	
	public OIDCTCIFactory(
		final BiFunction<OIDCServerContainer, String, OIDCTCI> infraBuilder,
		final Supplier<OIDCServerContainer> containerBuilder,
		final PreStartConfig config,
		final Timeouts timeouts)
	{
		super(
			infraBuilder, containerBuilder,
			DEFAULT_CONTAINER_BASE_NAME, DEFAULT_CONTAINER_LOGGER_NAME, DEFAULT_NAME, config, timeouts);
	}
	
	public OIDCTCIFactory(
		final BiFunction<OIDCServerContainer, String, OIDCTCI> infraBuilder,
		final Supplier<OIDCServerContainer> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName,
		final String name)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName, name);
	}
	
	public OIDCTCIFactory(
		final BiFunction<OIDCServerContainer, String, OIDCTCI> infraBuilder,
		final Supplier<OIDCServerContainer> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName,
		final String name,
		final PreStartConfig config,
		final Timeouts timeouts)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName, name, config, timeouts);
	}
	
	public OIDCTCIFactory()
	{
		super(
			OIDCTCI::new,
			OIDCTCIFactory::createDefaultContainer,
			DEFAULT_CONTAINER_BASE_NAME,
			DEFAULT_CONTAINER_LOGGER_NAME,
			DEFAULT_NAME);
	}
	
	@SuppressWarnings({"resource", "checkstyle:MagicNumber"})
	protected static OIDCServerContainer createDefaultContainer()
	{
		return new OIDCServerContainer()
			.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(ContainerMemory.M512M))
			.waitingFor(
				new WaitAllStrategy()
					.withStartupTimeout(Duration.ofSeconds(40L + 20L * EnvironmentPerformance.cpuSlownessFactor()))
					.withStrategy(new HostPortWaitStrategy())
					.withStrategy(
						new HttpWaitStrategy()
							.forPort(OIDCServerContainer.PORT)
							.forPath("/")
							.forStatusCode(HttpStatus.SC_OK)
							.withReadTimeout(Duration.ofSeconds(10))
					)
			)
			.withDefaultEnvConfig();
	}
}
