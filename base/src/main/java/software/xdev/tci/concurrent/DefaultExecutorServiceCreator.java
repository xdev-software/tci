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
package software.xdev.tci.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class DefaultExecutorServiceCreator implements ExecutorServiceCreator
{
	@Override
	public ExecutorService createUnlimited(final String threadNamePrefix)
	{
		return Executors.newCachedThreadPool(factory(threadNamePrefix));
	}
	
	@Override
	public ExecutorService createFixed(final String threadNamePrefix, final int size)
	{
		return Executors.newFixedThreadPool(size, factory(threadNamePrefix));
	}
	
	@Override
	public ScheduledExecutorService createdSingleScheduled(final String threadNamePrefix)
	{
		return Executors.newSingleThreadScheduledExecutor(factory(threadNamePrefix));
	}
	
	private static ThreadFactory factory(final String threadNamePrefix)
	{
		final AtomicInteger nextThreadId = new AtomicInteger(0);
		return r -> {
			final Thread t = new Thread(r, threadNamePrefix + "-" + nextThreadId.getAndIncrement());
			t.setDaemon(true);
			return t;
		};
	}
}
