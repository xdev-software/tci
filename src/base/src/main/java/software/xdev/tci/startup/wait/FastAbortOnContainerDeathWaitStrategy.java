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
package software.xdev.tci.startup.wait;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;

import software.xdev.tci.concurrent.ExecutorServiceCreatorHolder;
import software.xdev.tci.startup.wait.holder.WaitStrategyCurrentState;
import software.xdev.tci.startup.wait.strategy.AbstractWaitAbortableStrategy;
import software.xdev.tci.startup.wait.strategy.WaitAllAbortableStrategy;


/**
 * A "wrapper" strategy that causes a fast-abort when the container dies.
 * <p>
 * This is helpful when the container dies during the startup phase as the default wait-strategies
 * would wait for the timeout to expire which can take up to a few minutes.
 * </p>
 */
public class FastAbortOnContainerDeathWaitStrategy
	extends ImprovedAbstractWaitStrategy<FastAbortOnContainerDeathWaitStrategy>
{
	protected static final ExecutorService DEFAULT_EXECUTOR =
		ExecutorServiceCreatorHolder.instance().createUnlimited("TCI-testcontainers-wait");
	
	protected static final RateLimiter DEFAULT_RATE_LIMITER = RateLimiterBuilder
		.newBuilder()
		.withRate(10, TimeUnit.MINUTES) // all 6s
		.withConstantThroughput()
		.build();
	
	protected final AbstractWaitAbortableStrategy<?> waitStrategy;
	protected final ExecutorService executor;
	
	public FastAbortOnContainerDeathWaitStrategy(final AbstractWaitAbortableStrategy<?> waitStrategy)
	{
		this(waitStrategy, DEFAULT_EXECUTOR);
	}
	
	protected FastAbortOnContainerDeathWaitStrategy(
		final AbstractWaitAbortableStrategy<?> waitStrategy,
		final ExecutorService executor)
	{
		this.waitStrategy = waitStrategy;
		this.executor = executor;
		
		this.withRateLimiter(DEFAULT_RATE_LIMITER);
	}
	
	@Override
	public void waitUntilReady(final WaitStrategyTarget target)
	{
		final AbortMonitor abortMonitor = new AbortMonitor();
		final CompletableFuture<Void> cfWaitStrategy = CompletableFuture.runAsync(
			() -> this.waitStrategy.waitUntilReady(
				target,
				abortMonitor,
				this.executor,
				new WaitStrategyCurrentState(this.waitStrategy)),
			this.executor
		);
		
		final CompletableFuture<Void> cfContainerDeathWatchDog = CompletableFuture.runAsync(
			() -> this.runCheckIfContainerIsDead(target, abortMonitor, cfWaitStrategy),
			this.executor
		);
		
		try
		{
			CompletableFuture.anyOf(cfWaitStrategy, cfContainerDeathWatchDog).join();
		}
		finally
		{
			abortMonitor.trigger(new CancellationException("Completed"));
		}
	}
	
	@SuppressWarnings("java:S5411") // Irrelevant here!
	protected void runCheckIfContainerIsDead(
		final WaitStrategyTarget target,
		final AbortMonitor abortMonitor,
		final CompletableFuture<?> cfWaitStrategy)
	{
		while(!abortMonitor.shouldAbort() && !cfWaitStrategy.isDone())
		{
			if(!this.rateLimiterWhenReadyExceptionless(() -> this.isContainerAlive(target)))
			{
				final IllegalStateException abortEx = new IllegalStateException(
					"Container " + target.getContainerId() + " is dead - Aborting");
				abortMonitor.trigger(abortEx);
				throw abortEx;
			}
		}
	}
	
	@SuppressWarnings("PMD.PreserveStackTrace")
	protected <T> T rateLimiterWhenReadyExceptionless(final Supplier<T> supplier)
	{
		try
		{
			return this.getRateLimiter().getWhenReady(supplier::get);
		}
		catch(final Exception ex)
		{
			if(ex instanceof final RuntimeException rex)
			{
				throw rex;
			}
			throw new RuntimeException(ex);
		}
	}
	
	protected boolean isContainerAlive(final WaitStrategyTarget target)
	{
		return Optional.ofNullable(target.getCurrentContainerInfo().getState())
			.filter(s -> Boolean.TRUE.equals(s.getRunning())
				|| Boolean.TRUE.equals(s.getPaused()))
			.isPresent();
	}
	
	@Override
	public WaitStrategy withStartupTimeout(final Duration startupTimeout)
	{
		this.waitStrategy.withStartupTimeout(startupTimeout);
		return this;
	}
	
	/**
	 * Creates a new instance with {@link WaitAllAbortableStrategy} as inner strategy and allows configuring it.
	 */
	public static FastAbortOnContainerDeathWaitStrategy waitAll(
		final UnaryOperator<WaitAllAbortableStrategy> configure)
	{
		return new FastAbortOnContainerDeathWaitStrategy(configure.apply(new WaitAllAbortableStrategy()));
	}
}
