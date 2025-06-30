package software.xdev.tci.envperf;

import software.xdev.tci.envperf.impl.TCIEnvironmentPerformance;
import software.xdev.tci.serviceloading.TCIServiceLoader;


/**
 * Describes the Performance of the Environment where TCI is running.
 */
public final class EnvironmentPerformance
{
	/**
	 * @see TCIEnvironmentPerformance#cpuSlownessFactor()
	 */
	public static int cpuSlownessFactor()
	{
		return impl().cpuSlownessFactor();
	}
	
	public static TCIEnvironmentPerformance impl()
	{
		return TCIServiceLoader.instance().service(TCIEnvironmentPerformance.class);
	}
	
	private EnvironmentPerformance()
	{
	}
}
