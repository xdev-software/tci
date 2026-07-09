package software.xdev.tci.imagebuild.config;

import java.util.Optional;

import software.xdev.tci.serviceloading.TCIServiceLoaderHolder;


public interface CreateImageHandlerConfig
{
	boolean deleteOnExit();
	
	String loggerForBuildPrefix();
	
	Optional<String> cacheFrom();
	
	Optional<String> cacheTo();
	
	static CreateImageHandlerConfig instance()
	{
		return TCIServiceLoaderHolder.instance().service(CreateImageHandlerConfig.class);
	}
}
