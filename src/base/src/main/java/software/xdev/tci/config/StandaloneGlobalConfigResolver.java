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


/**
 * Utility class meant for small classes where it's not worth to use a full {@link DefaultConfig}
 */
public class StandaloneGlobalConfigResolver extends DefaultConfig
{
	private static StandaloneGlobalConfigResolver instance = new StandaloneGlobalConfigResolver();
	
	// Can be used for "emergency" overriding the default implementation
	protected static void setInstance(final StandaloneGlobalConfigResolver instance)
	{
		StandaloneGlobalConfigResolver.instance = instance;
	}
	
	public static StandaloneGlobalConfigResolver instance()
	{
		return instance;
	}
	
	@Override
	public Optional<Integer> resolveInt(final String propertyName)
	{
		return super.resolveInt(propertyName);
	}
	
	@Override
	protected String propertyNamePrefix()
	{
		return "tci";
	}
}
