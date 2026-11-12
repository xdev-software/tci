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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import software.xdev.tci.containers.HasWaitUntilStartedContainer;


/**
 * Works similar to {@link CommitedImageSnapshotManager} but stops the container before creating a snapshot
 * and starts it again afterwards.
 * <p>
 * This is required for some containers like e.g. Postgres.
 * Creating a snapshot of Postgres while running causes the subsequent container to start in recovery mode.
 * </p>
 */
public class CommitedStoppingImageSnapshotManager<C extends GenericContainer<?>> extends CommitedImageSnapshotManager<C>
{
	protected boolean logReflectionWarning = true;
	
	public CommitedStoppingImageSnapshotManager()
	{
	}
	
	public CommitedStoppingImageSnapshotManager(final String... ignoreWarningsVolumePaths)
	{
		super(ignoreWarningsVolumePaths);
	}
	
	public CommitedStoppingImageSnapshotManager(final Set<String> ignoreWarningsVolumePaths)
	{
		super(ignoreWarningsVolumePaths);
	}
	
	public CommitedStoppingImageSnapshotManager<C> withLogReflectionWarning(final boolean logReflectionWarning)
	{
		this.logReflectionWarning = logReflectionWarning;
		return this;
	}
	
	@Override
	public void snapshot(final C container)
	{
		this.commit(container, this::stopContainerBeforeCommit, this::startContainerAfterCommit);
	}
	
	protected void stopContainerBeforeCommit(final C container)
	{
		container.getDockerClient().stopContainerCmd(container.getContainerId()).exec();
	}
	
	protected void startContainerAfterCommit(final C container)
	{
		container.getDockerClient().startContainerCmd(container.getContainerId()).exec();
		this.waitUntilContainerIsStartedAfterCommit(container);
	}
	
	protected void waitUntilContainerIsStartedAfterCommit(final C container)
	{
		if(container instanceof final HasWaitUntilStartedContainer hasWaitUntilStartedContainer)
		{
			hasWaitUntilStartedContainer.waitUntilContainerStarted();
			return;
		}
		
		if(this.logReflectionWarning)
		{
			LoggerFactory.getLogger(this.getClass())
				.warn(
					"Will use reflection to wait until container is started. "
						+ "It's recommended to extend the container from {} for more stability and performance.",
					HasWaitUntilStartedContainer.class.getSimpleName());
			// Only report the warning once
			this.logReflectionWarning = false;
		}
		this.waitUntilContainerIsStartedAfterCommitUsingReflection(container);
	}
	
	protected void waitUntilContainerIsStartedAfterCommitUsingReflection(final C container)
	{
		try
		{
			final Method mWaitUntilContainerStarted =
				container.getClass().getDeclaredMethod("waitUntilContainerStarted");
			mWaitUntilContainerStarted.setAccessible(true);
			mWaitUntilContainerStarted.invoke(container);
		}
		catch(final NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
		{
			throw new IllegalStateException(
				"Failed to reflectively access waitUntilContainerStarted. "
					+ "As a workaround you can try to extend your container from "
					+ HasWaitUntilStartedContainer.class.getSimpleName(),
				e);
		}
	}
	
	@Override
	public boolean requiresFixatedPorts()
	{
		// This is only needed for the first snapshot because stopping the container can lead to losing it's ports
		// See moby/moby#44137 for details
		return this.cachedImage.get() == null;
	}
}
