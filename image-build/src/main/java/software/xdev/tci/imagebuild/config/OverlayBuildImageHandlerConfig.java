package software.xdev.tci.imagebuild.config;

import java.util.Optional;


@SuppressWarnings({"java:S2789", "OptionalAssignedToNull"})
public class OverlayBuildImageHandlerConfig extends DefaultBuildImageHandlerConfig
{
	private final BuildImageHandlerConfig parentConfig;
	private final String prefixName;
	
	public OverlayBuildImageHandlerConfig(final BuildImageHandlerConfig parentConfig, final String prefixName)
	{
		this.parentConfig = parentConfig;
		this.prefixName = prefixName;
	}
	
	@Override
	protected String propertyNamePrefix()
	{
		return super.propertyNamePrefix() + "." + this.prefixName;
	}
	
	@Override
	public boolean deleteOnExit()
	{
		if(this.deleteOnExit == null)
		{
			this.deleteOnExit = this.resolveBool(DELETE_ON_EXIT, this.parentConfig.deleteOnExit());
		}
		return this.deleteOnExit;
	}
	
	@Override
	public String loggerForBuildPrefix()
	{
		if(this.loggerForBuildPrefix == null)
		{
			this.loggerForBuildPrefix = this.resolve(LOGGER_FOR_BUILD_PREFIX)
				.orElseGet(this.parentConfig::loggerForBuildPrefix);
		}
		return this.loggerForBuildPrefix;
	}
	
	@Override
	public Optional<String> cacheFrom()
	{
		if(this.cacheFrom == null)
		{
			this.cacheFrom = this.resolve(CACHE_FROM)
				.or(this.parentConfig::cacheFrom);
		}
		return this.cacheFrom;
	}
	
	@Override
	public Optional<String> cacheTo()
	{
		if(this.cacheTo == null)
		{
			this.cacheTo = this.resolve(CACHE_TO)
				.or(this.parentConfig::cacheTo);
		}
		return this.cacheTo;
	}
	
	@Override
	public boolean waitForSaveCacheInBackground()
	{
		if(this.waitForSaveCacheInBackground == null)
		{
			this.waitForSaveCacheInBackground =
				this.resolveBool(WAIT_FOR_SAVE_CACHE_IN_BACKGROUND, this.parentConfig.waitForSaveCacheInBackground());
		}
		return this.waitForSaveCacheInBackground;
	}
}
