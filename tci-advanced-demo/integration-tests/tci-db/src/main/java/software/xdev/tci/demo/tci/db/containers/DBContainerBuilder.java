package software.xdev.tci.demo.tci.db.containers;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;


@SuppressWarnings("PMD.MoreThanOneLogger")
public final class DBContainerBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(DBContainerBuilder.class);
	private static final Logger LOG_CONTAINER_BUILD =
		LoggerFactory.getLogger("container.build.db");
	
	private static String builtImageName;
	
	private DBContainerBuilder()
	{
	}
	
	public static synchronized String getBuiltImageName()
	{
		if(builtImageName != null)
		{
			return builtImageName;
		}
		
		LOG.info("Building Webapp-db-DockerImage...");
		
		final AdvancedImageFromDockerFile builder =
			new AdvancedImageFromDockerFile("webapp-db", false)
				.withLoggerForBuild(LOG_CONTAINER_BUILD)
				.withPostGitIgnoreLines(
					// Ignore everything
					"**")
				.withDockerFilePath(Paths.get("../tci-db/Dockerfile"))
				.withBaseDir(Paths.get("../"))
				.withBaseDirRelativeIgnoreFile(null);
		
		try
		{
			builtImageName = builder.get(5, TimeUnit.MINUTES);
		}
		catch(final TimeoutException tex)
		{
			throw new IllegalStateException("Timed out", tex);
		}
		
		LOG.info("Built Image; Name ='{}'", builtImageName);
		
		return builtImageName;
	}
}
