package software.xdev.tci.imagebuild.handler;

import java.time.Duration;
import java.util.function.UnaryOperator;

import software.xdev.tci.imagebuild.config.BuildImageHandlerConfig;
import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;


public class AdvancedBuildImageHandler extends AbstractBuildImageHandler<AdvancedImageFromDockerFile>
{
	@Override
	protected String build(
		final String dockerImage,
		final String sanitizedDockerImageName,
		final BuildImageHandlerConfig config,
		final Duration timeout,
		final UnaryOperator<AdvancedImageFromDockerFile> configure)
	{
		this.logger.info("Starting build of image {}", dockerImage);
		
		final AdvancedImageFromDockerFile builder =
			new AdvancedImageFromDockerFile(dockerImage, config.deleteOnExit());
		
		this.configureAbstract(builder, config, sanitizedDockerImageName);
		
		return this.buildImage(configure.apply(builder), timeout);
	}
}
