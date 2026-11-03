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
package software.xdev.tci.factory.registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import software.xdev.tci.TCI;
import software.xdev.tci.concurrent.Suppliers;
import software.xdev.tci.concurrent.TCIExecutorServiceHolder;
import software.xdev.tci.config.StandaloneGlobalConfigResolver;
import software.xdev.tci.factory.TCIFactory;


public class DefaultTCIFactoryRegistry implements TCIFactoryRegistry
{
	protected final Set<TCIFactory<?, ?>> factories = Collections.synchronizedSet(new HashSet<>());
	protected final Set<TCIFactory<?, ?>> warmedUpFactories =
		Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
	
	protected final Supplier<Integer> warmUpTimeoutMinSupplier;
	
	public DefaultTCIFactoryRegistry()
	{
		this.warmUpTimeoutMinSupplier = Suppliers.memoize(this::determineWarmUpTimeoutMin);
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	protected int determineWarmUpTimeoutMin()
	{
		return StandaloneGlobalConfigResolver.instance().resolveInt("warmup.timeout-min")
			.map(i -> Math.max(i, 1))
			.orElse(15);
	}
	
	@Override
	public void register(final TCIFactory<?, ?> tciFactory)
	{
		this.factories.add(tciFactory);
	}
	
	@Override
	public void unRegister(final TCIFactory<?, ?> tciFactory)
	{
		this.factories.remove(tciFactory);
	}
	
	@SuppressWarnings("PMD.AvoidSynchronizedStatement") // Required by synchronizedList
	@Override
	public void warmUp()
	{
		synchronized(this.factories)
		{
			this.factories.stream()
				// Check if the factory was already warmed up
				.filter(f -> !this.warmedUpFactories.contains(f))
				// Collect first otherwise nothing will be run in parallel
				.collect(Collectors.toMap(Function.identity(), this::warmUpFactory))
				.forEach(this::waitForWarmUpCF);
		}
	}
	
	protected void waitForWarmUpCF(final TCIFactory<?, ?> factory, final CompletableFuture<?> cf)
	{
		try
		{
			cf.get(this.warmUpTimeoutMinSupplier.get(), TimeUnit.MINUTES);
		}
		catch(final TimeoutException ex)
		{
			throw this.factoryWarmUpException("Timed out", factory, ex);
		}
		catch(final InterruptedException ex)
		{
			Thread.currentThread().interrupt();
			throw this.factoryWarmUpException("Got interrupted", factory, ex);
		}
		catch(final ExecutionException ex)
		{
			throw this.factoryWarmUpException("Execution failed", factory, ex);
		}
	}
	
	protected RuntimeException factoryWarmUpException(
		final String msg,
		final TCIFactory<?, ?> factory,
		final Exception underlyingEx)
	{
		return new IllegalStateException("WarmUp failed: " + msg + " - " + factory.getFactoryName(), underlyingEx);
	}
	
	protected CompletableFuture<Void> warmUpFactory(final TCIFactory<?, ?> factory)
	{
		this.warmedUpFactories.add(factory);
		return CompletableFuture.runAsync(factory::warmUp, TCIExecutorServiceHolder.instance());
	}
	
	@Override
	public Set<TCIFactory<?, ?>> getFactories()
	{
		return this.factories;
	}
	
	@Override
	@SuppressWarnings("java:S1452")
	public Map<TCIFactory<?, ?>, Map<TCI<?>, CompletableFuture<Boolean>>> getReturnedAndInUse()
	{
		return this.factories.stream()
			.filter(f -> !f.getReturnedAndInUse().isEmpty())
			.collect(Collectors.toMap(
				Function.identity(),
				f -> f.getReturnedAndInUse().entrySet()
					.stream()
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
	}
}
