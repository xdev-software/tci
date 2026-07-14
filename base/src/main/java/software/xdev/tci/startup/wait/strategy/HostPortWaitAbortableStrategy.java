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
package software.xdev.tci.startup.wait.strategy;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.wait.internal.ExternalPortListeningCheck;
import org.testcontainers.containers.wait.internal.InternalCommandPortListeningCheck;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import software.xdev.tci.startup.wait.AbortMonitor;


/**
 * Based on {@link org.testcontainers.containers.wait.strategy.HostPortWaitStrategy}
 */
public class HostPortWaitAbortableStrategy extends AbstractWaitAbortableStrategy<HostPortWaitAbortableStrategy>
{
	private static final Logger LOG = LoggerFactory.getLogger(HostPortWaitAbortableStrategy.class);
	
	protected int[] ports;
	
	@Override
	protected void waitUntilReady(final AbortMonitor abortMonitor)
	{
		final Set<Integer> externalLivenessCheckPorts;
		if(this.ports == null || this.ports.length == 0)
		{
			externalLivenessCheckPorts = this.getLivenessCheckPorts();
			if(externalLivenessCheckPorts.isEmpty())
			{
				if(LOG.isDebugEnabled())
				{
					LOG.debug(
						"Liveness check ports of {} is empty. Not waiting.",
						this.waitStrategyTarget.getContainerInfo().getName()
					);
				}
				return;
			}
		}
		else
		{
			externalLivenessCheckPorts =
				Arrays
					.stream(this.ports)
					.mapToObj(port -> this.waitStrategyTarget.getMappedPort(port))
					.collect(Collectors.toSet());
		}
		
		abortMonitor.throwIfRequired();
		
		final List<Integer> exposedPorts = this.waitStrategyTarget.getExposedPorts();
		
		final Set<Integer> internalPorts = this.getInternalPorts(externalLivenessCheckPorts, exposedPorts);
		
		final Callable<Boolean> internalCheck =
			new InternalCommandPortListeningCheck(this.waitStrategyTarget, internalPorts);
		
		final Callable<Boolean> externalCheck = new ExternalPortListeningCheck(
			this.waitStrategyTarget,
			externalLivenessCheckPorts
		);
		
		try
		{
			final List<Future<Boolean>> futures = this.getExecutor().invokeAll(
				Arrays.asList(
					// Blocking
					() -> {
						final long startMs = System.currentTimeMillis();
						final Boolean result = internalCheck.call();
						LOG.debug(
							"Internal port check {} for {} took {}ms",
							Boolean.TRUE.equals(result) ? "passed" : "failed",
							internalPorts,
							System.currentTimeMillis() - startMs
						);
						return result;
					},
					// Polling
					() -> {
						final long startMs = System.currentTimeMillis();
						Awaitility.await()
							.pollInSameThread()
							.pollInterval(Duration.ofMillis(100))
							.pollDelay(Duration.ZERO)
							.failFast(
								"container is no longer running",
								() -> !this.waitStrategyTarget.isRunning() || abortMonitor.shouldAbort())
							.ignoreExceptions()
							.forever()
							.until(externalCheck);
						
						LOG.debug(
							"External port check passed for {} mapped as {} took {}ms",
							internalPorts,
							externalLivenessCheckPorts,
							System.currentTimeMillis() - startMs
						);
						return true;
					}
				),
				this.startupTimeout.getSeconds(),
				TimeUnit.SECONDS
			);
			
			for(final Future<Boolean> future : futures)
			{
				future.get(0, TimeUnit.SECONDS);
			}
		}
		catch(final InterruptedException iex)
		{
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Got interrupted", iex);
		}
		catch(final CancellationException | ExecutionException | TimeoutException e)
		{
			throw new ContainerLaunchException(
				"Timed out waiting for container port to open ("
					+ this.waitStrategyTarget.getHost()
					+ " ports: "
					+ externalLivenessCheckPorts
					+ " should be listening)",
				e
			);
		}
	}
	
	protected Set<Integer> getInternalPorts(
		final Set<Integer> externalLivenessCheckPorts,
		final List<Integer> exposedPorts)
	{
		return exposedPorts
			.stream()
			.filter(it -> externalLivenessCheckPorts.contains(this.waitStrategyTarget.getMappedPort(it)))
			.collect(Collectors.toSet());
	}
	
	public HostPortWaitAbortableStrategy forPorts(final int... ports)
	{
		this.ports = ports;
		return this.self();
	}
}
