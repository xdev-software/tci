package software.xdev.tci.mockserver;

import software.xdev.mockserver.client.MockServerClient;
import software.xdev.tci.TCI;
import software.xdev.testcontainers.mockserver.containers.MockServerContainer;


public abstract class MockServerTCI extends TCI<MockServerContainer>
{
	protected MockServerClient client;
	
	protected MockServerTCI(final MockServerContainer container, final String networkAlias)
	{
		super(container, networkAlias);
	}
	
	@Override
	public void start(final String containerName)
	{
		super.start(containerName);
		this.client = new MockServerClient(this.getContainer().getHost(), this.getContainer().getServerPort());
	}
	
	@Override
	public void stop()
	{
		this.client = null;
		super.stop();
	}
	
	public MockServerClient getClient()
	{
		return this.client;
	}
}
