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
package software.xdev.tci.selenium.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.MutableCapabilities;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.images.RemoteDockerImage;

import software.xdev.tci.factory.TCIFactory;
import software.xdev.tci.logging.JULtoSLF4JRedirector;
import software.xdev.tci.selenium.BrowserTCI;
import software.xdev.tci.selenium.TestBrowser;
import software.xdev.tci.selenium.containers.SeleniumBrowserWebDriverContainer;
import software.xdev.tci.tracing.TCITracer;
import software.xdev.testcontainers.selenium.containers.recorder.SeleniumRecordingContainer;


public class BrowsersTCIFactory implements TCIFactory<SeleniumBrowserWebDriverContainer, BrowserTCI>
{
	protected final Map<String, BrowserTCIFactory> browserFactories = new ConcurrentHashMap<>();
	protected boolean alreadyWarmedUp;
	
	public BrowsersTCIFactory()
	{
		this(Arrays.stream(TestBrowser.values())
			.map(TestBrowser::getCapabilityFactory)
			.map(Supplier::get));
	}
	
	public BrowsersTCIFactory(final Stream<MutableCapabilities> caps)
	{
		caps.forEach(cap -> this.browserFactories.put(cap.getBrowserName(), new BrowserTCIFactory(cap)));
	}
	
	public BrowsersTCIFactory(final Map<String, BrowserTCIFactory> browserFactories)
	{
		this.browserFactories.putAll(browserFactories);
	}
	
	@Override
	public void warmUp()
	{
		if(this.alreadyWarmedUp)
		{
			return;
		}
		
		this.warmUpInternal();
	}
	
	protected synchronized void warmUpInternal()
	{
		if(this.alreadyWarmedUp)
		{
			return;
		}
		
		// Selenium uses JUL
		JULtoSLF4JRedirector.redirect();
		
		this.browserFactories.values().forEach(BrowserTCIFactory::warmUp);
		
		// Pull video recorder
		CompletableFuture.runAsync(() -> {
			try
			{
				new RemoteDockerImage(SeleniumRecordingContainer.DEFAULT_IMAGE).get();
			}
			catch(final Exception e)
			{
				LoggerFactory.getLogger(this.getClass())
					.warn("Failed to pull {}", SeleniumRecordingContainer.DEFAULT_IMAGE, e);
			}
		});
		this.alreadyWarmedUp = true;
	}
	
	@SuppressWarnings("resource")
	public BrowserTCI getNew(
		final MutableCapabilities capabilities,
		final Network network,
		final String... networkAliases)
	{
		return this.browserFactories.computeIfAbsent(
				capabilities.getBrowserName(),
				x -> new BrowserTCIFactory(capabilities))
			.getNew(network, networkAliases);
	}
	
	@Override
	public void close()
	{
		final List<CompletableFuture<Void>> cfFactories = this.browserFactories.values().stream()
			.map(f -> CompletableFuture.runAsync(f::close))
			.toList();
		cfFactories.forEach(CompletableFuture::join);
	}
	
	@Override
	public Map<BrowserTCI, CompletableFuture<Boolean>> getReturnedAndInUse()
	{
		return this.browserFactories.values()
			.stream()
			.map(BrowserTCIFactory::getReturnedAndInUse)
			.map(Map::entrySet)
			.flatMap(Collection::stream)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
	
	@Override
	public TCITracer getTracer()
	{
		return null;
	}
}
