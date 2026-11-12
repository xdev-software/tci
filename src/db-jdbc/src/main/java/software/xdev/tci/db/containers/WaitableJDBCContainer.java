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
package software.xdev.tci.db.containers;

import java.time.Duration;

import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import software.xdev.tci.containers.HasWaitUntilStartedContainer;
import software.xdev.tci.envperf.EnvironmentPerformance;
import software.xdev.tci.startup.wait.FastAbortOnContainerDeathWaitStrategy;
import software.xdev.tci.startup.wait.strategy.HostPortWaitAbortableStrategy;


public interface WaitableJDBCContainer extends WaitStrategyTarget, HasWaitUntilStartedContainer
{
	@SuppressWarnings("checkstyle:MagicNumber")
	default WaitStrategy completeJDBCWaitStrategy()
	{
		return FastAbortOnContainerDeathWaitStrategy.waitAll(s -> s
			.withStartupTimeout(Duration.ofSeconds(40L + EnvironmentPerformance.cpuSlownessFactor() * 20L))
			.withStrategy(new HostPortWaitAbortableStrategy())
			.withStrategy(new JDBCAbortableWaitStrategy())
		);
	}
	
	WaitStrategy getWaitStrategy();
	
	@Override
	default void waitUntilContainerStarted()
	{
		final WaitStrategy waitStrategy = this.getWaitStrategy();
		if(waitStrategy != null)
		{
			waitStrategy.waitUntilReady(this);
		}
	}
}
