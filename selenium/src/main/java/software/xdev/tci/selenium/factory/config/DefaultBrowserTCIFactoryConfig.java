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
package software.xdev.tci.selenium.factory.config;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.tci.config.DefaultConfig;
import software.xdev.tci.selenium.factory.BrowserTCIFactory;
import software.xdev.testcontainers.selenium.containers.browser.BrowserWebDriverContainer;


public class DefaultBrowserTCIFactoryConfig extends DefaultConfig implements BrowserTCIFactoryConfig
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultBrowserTCIFactoryConfig.class);
	
	public static final String RECORD_MODE = "recordMode";
	public static final String RECORD_DIR = "recordDir";
	public static final String VNC_ENABLED = "vncEnabled";
	public static final String BIDI_ENABLED = "bidiEnabled";
	public static final String DEACTIVATE_CDP_IF_POSSIBLE = "deactivateCdpIfPossible";
	public static final String BROWSER_CONSOLE_LOG_LEVEL = "browserConsoleLogLevel";
	
	public static final String DEFAULT_RECORD_DIR = "target/records";
	
	protected BrowserWebDriverContainer.RecordingMode systemRecordingMode;
	protected Path dirForRecords;
	protected Boolean vncEnabled;
	protected Boolean bidiEnabled;
	protected Boolean deactivateCdpIfPossible;
	protected BrowserTCIFactory.BrowserConsoleLogLevel browserConsoleLogLevel;
	
	@Override
	protected String propertyNamePrefix()
	{
		return "tci.selenium";
	}
	
	@Override
	public BrowserWebDriverContainer.RecordingMode recordingMode()
	{
		if(this.systemRecordingMode == null)
		{
			final String resolvedRecordMode = this.resolve(RECORD_MODE).orElse(null);
			this.systemRecordingMode = Stream.of(BrowserWebDriverContainer.RecordingMode.values())
				.filter(rm -> rm.toString().equals(resolvedRecordMode))
				.findFirst()
				.orElse(BrowserWebDriverContainer.RecordingMode.RECORD_FAILING);
			LOG.info("Default Recording Mode='{}'", this.systemRecordingMode);
		}
		return this.systemRecordingMode;
	}
	
	@Override
	public Path dirForRecords()
	{
		if(this.dirForRecords == null)
		{
			this.dirForRecords = Path.of(this.resolve(RECORD_DIR).orElse(DEFAULT_RECORD_DIR));
			final boolean wasCreated = this.dirForRecords.toFile().mkdirs();
			LOG.info(
				"Default directory for records='{}', created={}", this.dirForRecords.toAbsolutePath(),
				wasCreated);
		}
		
		return this.dirForRecords;
	}
	
	@Override
	public boolean vncEnabled()
	{
		if(this.vncEnabled == null)
		{
			this.vncEnabled = this.resolveBool(VNC_ENABLED, false);
			LOG.info("VNC enabled={}", this.vncEnabled);
		}
		return this.vncEnabled;
	}
	
	@Override
	public boolean bidiEnabled()
	{
		if(this.bidiEnabled == null)
		{
			this.bidiEnabled = this.resolveBool(BIDI_ENABLED, true);
			LOG.info("BiDi enabled={}", this.bidiEnabled);
		}
		return this.bidiEnabled;
	}
	
	@Override
	public boolean deactivateCdpIfPossible()
	{
		if(this.deactivateCdpIfPossible == null)
		{
			this.deactivateCdpIfPossible = this.resolveBool(DEACTIVATE_CDP_IF_POSSIBLE, true);
			LOG.info("DeactivateCDPIfPossible={}", this.deactivateCdpIfPossible);
		}
		return this.deactivateCdpIfPossible;
	}
	
	@Override
	public BrowserTCIFactory.BrowserConsoleLogLevel browserConsoleLogLevel()
	{
		if(this.browserConsoleLogLevel == null)
		{
			this.browserConsoleLogLevel = this.resolve(BROWSER_CONSOLE_LOG_LEVEL)
				.map(BrowserTCIFactory.BrowserConsoleLogLevel::valueOf)
				.orElse(BrowserTCIFactory.BrowserConsoleLogLevel.ERROR);
			LOG.info("BrowserConsoleLogLevel={}", this.browserConsoleLogLevel);
		}
		return this.browserConsoleLogLevel;
	}
}
