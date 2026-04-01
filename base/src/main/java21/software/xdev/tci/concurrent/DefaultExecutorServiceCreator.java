package software.xdev.tci.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;


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
		return Thread.ofVirtual()
			.name(threadNamePrefix + "-", 0)
			.factory();
	}
}
