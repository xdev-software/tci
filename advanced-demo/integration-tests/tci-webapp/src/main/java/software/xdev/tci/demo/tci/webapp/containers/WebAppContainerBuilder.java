package software.xdev.tci.demo.tci.webapp.containers;

import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.xdev.testcontainers.imagebuilder.AdvancedImageFromDockerFile;
import software.xdev.testcontainers.imagebuilder.compat.DockerfileCOPYParentsEmulator;


@SuppressWarnings("PMD.MoreThanOneLogger")
public final class WebAppContainerBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(WebAppContainerBuilder.class);
	private static final Logger LOG_CONTAINER_BUILD =
		LoggerFactory.getLogger("container.build.webapp");
	
	private static String builtImageName;
	
	private WebAppContainerBuilder()
	{
	}
	
	public static synchronized String getBuiltImageName()
	{
		if(builtImageName != null)
		{
			return builtImageName;
		}
		
		LOG.info("Building WebApp-DockerImage...");
		
		final AdvancedImageFromDockerFile builder =
			new AdvancedImageFromDockerFile("webapp-it-local", false)
				.withLoggerForBuild(LOG_CONTAINER_BUILD)
				.withPostGitIgnoreLines(
					// Ignore git-folder, as it will be provided in the Dockerfile
					".git/**",
					// Ignore other unused folders and extensions
					"*.iml",
					"*.cmd",
					"*.md",
					"_dev_infra/**",
					"_resource_metrics/**",
					// Ignore other Dockerfiles (our required file will always be transferred)
					"Dockerfile",
					// Ignore not required test-modules that may have changed
					// sources only - otherwise the parent pom doesn't find the resources
					"integration-tests/*/src/**",
					"**/src/test/**",
					// Ignore resources that are just used for development
					"webapp/src/main/resources-dev/**",
					// Most files from these folders need to be ignored -> Down there for highest prio
					"node_modules",
					"target")
				.withDockerFilePath(Paths.get("../../integration-tests/tci-webapp/Dockerfile"))
				.withBaseDir(Paths.get("../../"))
				// File is in root directory - we can't access it
				.withBaseDirRelativeIgnoreFile(null)
				.withDockerFileLinesModifier(new DockerfileCOPYParentsEmulator());
		
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
