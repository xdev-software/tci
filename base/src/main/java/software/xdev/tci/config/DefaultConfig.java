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
