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
package software.xdev.tci.s3.rustfs.factory;

import static software.xdev.tci.envperf.EnvironmentPerformance.cpuSlownessFactor;

import java.time.Duration;
import java.util.List;

import software.xdev.tci.factory.prestart.PreStartableTCIFactory;
import software.xdev.tci.misc.ContainerMemory;
import software.xdev.tci.s3.rustfs.RustFSTCI;
import software.xdev.tci.s3.rustfs.containers.RustFSContainer;
import software.xdev.tci.startup.wait.FastAbortOnContainerDeathWaitStrategy;
import software.xdev.tci.startup.wait.strategy.HostPortWaitAbortableStrategy;
import software.xdev.tci.startup.wait.strategy.HttpWaitAbortableStrategy;


public class RustFSTCIFactory extends PreStartableTCIFactory<RustFSContainer, RustFSTCI>
{
	@SuppressWarnings("checkstyle:MagicNumber")
	public RustFSTCIFactory(final String... bucketsToCreate)
	{
		super(
			(c, na) -> new RustFSTCI(c, na, List.of(bucketsToCreate)),
			() -> new RustFSContainer()
				.withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withMemory(ContainerMemory.M512M))
				.waitingFor(FastAbortOnContainerDeathWaitStrategy.waitAll(s -> s
					.withStartupTimeout(Duration.ofSeconds(30 + 20L * cpuSlownessFactor()))
					.withStrategy(new HostPortWaitAbortableStrategy())
					.withStrategy(new HttpWaitAbortableStrategy()
						.forPath("/health/ready")
						.forPort(RustFSContainer.S3_PORT)
					))),
			"s3-rustfs",
			"container.s3.rustfs",
			"S3-RustFS");
	}
}
