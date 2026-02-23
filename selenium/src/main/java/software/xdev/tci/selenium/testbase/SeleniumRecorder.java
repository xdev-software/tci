package software.xdev.tci.selenium.testbase;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.lifecycle.TestDescription;

import software.xdev.tci.selenium.BrowserTCI;


@SuppressWarnings("PMD.MoreThanOneLogger")
public class SeleniumRecorder
{
	protected static final Logger INTERNAL_LOG = LoggerFactory.getLogger(SeleniumRecorder.class);
	
	protected final Logger logger;
	
	public SeleniumRecorder()
	{
		this(INTERNAL_LOG);
	}
	
	public SeleniumRecorder(final Logger logger)
	{
		this.logger = logger;
	}
	
	public void afterTest(
		final ExtensionContext context,
		final Optional<BrowserTCI> optBrowserTCI,
		final Supplier<String> fileSystemFriendlyNameSupplier)
	{
		optBrowserTCI.ifPresent(browserTCI ->
		{
			this.logger.debug("Trying to capture video");
			
			browserTCI.afterTest(
				new TestDescription()
				{
					@Override
					public String getTestId()
					{
						return this.getFilesystemFriendlyName();
					}
					
					@SuppressWarnings("checkstyle:MagicNumber")
					@Override
					public String getFilesystemFriendlyName()
					{
						return fileSystemFriendlyNameSupplier.get();
					}
				}, context.getExecutionException());
		});
		this.logger.debug("afterTest done");
	}
}
