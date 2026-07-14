package software.xdev.tci.imagebuild.handler;

import java.time.Duration;
import java.util.function.UnaryOperator;

import software.xdev.tci.imagebuild.config.BuildImageHandlerConfig;


public interface BuildImageHandler<I>
{
	String build(
		String dockerImage,
		BuildImageHandlerConfig parentConfig,
		Duration timeout,
		UnaryOperator<I> configure);
}
