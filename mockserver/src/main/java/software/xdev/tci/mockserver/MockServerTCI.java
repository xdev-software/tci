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
