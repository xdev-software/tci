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
