package software.xdev.tci.config;

import java.util.Optional;


/**
 * Utility class meant for small classes where it's not worth to use a full {@link DefaultConfig}
 */
public class StandaloneGlobalConfigResolver extends DefaultConfig
{
	private static StandaloneGlobalConfigResolver instance = new StandaloneGlobalConfigResolver();
	
	// Can be used for "emergency" overriding the default implementation
	protected static void setInstance(final StandaloneGlobalConfigResolver instance)
	{
		StandaloneGlobalConfigResolver.instance = instance;
	}
	
	public static StandaloneGlobalConfigResolver instance()
	{
		return instance;
	}
	
	@Override
	public Optional<Integer> resolveInt(final String propertyName)
	{
		return super.resolveInt(propertyName);
	}
	
	@Override
	protected String propertyNamePrefix()
	{
		return "tci";
	}
}
