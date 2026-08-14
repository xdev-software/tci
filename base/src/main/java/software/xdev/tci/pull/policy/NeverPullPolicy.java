package software.xdev.tci.pull.policy;

import org.testcontainers.images.ImagePullPolicy;
import org.testcontainers.utility.DockerImageName;


/**
 * Never pulls the image.
 */
public class NeverPullPolicy implements ImagePullPolicy
{
	public static final NeverPullPolicy INSTANCE = new NeverPullPolicy();
	
	protected NeverPullPolicy()
	{
	}
	
	@Override
	public boolean shouldPull(final DockerImageName imageName)
	{
		return false;
	}
}
