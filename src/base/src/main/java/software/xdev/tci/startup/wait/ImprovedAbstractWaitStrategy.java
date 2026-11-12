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
import java.util.concurrent.TimeUnit;

import org.rnorth.ducttape.ratelimits.RateLimiter;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.testcontainers.containers.wait.strategy.WaitStrategy;


/**
 * Rewrite of {@link org.testcontainers.containers.wait.strategy.AbstractWaitStrategy}
 */
public abstract class ImprovedAbstractWaitStrategy<S extends ImprovedAbstractWaitStrategy<S>>
	implements WaitStrategy
{
	private static final RateLimiter DOCKER_CLIENT_RATE_LIMITER = RateLimiterBuilder
		.newBuilder()
		.withRate(1, TimeUnit.SECONDS)
		.withConstantThroughput()
		.build();
	
	private Duration startupTimeout = Duration.ofSeconds(60);
	
	private RateLimiter rateLimiter = DOCKER_CLIENT_RATE_LIMITER;
	
	@Override
	public WaitStrategy withStartupTimeout(final Duration startupTimeout)
	{
		this.startupTimeout = startupTimeout;
		return this;
	}
	
	public S withStartupTimeoutReturnSelf(final Duration startupTimeout)
	{
		this.withStartupTimeout(startupTimeout);
		return this.self();
	}
	
	@SuppressWarnings("unchecked")
	protected S self()
	{
		return (S)this;
	}
	
	protected Duration getStartupTimeout()
	{
		return this.startupTimeout;
	}
	
	protected RateLimiter getRateLimiter()
	{
		return this.rateLimiter;
	}
	
	public WaitStrategy withRateLimiter(final RateLimiter rateLimiter)
	{
		this.rateLimiter = rateLimiter;
		return this;
	}
}
