package software.xdev.tci.mockserver.factory;

import java.util.function.BiFunction;

import software.xdev.tci.factory.prestart.PreStartableTCIFactory;
import software.xdev.tci.misc.ContainerMemory;
import software.xdev.tci.mockserver.MockServerTCI;
import software.xdev.testcontainers.mockserver.containers.MockServerContainer;


public abstract class PreStartableMockServerTCIFactory<I extends MockServerTCI>
	extends PreStartableTCIFactory<MockServerContainer, I>
{
	@SuppressWarnings("resource")
	protected PreStartableMockServerTCIFactory(
		final BiFunction<MockServerContainer, String, I> infraBuilder,
		final String additionalContainerBaseName,
		final String additionalLoggerName,
		final String prestartName)
	{
		super(
			infraBuilder,
			() -> new MockServerContainer()
				.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(ContainerMemory.M512M)),
			"mockserver-" + additionalContainerBaseName,
			"container.mockserver." + additionalLoggerName,
			prestartName);
	}
}
