package software.xdev.tci.demo.tci.db.containers;

import java.nio.file.Paths;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.tci.imagebuild.ImageCreator;
import software.xdev.testcontainers.imagebuilder.buildxnative.NativeAdvancedImageFromDockerfile;


public final class DBContainerBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(DBContainerBuilder.class);
	
	private DBContainerBuilder()
	{
	}
	
	public static String getImageName()
	{
		LOG.info("Building Webapp-db-DockerImage...");
		
		final NativeAdvancedImageFromDockerfile builder = ImageCreator.nativeImage("tci-demo-db")
			.withDockerFilePath(Paths.get("../tci-db/Dockerfile"))
			.withBaseDir(Paths.get("../"))
			.configureFilesToTransferHandler(h -> h
				.withPostGitIgnoreLines(
					// Ignore everything
					"**")
				.withBaseDirRelativeIgnoreFile(null)
			);
		
		final String name = builder.build(Duration.ofMinutes(5));
		
		LOG.info("Built Image; Name ='{}'", name);
		
		return name;
	}
}
