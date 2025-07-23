/*
 * Copyright © 2025 XDEV Software (https://xdev.software)
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
package software.xdev.tci.db.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitTransactionType;

import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;


@SuppressWarnings("java:S119")
public abstract class SpringEntityManagerControllerFactory<SELF extends SpringEntityManagerControllerFactory<SELF>>
	extends EntityManagerControllerFactory<SELF, MutablePersistenceUnitInfo>
{
	public SpringEntityManagerControllerFactory()
	{
	}
	
	public SpringEntityManagerControllerFactory(final Supplier<Set<String>> entityClassesFinder)
	{
		super(entityClassesFinder);
	}
	
	@Override
	protected MutablePersistenceUnitInfo createPersistenceUnitInfo()
	{
		final MutablePersistenceUnitInfo pui = new MutablePersistenceUnitInfo()
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
		pui.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
		pui.setPersistenceUnitName(this.persistenceUnitName);
		
		this.jarFileUrlsToAdd().forEach(pui::addJarFileUrl);
		
		return pui;
	}
	
	protected Collection<URL> jarFileUrlsToAdd()
	{
		try
		{
			return Collections.list(EntityManagerController.class
				.getClassLoader()
				.getResources(""));
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException(ioe);
		}
	}
}
