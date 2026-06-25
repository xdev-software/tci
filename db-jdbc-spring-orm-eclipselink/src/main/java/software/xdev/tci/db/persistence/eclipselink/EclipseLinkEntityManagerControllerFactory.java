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
package software.xdev.tci.db.persistence.eclipselink;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.orm.jpa.persistenceunit.SpringPersistenceUnitInfo;

import software.xdev.tci.db.persistence.SpringEntityManagerControllerFactory;


public class EclipseLinkEntityManagerControllerFactory
	extends SpringEntityManagerControllerFactory<EclipseLinkEntityManagerControllerFactory>
{
	protected ClassLoader persistenceUnitInfoCl = this.getClass().getClassLoader();
	
	public EclipseLinkEntityManagerControllerFactory()
	{
	}
	
	public EclipseLinkEntityManagerControllerFactory(final Supplier<Set<String>> entityClassesFinder)
	{
		super(entityClassesFinder);
	}
	
	public EclipseLinkEntityManagerControllerFactory setPersistenceUnitInfoCl(final ClassLoader persistenceUnitInfoCl)
	{
		this.persistenceUnitInfoCl = persistenceUnitInfoCl;
		return this;
	}
	
	@Override
	protected SpringPersistenceUnitInfo instantiateSpringPersistenceUnitInfo()
	{
		return new SpringPersistenceUnitInfo(this.persistenceUnitInfoCl);
	}
	
	@Override
	protected SpringPersistenceUnitInfo createSpringPersistenceUnitInfo()
	{
		final SpringPersistenceUnitInfo spui = super.createSpringPersistenceUnitInfo();
		// Required otherwise createContainerEntityManagerFactoryImpl crashes
		spui.setPersistenceUnitRootUrl(this.getClass().getResource(""));
		spui.setPersistenceProviderClassName(PersistenceProvider.class.getName());
		return spui;
	}
	
	@Override
	protected EntityManagerFactory createEntityManagerFactory(
		final PersistenceUnitInfo pui,
		final Map<String, Object> properties)
	{
		return new PersistenceProvider().createContainerEntityManagerFactory(pui, properties);
	}
}
