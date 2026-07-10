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
package software.xdev.tci.imagebuild.config;

import java.util.Optional;

import software.xdev.tci.config.DefaultConfig;


@SuppressWarnings({"java:S2789", "OptionalAssignedToNull", "OptionalUsedAsFieldOrParameterType"})
public class DefaultCreateImageHandlerConfig extends DefaultConfig implements CreateImageHandlerConfig
{
	protected Boolean deleteOnExit;
	protected String loggerForBuildPrefix;
	protected Optional<String> cacheFrom;
	protected Optional<String> cacheTo;
	
	@Override
	protected String propertyNamePrefix()
	{
		return "tci.image-build";
	}
	
	@Override
	public boolean deleteOnExit()
	{
		if(this.deleteOnExit == null)
		{
			this.deleteOnExit = this.resolveBool("delete-on-exit", false);
		}
		return this.deleteOnExit;
	}
	
	@Override
	public String loggerForBuildPrefix()
	{
		if(this.loggerForBuildPrefix == null)
		{
			this.loggerForBuildPrefix = this.resolve("logger-for-build-prefix")
				.orElse("container.build.");
		}
		return this.loggerForBuildPrefix;
	}
	
	@Override
	public Optional<String> cacheFrom()
	{
		if(this.cacheFrom == null)
		{
			this.cacheFrom = this.resolve("cache-from");
		}
		return this.cacheFrom;
	}
	
	@Override
	public Optional<String> cacheTo()
	{
		if(this.cacheTo == null)
		{
			this.cacheTo = this.resolve("cache-to");
		}
		return this.cacheTo;
	}
}
