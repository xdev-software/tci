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
package software.xdev.tci.selenium.testbase;

import java.util.Objects;
import java.util.function.Function;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.lifecycle.TestDescription;

import software.xdev.tci.selenium.BrowserTCI;


public abstract class SeleniumRecordingExtension implements AfterTestExecutionCallback
{
	private static final Logger LOG = LoggerFactory.getLogger(SeleniumRecordingExtension.class);
	
	protected final Function<ExtensionContext, BrowserTCI> tciExtractor;
	
	protected SeleniumRecordingExtension(
		final Function<ExtensionContext, BrowserTCI> tciExtractor)
	{
		this.tciExtractor = Objects.requireNonNull(tciExtractor);
	}
	
	@Override
	public void afterTestExecution(final ExtensionContext context) throws Exception
	{
		final BrowserTCI browserTCI = this.tciExtractor.apply(context);
		if(browserTCI != null)
		{
			LOG.debug("Trying to capture video");
			
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
						final String testClassName =
							this.cleanForFilename(context.getRequiredTestClass().getSimpleName());
						final String displayName = this.cleanForFilename(context.getDisplayName());
						return System.currentTimeMillis()
							+ "_"
							+ testClassName
							+ "_"
							// Cut off otherwise file name is too long
							+ displayName.substring(0, Math.min(displayName.length(), 200));
					}
					
					private String cleanForFilename(final String str)
					{
						return str.replace(' ', '_')
							.replaceAll("[^A-Za-z0-9#_-]", "")
							.toLowerCase();
					}
				}, context.getExecutionException());
		}
		LOG.debug("AfterTestExecution done");
	}
}
