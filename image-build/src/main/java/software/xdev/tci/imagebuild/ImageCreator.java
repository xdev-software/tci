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
package software.xdev.tci.imagebuild;

import software.xdev.tci.serviceloading.TCIServiceLoaderHolder;
import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;
import software.xdev.testcontainers.imagebuilder.buildxnative.NativeAdvancedImageFromDockerfile;


public final class ImageCreator
{
	public static NativeAdvancedImageFromDockerfile nativeImage(final String dockerImage)
	{
		return impl().nativeImage(dockerImage);
	}
	
	public static AdvancedImageFromDockerFile image(final String dockerImage)
	{
		return impl().image(dockerImage);
	}
	
	public static CreateImageHandler impl()
	{
		return TCIServiceLoaderHolder.instance().service(CreateImageHandler.class);
	}
	
	private ImageCreator()
	{
	}
}
