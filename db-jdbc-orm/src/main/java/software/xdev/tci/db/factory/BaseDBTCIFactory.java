package software.xdev.tci.db.factory;

import java.sql.Connection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.JdbcDatabaseContainer;

import software.xdev.tci.db.BaseDBTCI;
import software.xdev.tci.db.containers.TestQueryStringAccessor;
import software.xdev.tci.envperf.EnvironmentPerformance;
import software.xdev.tci.factory.prestart.PreStartableTCIFactory;
import software.xdev.tci.factory.prestart.config.PreStartConfig;


public abstract class BaseDBTCIFactory<C extends JdbcDatabaseContainer<C>, I extends BaseDBTCI<C>>
	extends PreStartableTCIFactory<C, I>
{
	public BaseDBTCIFactory(
		final BiFunction<C, String, I> infraBuilder,
		final Supplier<C> containerBuilder)
	{
		super(infraBuilder, containerBuilder, "db", "container.db", "DB");
	}
	
	public BaseDBTCIFactory(
		final BiFunction<C, String, I> infraBuilder,
		final Supplier<C> containerBuilder,
		final PreStartConfig config,
		final Timeouts timeouts)
	{
		super(infraBuilder, containerBuilder, "db", "container.db", "DB", config, timeouts);
	}
	
	public BaseDBTCIFactory(
		final BiFunction<C, String, I> infraBuilder,
		final Supplier<C> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName,
		final String name)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName, name);
	}
	
	public BaseDBTCIFactory(
		final BiFunction<C, String, I> infraBuilder,
		final Supplier<C> containerBuilder,
		final String containerBaseName,
		final String containerLoggerName,
		final String name,
		final PreStartConfig config,
		final Timeouts timeouts)
	{
		super(infraBuilder, containerBuilder, containerBaseName, containerLoggerName, name, config, timeouts);
	}
	
	@Override
	protected void postProcessNew(final I infra)
	{
		// Docker needs a few milliseconds (usually less than 100) to reconfigure its networks
		// In the meantime existing connections might fail if we proceed immediately
		// So let's wait a moment here until everything is working
		Unreliables.retryUntilSuccess(
			10 + EnvironmentPerformance.cpuSlownessFactor() * 2,
			TimeUnit.SECONDS,
			() -> {
				try(final Connection con = infra.createDataSource().getConnection())
				{
					con.isValid(10);
				}
				
				if(infra.isMigrateAndInitializeEMC())
				{
					// Check EMC
					infra.useNewEntityManager(em -> em
						.createNativeQuery(Objects.requireNonNullElse(
							this.getTestQueryStringForEntityManager(infra),
							"SELECT 1"))
						.getResultList());
				}
				return null;
			});
	}
	
	protected String getTestQueryStringForEntityManager(final I infra)
	{
		try
		{
			return TestQueryStringAccessor.testQueryString(infra.getContainer());
		}
		catch(final Exception ex)
		{
			this.log().warn("Failed to get test query string", ex);
			return null;
		}
	}
}
