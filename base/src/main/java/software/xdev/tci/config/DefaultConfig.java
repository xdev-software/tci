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
package software.xdev.tci.config;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;


public abstract class DefaultConfig
{
	protected boolean getBool(final String property, final boolean defaultValue)
	{
		return this.getBool(property, () -> defaultValue);
	}
	
	protected boolean getBool(
		final String property,
		final BooleanSupplier defaultValueSupplier)
	{
		return Optional.ofNullable(System.getProperty(property))
			.map(v -> "1".equals(v) || Boolean.parseBoolean(v))
			.orElseGet(defaultValueSupplier::getAsBoolean);
	}
	
	protected int getInt(final String property, final int defaultValue)
	{
		return this.getInt(property, () -> defaultValue);
	}
	
	protected int getInt(final String property, final IntSupplier defaultValueSupplier)
	{
		return Optional.ofNullable(System.getProperty(property))
			.map(s -> {
				try
				{
					return Integer.parseInt(s);
				}
				catch(final NumberFormatException nfe)
				{
					return null;
				}
			})
			.orElseGet(defaultValueSupplier::getAsInt);
	}
	
	protected long getLong(final String property, final long defaultValue)
	{
		return this.getLong(property, () -> defaultValue);
	}
	
	protected long getLong(final String property, final LongSupplier defaultValueSupplier)
	{
		return Optional.ofNullable(System.getProperty(property))
			.map(s -> {
				try
				{
					return Long.parseLong(s);
				}
				catch(final NumberFormatException nfe)
				{
					return null;
				}
			})
			.orElseGet(defaultValueSupplier::getAsLong);
	}
}
