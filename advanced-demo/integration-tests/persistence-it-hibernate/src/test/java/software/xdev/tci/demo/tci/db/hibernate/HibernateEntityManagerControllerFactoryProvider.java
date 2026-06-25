package software.xdev.tci.demo.tci.db.hibernate;

import java.util.Set;
import java.util.function.Supplier;

import software.xdev.tci.db.persistence.EntityManagerControllerFactory;
import software.xdev.tci.db.persistence.hibernate.HibernateEntityManagerControllerFactory;
import software.xdev.tci.demo.tci.db.EntityManagerControllerFactoryProvider;


public class HibernateEntityManagerControllerFactoryProvider
	implements EntityManagerControllerFactoryProvider
{
	@Override
	public EntityManagerControllerFactory<?, ?> create(final Supplier<Set<String>> entityClassesFinder)
	{
		return new HibernateEntityManagerControllerFactory(entityClassesFinder);
	}
}
