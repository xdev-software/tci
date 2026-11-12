/*
 * Copyright © 2024 XDEV Software (https://xdev.software)
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
package software.xdev.tci.s3.rustfs.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.utility.DockerImageName;


@SuppressWarnings("java:S2068")
public class RustFSContainer extends GenericContainer<RustFSContainer>
{
	public static final DockerImageName DEFAULT_IMAGE = DockerImageName.parse("rustfs/rustfs:1.0.5");
	
	public static final int S3_PORT = 9000;
	
	public static final String DEFAULT_USERNAME = "rustfs";
	public static final String DEFAULT_PASSWORD = "rustfs";
	
	protected String username = DEFAULT_USERNAME;
	protected String password = DEFAULT_PASSWORD;
	
	public RustFSContainer(final DockerImageName dockerImageName)
	{
		super(dockerImageName);
	}
	
	public RustFSContainer(final RemoteDockerImage image)
	{
		super(image);
	}
	
	public RustFSContainer()
	{
		this(DEFAULT_IMAGE);
		
		this.withCommand("server", "/data");
		this.addExposedPort(S3_PORT);
	}
	
	public RustFSContainer withUsername(final String username)
	{
		this.username = username;
		return this;
	}
	
	public RustFSContainer withPassword(final String password)
	{
		this.password = password;
		return this;
	}
	
	@Override
	protected void configure()
	{
		if(this.username != null)
		{
			this.addEnv("RUSTFS_ACCESS_KEY", this.username);
		}
		if(this.password != null)
		{
			this.addEnv("RUSTFS_SECRET_KEY", this.password);
		}
	}
	
	public String gets3Url()
	{
		// noinspection HttpUrlsUsage
		return "http://" + this.getHost() + ":" + this.getMappedPort(S3_PORT);
	}
	
	public String getUsername()
	{
		return this.username;
	}
	
	public String getPassword()
	{
		return this.password;
	}
}
