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
package software.xdev.tci.leakdetection.config;

import java.time.Duration;

import software.xdev.tci.envperf.EnvironmentPerformance;


public interface LeakDetectionConfig
{
	boolean DEFAULT_ENABLED = true;
	Duration DEFAULT_STOP_TIMEOUT = Duration.ofSeconds(15L + EnvironmentPerformance.cpuSlownessFactor() * 5L);
	
	default boolean enabled()
	{
		return DEFAULT_ENABLED;
	}
	
	default Duration defaultStopTimeout()
	{
		return DEFAULT_STOP_TIMEOUT;
	}
}
