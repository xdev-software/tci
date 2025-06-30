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
	public OIDCTCIFactory(
		final BiFunction<OIDCServerContainer, String, OIDCTCI> infraBuilder,
		final Supplier<OIDCServerContainer> containerBuilder)
	{
		super(infraBuilder, containerBuilder, "oidc", "container.oidc", "OIDC");
	}
	
	public OIDCTCIFactory(
		final BiFunction<OIDCServerContainer, String, OIDCTCI> infraBuilder,
		final Supplier<OIDCServerContainer> containerBuilder,
		final PreStartConfig config,
		final Timeouts timeouts)
	{
		super(infraBuilder, containerBuilder, "oidc", "container.oidc", "OIDC", config, timeouts);
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
			"oidc",
			"container.oidc",
			"OIDC");
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
