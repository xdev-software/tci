package software.xdev.tci.envperf.impl;

public interface TCIEnvironmentPerformance
{
	/**
	 * Describes the performance of the underlying environment.<br/> Higher values indicate a slower environment.<br/>
	 * Guideline is a follows:
	 * <ul>
	 *     <li>1 equals a standard developer machine CPU with roughly <code>16T 3GHz</code> or better</li>
	 *     <li>for a Raspberry PI 5 with <code>4T 2.4GHz</code> a value of roughly 3 should be chosen</li>
	 * </ul>
	 * The default value is 1.<br/>Min=1, Max=10
	 */
	int cpuSlownessFactor();
}
