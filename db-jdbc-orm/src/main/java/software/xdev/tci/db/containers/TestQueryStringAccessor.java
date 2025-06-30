package software.xdev.tci.db.containers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.testcontainers.containers.JdbcDatabaseContainer;


public final class TestQueryStringAccessor
{
	public static String testQueryString(final JdbcDatabaseContainer<?> container)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		final Method mGetTestQueryString = container.getClass().getDeclaredMethod("getTestQueryString");
		mGetTestQueryString.setAccessible(true);
		return (String)mGetTestQueryString.invoke(container);
	}
	
	private TestQueryStringAccessor()
	{
	}
}
