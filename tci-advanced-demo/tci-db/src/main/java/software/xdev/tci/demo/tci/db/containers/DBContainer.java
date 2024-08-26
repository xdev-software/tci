package software.xdev.tci.demo.tci.db.containers;

import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;


public class DBContainer extends MariaDBContainer<DBContainer>
{
	public static final int PORT = 3306;
	
	public static final int MAJOR_VERSION = 10;
	public static final int MINOR_VERSION = 11;
	
	public DBContainer()
	{
		super(DockerImageName.parse("mariadb:" + MAJOR_VERSION + "." + MINOR_VERSION));
		
		// (31.03.2022 AB)NOTE: https://github.com/testcontainers/testcontainers-java/issues/914
		// Do not mount the volume!
		this.withConfigurationOverride(null);
		
		// DO NOT resolve client hostnames for more performance
		// https://mariadb.com/docs/server/ref/mdb/system-variables/skip_name_resolve/
		this.setCommand("--skip-name-resolve");
	}
}
