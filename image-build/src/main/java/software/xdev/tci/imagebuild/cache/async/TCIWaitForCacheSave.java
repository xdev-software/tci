package software.xdev.tci.imagebuild.cache.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.tci.imagebuild.config.BuildImageHandlerConfig;


public class TCIWaitForCacheSave implements TestExecutionListener
{
	private static final Logger LOG = LoggerFactory.getLogger(TCIWaitForCacheSave.class);
	
	private BuildImageHandlerConfig config;
	
	@Override
	public void testPlanExecutionStarted(final TestPlan testPlan)
	{
		this.config = BuildImageHandlerConfig.instance();
	}
	
	@Override
	public void testPlanExecutionFinished(final TestPlan testPlan)
	{
		if(!this.config.waitForSaveCacheInBackground())
		{
			return;
		}
		
		final List<CompletableFuture<?>> cfs = TCICacheSaveRegistry.instance().get();
		if(cfs.isEmpty())
		{
			return;
		}
		
		LOG.info("Waiting for cache saves to finish...");
		final long startMs = System.currentTimeMillis();
		try
		{
			CompletableFuture.allOf(cfs.toArray(CompletableFuture[]::new))
				.get(10, TimeUnit.MINUTES);
		}
		catch(final InterruptedException e)
		{
			LOG.warn("Got interrupted", e);
			Thread.currentThread().interrupt();
		}
		catch(final ExecutionException e)
		{
			LOG.warn("A cache save failed", e);
		}
		catch(final TimeoutException e)
		{
			LOG.warn("Timed out while waiting for cache save", e);
		}
		
		LOG.info(
			"Finished waiting for {}x cache saves, took {}ms",
			cfs.size(),
			System.currentTimeMillis() - startMs);
	}
}
