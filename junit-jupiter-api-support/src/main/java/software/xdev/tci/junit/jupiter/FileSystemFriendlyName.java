package software.xdev.tci.junit.jupiter;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.ExtensionContext;


public class FileSystemFriendlyName implements Supplier<String>
{
	protected static final Pattern FILE_NAME_ALLOWED_CHARS = Pattern.compile("[^A-Za-z0-9#_-]");
	
	protected final ExtensionContext context;
	
	// Used for caching
	protected String name;
	
	public FileSystemFriendlyName(final ExtensionContext context)
	{
		this.context = context;
	}
	
	@Override
	public String get()
	{
		if(this.name == null)
		{
			this.createThreadSafeIfRequired();
		}
		return this.name;
	}
	
	protected synchronized void createThreadSafeIfRequired()
	{
		if(this.name == null)
		{
			this.name = this.create();
		}
	}
	
	protected String create()
	{
		final String testClassName =
			this.cleanForFilename(this.context.getRequiredTestClass().getSimpleName());
		final String displayName = this.cleanForFilename(this.context.getDisplayName());
		return System.currentTimeMillis()
			+ "_"
			+ testClassName
			+ "_"
			// Cut off otherwise file name is too long
			+ displayName.substring(0, Math.min(displayName.length(), 200));
	}
	
	protected String cleanForFilename(final String str)
	{
		return FILE_NAME_ALLOWED_CHARS.matcher(str.replace(' ', '_'))
			.replaceAll("")
			.toLowerCase();
	}
}
