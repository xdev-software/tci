package software.xdev.tci.concurrent;

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
		if(delegate instanceof NonSerializableMemoizingWithExceptionSupplier)
		{
			return delegate;
		}
		return new NonSerializableMemoizingWithExceptionSupplier<>(delegate);
	}
	
	static class NonSerializableMemoizingWithExceptionSupplier<T> extends Suppliers.NonSerializableMemoizingSupplier<T>
	{
		protected RuntimeException exception;
		
		protected NonSerializableMemoizingWithExceptionSupplier(final Supplier<T> delegate)
		{
			super(delegate);
		}
		
		@Override
		protected T initiallyComputeValue()
		{
			try
			{
				return super.initiallyComputeValue();
			}
			catch(final RuntimeException rex)
			{
				this.exception = rex;
				return null;
			}
		}
		
		@Override
		protected T returnCachedValue()
		{
			if(this.exception != null)
			{
				throw this.exception;
			}
			return super.returnCachedValue();
		}
		
		@Override
		public String toString()
		{
			final Supplier<T> currentDelegate = this.delegate;
			return "LazyConstantSuppliers.of("
				+ (currentDelegate == SUCCESSFULLY_COMPUTED
				? "<supplier that returned " + Objects.requireNonNullElse(this.exception, this.value) + ">"
				: currentDelegate)
				+ ")";
		}
	}
	
	private LazyConstantSuppliers()
	{
	}
}
