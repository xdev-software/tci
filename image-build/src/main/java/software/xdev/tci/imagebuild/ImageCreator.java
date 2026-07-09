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
