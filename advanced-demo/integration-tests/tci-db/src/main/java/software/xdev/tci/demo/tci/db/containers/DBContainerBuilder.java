package software.xdev.tci.demo.tci.db.containers;

import java.nio.file.Paths;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;


@SuppressWarnings("PMD.MoreThanOneLogger")
public final class DBContainerBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(DBContainerBuilder.class);
	private static final Logger LOG_CONTAINER_BUILD =
		LoggerFactory.getLogger("container.build.db");
	
	
	private DBContainerBuilder()
	{
	}
	
	public static synchronized String getImageName()
	{
		LOG.info("Building Webapp-db-DockerImage...");
		
		final AdvancedImageFromDockerFile builder =
			new AdvancedImageFromDockerFile("webapp-db", false)
				.withLoggerForBuild(LOG_CONTAINER_BUILD)
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
