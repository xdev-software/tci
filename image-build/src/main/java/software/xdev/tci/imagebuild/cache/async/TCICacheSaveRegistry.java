package software.xdev.tci.imagebuild.cache.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class TCICacheSaveRegistry
{
	private static TCICacheSaveRegistry instance;
	
	public static void setInstance(final TCICacheSaveRegistry instance)
	{
		TCICacheSaveRegistry.instance = instance;
	}
	
	public static TCICacheSaveRegistry instance()
	{
		if(instance == null)
		{
			createInstance();
		}
		return instance;
	}
	
	private static synchronized void createInstance()
	{
		if(instance == null)
		{
			instance = new TCICacheSaveRegistry();
		}
	}
	
	protected final List<CompletableFuture<?>> cfs = Collections.synchronizedList(new ArrayList<>());
	
	public void add(final CompletableFuture<?> cf)
	{
		this.cfs.add(cf);
	}
	
	public List<CompletableFuture<?>> get()
	{
		return this.cfs;
	}
	
	protected TCICacheSaveRegistry()
	{
	}
}
