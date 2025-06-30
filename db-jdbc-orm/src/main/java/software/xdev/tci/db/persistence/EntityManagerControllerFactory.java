package software.xdev.tci.db.persistence;

import static java.util.Map.entry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.cfg.JdbcSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

import software.xdev.tci.db.persistence.hibernate.DisableHibernateFormatMapper;


public class EntityManagerControllerFactory
{
	protected Supplier<Set<String>> entityClassesFinder;
	protected String driverFullClassName;
	protected String connectionProviderClassName;
	protected String persistenceUnitName = "Test";
	protected String jdbcUrl;
	protected String username;
	protected String password;
	protected Map<String, Object> additionalConfig;
	
	protected boolean disableHibernateFormatter = true;
	
	public EntityManagerControllerFactory()
	{
	}
	
	public EntityManagerControllerFactory(final Supplier<Set<String>> entityClassesFinder)
	{
		this.withEntityClassesFinder(entityClassesFinder);
	}
	
	public EntityManagerControllerFactory withEntityClassesFinder(final Supplier<Set<String>> entityClassesFinder)
	{
		this.entityClassesFinder = entityClassesFinder;
		return this;
	}
	
	public EntityManagerControllerFactory withDriverFullClassName(final String driverFullClassName)
	{
		this.driverFullClassName = driverFullClassName;
		return this;
	}
	
	public EntityManagerControllerFactory withConnectionProviderClassName(final String connectionProviderClassName)
	{
		this.connectionProviderClassName = connectionProviderClassName;
		return this;
	}
	
	public EntityManagerControllerFactory withPersistenceUnitName(final String persistenceUnitName)
	{
		this.persistenceUnitName = persistenceUnitName;
		return this;
	}
	
	public EntityManagerControllerFactory withJdbcUrl(final String jdbcUrl)
	{
		this.jdbcUrl = jdbcUrl;
		return this;
	}
	
	public EntityManagerControllerFactory withUsername(final String username)
	{
		this.username = username;
		return this;
	}
	
	public EntityManagerControllerFactory withPassword(final String password)
	{
		this.password = password;
		return this;
	}
	
	public EntityManagerControllerFactory withAdditionalConfig(final Map<String, Object> additionalConfig)
	{
		this.additionalConfig = additionalConfig;
		return this;
	}
	
	public EntityManagerControllerFactory withDisableHibernateFormatter(final boolean disableHibernateFormatter)
	{
		this.disableHibernateFormatter = disableHibernateFormatter;
		return this;
	}
	
	protected MutablePersistenceUnitInfo createBasicMutablePersistenceUnitInfo()
	{
		final MutablePersistenceUnitInfo persistenceUnitInfo = new MutablePersistenceUnitInfo()
		{
			@Override
			public void addTransformer(final ClassTransformer classTransformer)
			{
				// Do nothing
			}
			
			@Override
			public ClassLoader getNewTempClassLoader()
			{
				return null;
			}
		};
		persistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
		persistenceUnitInfo.setPersistenceUnitName(this.persistenceUnitName);
		persistenceUnitInfo.setPersistenceProviderClassName(HibernatePersistenceProvider.class.getName());
		return persistenceUnitInfo;
	}
	
	protected Collection<URL> jarFileUrlsToAdd() throws IOException
	{
		return Collections.list(EntityManagerController.class
			.getClassLoader()
			.getResources(""));
	}
	
	protected Map<String, Object> defaultPropertiesMap()
	{
		return new HashMap<>(Map.ofEntries(
			entry(JdbcSettings.JAKARTA_JDBC_DRIVER, this.driverFullClassName),
			entry(JdbcSettings.JAKARTA_JDBC_URL, this.jdbcUrl),
			entry(JdbcSettings.JAKARTA_JDBC_USER, this.username),
			entry(JdbcSettings.JAKARTA_JDBC_PASSWORD, this.password)
		));
	}
	
	public EntityManagerController build()
	{
		final MutablePersistenceUnitInfo persistenceUnitInfo = this.createBasicMutablePersistenceUnitInfo();
		// TODO
		// if(cachedEntityClassNames == null)
		// {
		// 	cachedEntityClassNames = AnnotatedClassFinder.find(DefaultJPAConfig.ENTITY_PACKAGE, Entity.class)
		// 		.stream()
		// 		.map(Class::getName)
		// 		.collect(Collectors.toSet());
		// }
		if(this.entityClassesFinder != null)
		{
			persistenceUnitInfo.getManagedClassNames().addAll(this.entityClassesFinder.get());
		}
		try
		{
			this.jarFileUrlsToAdd().forEach(persistenceUnitInfo::addJarFileUrl);
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException(ioe);
		}
		
		final Map<String, Object> properties = this.defaultPropertiesMap();
		Optional.ofNullable(this.connectionProviderClassName)
			.ifPresent(p -> properties.put(JdbcSettings.CONNECTION_PROVIDER, this.connectionProviderClassName));
		if(this.disableHibernateFormatter)
		{
			properties.putAll(DisableHibernateFormatMapper.properties());
		}
		properties.putAll(this.additionalConfig);
		return new EntityManagerController(
			new HibernatePersistenceProvider().createContainerEntityManagerFactory(
				persistenceUnitInfo,
				properties));
	}
}
