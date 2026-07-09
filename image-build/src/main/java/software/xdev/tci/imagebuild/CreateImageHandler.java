package software.xdev.tci.imagebuild;

import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;
import software.xdev.testcontainers.imagebuilder.buildxnative.NativeAdvancedImageFromDockerfile;


public interface CreateImageHandler
{
	NativeAdvancedImageFromDockerfile nativeImage(String dockerImage);
	
	AdvancedImageFromDockerFile image(String dockerImage);
}
