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
package software.xdev.tci.factory.prestart.snapshoting;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.postgresql.PostgreSQLContainer;

import software.xdev.tci.db.containers.WaitableJDBCContainer;


class SnapshotManagerTest
{
	@Test
	void checkStoppingSnapshot()
	{
		final CustomPostgresContainer container = new CustomPostgresContainer();
		
		container.start();
		
		final var snapshotManager = new CommitedStoppingImageSnapshotManager<>();
		snapshotManager.snapshot(container);
		
		container.stop();
		
		final CustomPostgresContainer container2 = new CustomPostgresContainer();
		snapshotManager.tryReuse(container2);
		
		container2.start();
		
		container2.stop();
		
		// There should be no postgres report about a recovery going on
		Assertions.assertFalse(container2.getLogs().toLowerCase(Locale.ROOT).contains("recovery"));
	}
	
	@SuppressWarnings("PMD.PublicMemberInNonPublicType") // Required by API
	static class CustomPostgresContainer extends PostgreSQLContainer implements WaitableJDBCContainer
	{
		CustomPostgresContainer()
		{
			super("postgres:18-alpine");
			
			// Use different data location because the default location in a volume will not be snapshoted
			this.withEnv("PGDATA", "/var/lib/postgresql-persistent");
			this.withLogConsumer(new Slf4jLogConsumer(this.logger()));
			
			this.waitingFor(this.completeJDBCWaitStrategy());
		}
		
		@Override
		public WaitStrategy getWaitStrategy()
		{
			return super.getWaitStrategy();
		}
		
		@Override
		public void waitUntilContainerStarted()
		{
			WaitableJDBCContainer.super.waitUntilContainerStarted();
		}
	}
}
