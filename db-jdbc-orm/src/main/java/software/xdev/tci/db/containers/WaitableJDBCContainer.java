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
package software.xdev.tci.db.containers;

import java.sql.Connection;
import java.util.concurrent.TimeUnit;

import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.ratelimits.RateLimiterBuilder;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategyTarget;


public interface WaitableJDBCContainer extends WaitStrategyTarget
{
	default WaitStrategy completeJDBCWaitStrategy()
	{
		return new WaitAllStrategy()
			.withStrategy(Wait.defaultWaitStrategy())
			.withStrategy(new JDBCWaitStrategy());
	}
	
	WaitStrategy getWaitStrategy();
	
	default void waitUntilContainerStarted()
	{
		final WaitStrategy waitStrategy = this.getWaitStrategy();
		if(waitStrategy != null)
		{
			waitStrategy.waitUntilReady(this);
		}
	}
	
	/**
	 * @apiNote Assumes that the container is already started
	 */
	class JDBCWaitStrategy extends AbstractWaitStrategy
	{
		@SuppressWarnings("checkstyle:MagicNumber")
		public JDBCWaitStrategy()
		{
			this.withRateLimiter(RateLimiterBuilder.newBuilder()
				.withRate(200, TimeUnit.MILLISECONDS)
				.withConstantThroughput()
				.build());
		}
		
		@SuppressWarnings("PMD.PreserveStackTrace")
		@Override
		protected void waitUntilReady()
		{
			if(!(this.waitStrategyTarget instanceof final JdbcDatabaseContainer<?> container))
			{
				throw new IllegalArgumentException(
					"Container must implement JdbcDatabaseContainer and WaitableJDBCContainer");
			}
			
			try
			{
				this.waitUntilJDBCValid(container);
			}
			catch(final TimeoutException e)
			{
				throw new ContainerLaunchException(
					"JDBCContainer cannot be accessed by (JDBC URL: "
						+ container.getJdbcUrl()
						+ "), please check container logs");
			}
		}
		
		protected void waitUntilJDBCValid(final JdbcDatabaseContainer<?> container)
		{
			Unreliables.retryUntilTrue(
				(int)this.startupTimeout.getSeconds(),
				TimeUnit.SECONDS,
				// Rate limit creation of connections as this is quite an expensive operation
				() -> this.getRateLimiter().getWhenReady(() -> {
					try(final Connection connection = container.createConnection(""))
					{
						return this.waitUntilJDBCConnectionValidated(container, connection);
					}
				})
			);
		}
		
		protected boolean waitUntilJDBCConnectionValidated(
			final JdbcDatabaseContainer<?> container,
			final Connection connection)
		{
			return Unreliables.retryUntilSuccess(
				(int)this.startupTimeout.getSeconds(),
				TimeUnit.SECONDS,
				() -> this.validateJDBCConnection(connection));
		}
		
		@SuppressWarnings("java:S112")
		protected boolean validateJDBCConnection(final Connection connection) throws Exception
		{
			return connection.isValid(10);
		}
	}
}
