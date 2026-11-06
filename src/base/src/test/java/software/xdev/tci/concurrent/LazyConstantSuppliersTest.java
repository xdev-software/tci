package software.xdev.tci.concurrent;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class LazyConstantSuppliersTest
{
	@Test
	void checkReturn()
	{
		final AtomicInteger counter = new AtomicInteger();
		
		final Supplier<Integer> supplier = LazyConstantSuppliers.of(counter::incrementAndGet);
		
		for(int i = 0; i < 2; i++)
		{
			Assertions.assertEquals(1, supplier.get());
			Assertions.assertEquals(1, counter.get());
		}
	}
	
	@Test
	void checkErrorHandling()
	{
		final AtomicInteger counter = new AtomicInteger();
		final Supplier<Object> supplier = LazyConstantSuppliers.of(() -> {
			counter.incrementAndGet();
			throw new IllegalStateException("TEST");
		});
		
		final NoSuchElementException ex1 =
			Assertions.assertThrows(NoSuchElementException.class, supplier::get);
		final Throwable ex1Cause = ex1.getCause();
		Assertions.assertInstanceOf(IllegalStateException.class, ex1Cause);
		Assertions.assertEquals("TEST", ex1Cause.getMessage());
		Assertions.assertEquals(1, counter.get());
		
		final NoSuchElementException ex2 = Assertions.assertThrows(NoSuchElementException.class, supplier::get);
		Assertions.assertNull(ex2.getCause());
		Assertions.assertEquals(1, counter.get());
	}
}
