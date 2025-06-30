package software.xdev.tci.mockserver.factory;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.testcontainers.containers.Network;

import software.xdev.tci.factory.ondemand.OnDemandTCIFactory;
import software.xdev.tci.misc.ContainerMemory;
import software.xdev.tci.mockserver.MockServerTCI;
import software.xdev.testcontainers.mockserver.containers.MockServerContainer;


public abstract class MockServerTCIFactory<I extends MockServerTCI>
	extends OnDemandTCIFactory<MockServerContainer, I>
{
	protected MockServerTCIFactory(
		final BiFunction<MockServerContainer, String, I> infraBuilder,
		final Supplier<MockServerContainer> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName);
	}
	
	@SuppressWarnings("resource")
	protected MockServerTCIFactory(
		final BiFunction<MockServerContainer, String, I> infraBuilder,
		final String additionalContainerBaseName,
		final String additionalLoggerName)
	{
		super(
			infraBuilder,
			() -> new MockServerContainer()
				.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(ContainerMemory.M512M)),
			"mockserver-" + additionalContainerBaseName,
			"container.mockserver." + additionalLoggerName);
	}
	
	public I getNew(
		final Network network,
		final String name,
		final String... networkAliases)
	{
		return this.getNew(
			network, c -> c.withNetworkAliases(networkAliases)
				.setLogConsumers(List.of(getLogConsumer(this.containerLoggerName + "." + name))));
	}
	
	@Override
	public I getNew(final Network network)
	{
		throw new UnsupportedOperationException();
	}
}
