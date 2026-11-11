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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.ExecInContainerPattern;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import software.xdev.tci.concurrent.TCIExecutorServiceHolder;
import software.xdev.tci.startup.wait.AbortMonitor;
import software.xdev.tci.startup.wait.holder.WaitStrategyCurrentState;


/**
 * Based on {@link org.testcontainers.containers.wait.strategy.HostPortWaitStrategy}
 */
public class HostPortWaitAbortableStrategy extends AbstractWaitAbortableStrategy<HostPortWaitAbortableStrategy>
{
	private static final Logger LOG = LoggerFactory.getLogger(HostPortWaitAbortableStrategy.class);
	
	protected int[] ports;
	
	@Override
	public void waitUntilReady(
		final WaitStrategyTarget target,
		final AbortMonitor abortMonitor,
		final ExecutorService executor,
		final WaitStrategyCurrentState sc)
	{
		final Set<Integer> externalLivenessCheckPorts = this.determineExternalLivenessCheckPorts(target);
		if(externalLivenessCheckPorts == null || externalLivenessCheckPorts.isEmpty())
		{
			if(LOG.isDebugEnabled())
			{
				LOG.debug(
					"Liveness check ports of {} is empty. Not waiting.",
					target.getContainerInfo().getName()
				);
			}
			return;
		}
		
		abortMonitor.throwIfRequired();
		
		final Set<Integer> internalPorts = this.getInternalPorts(
			target,
			externalLivenessCheckPorts);
		
		final Callable<Boolean> internalCheck = this.createInternalCheck(target, internalPorts, sc);
		final Callable<Boolean> externalCheck = this.createExternalCheck(target, externalLivenessCheckPorts, sc);
		
		try
		{
			final List<Future<Boolean>> futures = executor.invokeAll(
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
								() -> !target.isRunning() || abortMonitor.shouldAbort())
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
				this.getStartupTimeout().getSeconds(),
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
			final ContainerLaunchException cle = new ContainerLaunchException(
				"Timed out waiting for container port to open ("
					+ target.getHost()
					+ " ports: "
					+ externalLivenessCheckPorts
					+ " should be listening)",
				e
			);
			sc.withException(cle);
			throw cle;
		}
	}
	
	private ExternalPortListeningCheck createExternalCheck(
		final WaitStrategyTarget target,
		final Set<Integer> externalLivenessCheckPorts,
		final WaitStrategyCurrentState ec)
	{
		return new ExternalPortListeningCheck(
			target,
			externalLivenessCheckPorts,
			ec.newChild("External port check"));
	}
	
	private InternalCommandPortListeningCheck createInternalCheck(
		final WaitStrategyTarget target,
		final Set<Integer> internalPorts,
		final WaitStrategyCurrentState ec)
	{
		return new InternalCommandPortListeningCheck(
			target,
			internalPorts,
			ec.newChild("Internal port check"));
	}
	
	/**
	 * @apiNote Can return null
	 */
	protected Set<Integer> determineExternalLivenessCheckPorts(final WaitStrategyTarget target)
	{
		if(this.ports == null || this.ports.length == 0)
		{
			return target.getLivenessCheckPortNumbers();
		}
		
		return Arrays
			.stream(this.ports)
			.mapToObj(target::getMappedPort)
			.collect(Collectors.toSet());
	}
	
	protected Set<Integer> getInternalPorts(
		final WaitStrategyTarget target,
		final Set<Integer> externalLivenessCheckPorts)
	{
		return target.getExposedPorts()
			.stream()
			.filter(port -> externalLivenessCheckPorts.contains(target.getMappedPort(port)))
			.collect(Collectors.toSet());
	}
	
	public HostPortWaitAbortableStrategy forPorts(final int... ports)
	{
		this.ports = ports;
		return this.self();
	}
	
	protected static class InternalCommandPortListeningCheck implements Callable<Boolean>
	{
		private static final Logger LOG = LoggerFactory.getLogger(InternalCommandPortListeningCheck.class);
		
		protected final WaitStrategyTarget waitStrategyTarget;
		protected final Set<Integer> internalPorts;
		protected final String command;
		protected final WaitStrategyCurrentState ec;
		
		protected InternalCommandPortListeningCheck(
			final WaitStrategyTarget waitStrategyTarget,
			final Set<Integer> internalPorts,
			final WaitStrategyCurrentState ec)
		{
			this.waitStrategyTarget = waitStrategyTarget;
			this.internalPorts = internalPorts;
			this.command = this.buildCommand(internalPorts);
			this.ec = ec;
		}
		
		protected String buildCommand(final Set<Integer> internalPorts)
		{
			return "while true; do ( true "
				+ internalPorts.stream()
				.map(port -> " && "
					+ " ("
					+ String.format("grep -i ':0*%x' /proc/net/tcp*", port)
					+ " || "
					+ String.format("nc -vz -w 1 localhost %d", port)
					+ " || "
					+ String.format("/bin/bash -c '</dev/tcp/localhost/%d'", port)
					+ ")")
				.collect(Collectors.joining())
				+ " ) && exit 0 || sleep 0.1; done";
		}
		
		@Override
		public Boolean call()
		{
			final Instant before = Instant.now();
			
			try
			{
				final Container.ExecResult result = ExecInContainerPattern.execInContainer(
					this.waitStrategyTarget.getDockerClient(),
					this.waitStrategyTarget.getContainerInfo(),
					"/bin/sh", "-c", this.command);
				LOG.trace(
					"Check for {} took {}. Result code '{}', stdout message: '{}'",
					this.internalPorts,
					Duration.between(before, Instant.now()),
					result.getExitCode(),
					result.getStdout());
				final int exitCode = result.getExitCode();
				if(exitCode != 0 && exitCode != 1)
				{
					LOG.warn("An exception while executing the internal check: {}", result);
					this.ec.withErrorMessage("Non successful exit: " + result);
				}
				
				final boolean success = exitCode == 0;
				if(success)
				{
					this.ec.successfullyFinished();
				}
				return success;
			}
			catch(final Exception e)
			{
				this.ec.withException(e);
				throw new IllegalStateException(e);
			}
		}
	}
	
	
	protected static class ExternalPortListeningCheck implements Callable<Boolean>
	{
		protected final ContainerState containerState;
		protected final Set<Integer> externalLivenessCheckPorts;
		protected final WaitStrategyCurrentState ec;
		
		protected ExternalPortListeningCheck(
			final ContainerState containerState,
			final Set<Integer> externalLivenessCheckPorts,
			final WaitStrategyCurrentState ec)
		{
			this.containerState = containerState;
			this.externalLivenessCheckPorts = externalLivenessCheckPorts;
			this.ec = ec;
		}
		
		@SuppressWarnings("resource")
		@Override
		public Boolean call()
		{
			if(this.externalLivenessCheckPorts.isEmpty())
			{
				return true;
			}
			
			final String address = this.containerState.getHost();
			if(this.externalLivenessCheckPorts.size() == 1)
			{
				return this.executeAndRecord(() -> checkPort(
					address,
					this.externalLivenessCheckPorts.iterator().next()));
			}
			
			final ExecutorService executor = TCIExecutorServiceHolder.instance();
			return this.executeAndRecord(() -> CompletableFuture.allOf(this.externalLivenessCheckPorts.stream()
				.map(port -> CompletableFuture.runAsync(() -> checkPort(address, port), executor))
				.toArray(CompletableFuture[]::new)).join());
		}
		
		protected boolean executeAndRecord(final Runnable run)
		{
			try
			{
				run.run();
				this.ec.successfullyFinished();
			}
			catch(final RuntimeException rex)
			{
				this.ec.withException(rex);
				throw rex;
			}
			return true;
		}
		
		@SuppressWarnings("java:S1141")
		protected static void checkPort(final String address, final int port)
		{
			try
			{
				final Socket socket = new Socket();
				
				try
				{
					final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
					socket.connect(inetSocketAddress, 1000);
				}
				catch(final IOException ioe)
				{
					try
					{
						socket.close();
					}
					catch(final IOException closeIoe)
					{
						ioe.addSuppressed(closeIoe);
					}
					
					throw ioe;
				}
				
				socket.close();
			}
			catch(final Exception ex)
			{
				throw new IllegalStateException("Socket not listening yet: " + port, ex);
			}
		}
	}
}
