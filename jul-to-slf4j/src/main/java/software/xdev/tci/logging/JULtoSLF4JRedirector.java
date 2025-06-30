package software.xdev.tci.logging;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;


public class JULtoSLF4JRedirector
{
	static final JULtoSLF4JRedirector INSTANCE = new JULtoSLF4JRedirector();
	
	protected boolean installed;
	
	protected JULtoSLF4JRedirector()
	{
	}
	
	protected void redirectInternal()
	{
		if(this.installed)
		{
			return;
		}
		if(SLF4JBridgeHandler.isInstalled())
		{
			this.installed = true;
			return;
		}
		
		LoggerFactory.getLogger(JULtoSLF4JRedirector.class).debug("Installing SLF4JBridgeHandler");
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		this.installed = true;
	}
	
	public static void redirect()
	{
		INSTANCE.redirectInternal();
	}
}
