package software.xdev.tci.demo.tci.webapp.containers;

import java.nio.file.Paths;
import java.time.Duration;

import software.xdev.tci.imagebuild.BuildImage;
import software.xdev.tci.imagebuild.integrations.maven.POMSimpleModuleFilter;
import software.xdev.tci.jacoco.testbase.config.JaCoCoConfig;


public final class WebAppContainerBuilder
{
	private WebAppContainerBuilder()
	{
	}
	
	public static String getImageName()
	{
		return BuildImage.nativeImage(
			"tci-demo-webapp",
			Duration.ofMinutes(5),
			builder -> {
				builder
					// NOTE: AOT can't be used properly when JaCoCo is active
					.withBuildArg("ENABLE_AOT", "1")
					.withDockerFilePath(Paths.get(
						"../../../../demos/advanced-demo/integration-tests/tci-webapp/Dockerfile"))
					.withBaseDir(Paths.get("../../../../"))
					.configureFilesToTransferHandler(h -> h
						.withPostGitIgnoreLines(
							// Ignore git-folder, as it will be provided in the Dockerfile
							".git/**",
							// Ignore other unused folders and extensions
							".github/**",
							".config/**",
							".idea/**",
							".run/**",
							"*.iml",
							"*.cmd",
							"*.md",
							"assets/**",
							"demos/advanced-demo/_dev_infra/**",
							// Ignore other Dockerfiles (our required file will always be transferred)
							"Dockerfile",
							// Ignore not required test-modules that may have changed
							// sources only - otherwise the parent pom doesn't find the resources
							"demos/advanced-demo/integration-tests/**",
							"**/src/test/**",
							// Ignore not needed modules
							"/src/**",
							"/demos/base-demo/**",
							// Ignore resources that are just used for development
							"demos/advanced-demo/webapp/src/main/resources-dev/**",
							// Most files from these folders need to be ignored -> Down there for highest prio
							"node_modules",
							"target")
						// File is in root directory - we can't access it
						.withTransferArchiveTARCompressorCustomizer(c -> c
							// Rewrite parent pom to exclude integration tests
							// This way changes in test pom's cause no redownload of dependencies
							.withContentModifier(POMSimpleModuleFilter.keep("", "demos"))
							.withContentModifier(POMSimpleModuleFilter.keep("demos", "advanced-demo"))
							.withContentModifier(POMSimpleModuleFilter.remove(
								"demos/advanced-demo", "integration-tests"))));
				
				if(JaCoCoConfig.instance().enabled())
				{
					builder.withBuildArg("JACOCO_AGENT_ENABLED", "1");
				}
				
				return builder;
			});
	}
}
