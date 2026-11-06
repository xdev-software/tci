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
package software.xdev.tci.concurrent;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;


/**
 * This class is designed as a stopgap measure until Lazy Constants are introduced.
 *
 * @apiNote once released you should use lazy constants directly
 */
public final class LazyConstantSuppliers
{
	/**
	 * Identical to {@link Suppliers#memoize(Supplier)} but also caches exceptions
	 */
	public static <T> Supplier<T> of(final Supplier<T> delegate)
	{
		if(delegate instanceof LazyConstantSupplier)
		{
			return delegate;
		}
		return new LazyConstantSupplier<>(delegate);
	}
	
	static class LazyConstantSupplier<T> extends Suppliers.NonSerializableMemoizingSupplier<T>
	{
		protected String exceptionType;
		
		protected LazyConstantSupplier(final Supplier<T> delegate)
		{
			super(delegate);
		}
		
		@Override
		protected T initiallyComputeValue()
		{
			try
			{
				this.value = this.delegate.get();
				this.setComputed();
				return this.value;
			}
			catch(final RuntimeException rex)
			{
				this.setComputed();
				this.exceptionType = rex.getClass().getName();
				throw unableToAccessConstant(this.exceptionType, rex);
			}
		}
		
		@Override
		protected T returnCachedValue()
		{
			if(this.exceptionType != null)
			{
				throw unableToAccessConstant(this.exceptionType, null);
			}
			return super.returnCachedValue();
		}
		
		static NoSuchElementException unableToAccessConstant(final String exceptionType, final Throwable cause)
		{
			return new NoSuchElementException(
				"Unable to access the constant because " + exceptionType + " was thrown at initial computation",
				cause);
		}
		
		@Override
		public String toString()
		{
			final Supplier<T> currentDelegate = this.delegate;
			return "LazyConstantSuppliers.of("
				+ (currentDelegate == COMPUTED
				? "<supplier that returned " + Objects.requireNonNullElse(this.exceptionType, this.value) + ">"
				: currentDelegate)
				+ ")";
		}
	}
	
	private LazyConstantSuppliers()
	{
	}
}
