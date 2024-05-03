package software.xdev.tci.demo.tci.webapp.containers;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;


@SuppressWarnings("java:S2160")
public class WebAppContainer extends GenericContainer<WebAppContainer>
{
	public static final int DEFAULT_HTTP_PORT = 8080;
	
	protected final boolean connectionlessStart;
	
	public WebAppContainer(final String dockerImageName, final boolean connectionlessStart)
	{
		super(dockerImageName);
		this.connectionlessStart = connectionlessStart;
		if(connectionlessStart)
		{
			this.withConnectionlessStart();
		}
		this.addExposedPort(DEFAULT_HTTP_PORT);
	}
	
	public WebAppContainer withDB(final String jdbcUrl, final String username, final String password)
	{
		return this.withEnv("SPRING_DATASOURCE_URL", jdbcUrl)
			.withEnv("SPRING_DATASOURCE_USERNAME", username)
			.withEnv("SPRING_DATASOURCE_PASSWORD", password);
	}
	
	public WebAppContainer withAuth(
		final String oidcClientId,
		final String oidcClientSecret,
		final String oidcServerUrl)
	{
		final String registration = "SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_LOCAL_";
		final String provider = "SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_LOCAL_";
		if(this.connectionlessStart)
		{
			this.addEnv(registration + "AUTHORIZATION-GRANT-TYPE", "authorization_code");
			this.addEnv(registration + "REDIRECT-URI", "{baseUrl}/{action}/oauth2/code/{registrationId}");
			this.addEnv(provider + "AUTHORIZATION-URI", oidcServerUrl + "/connect/authorize");
			this.addEnv(provider + "TOKEN-URI", oidcServerUrl + "/connect/token");
			this.addEnv(provider + "JWK-SET-URI", oidcServerUrl + "/.well-known/openid-configuration/jwks");
			this.addEnv(provider + "USER-INFO-URI", oidcServerUrl + "/connect/userinfo");
			this.addEnv(provider + "USER-INFO-AUTHENTICATION-METHOD", "header");
			this.addEnv(provider + "USER-NAME-ATTRIBUTE", "sub");
		}
		else
		{
			this.addEnv(provider + "ISSUER-URI", oidcServerUrl);
		}
		
		return this.withEnv(registration + "CLIENT-NAME", "Local")
			.withEnv(registration + "CLIENT-ID", oidcClientId)
			.withEnv(registration + "CLIENT-SECRET", oidcClientSecret)
			// offline_access is required to get the refresh token in the initial response!
			.withEnv(registration + "SCOPE", "openid,profile,email,offline_access");
	}
	
	public WebAppContainer withActuator(final String username, final String hash)
	{
		return this.withEnv("DEMO_ACTUATOR_USERNAME", username)
			.withEnv("DEMO_ACTUATOR_PASSWORD-HASH", hash);
	}
	
	public WebAppContainer withDisableHTTPS()
	{
		return this.withEnv("SERVER_SERVLET_SESSION_COOKIE_SECURE", false);
	}
	
	/**
	 * Starts the application without establishing any connections.<br/> Connection/Initialization is done on demand.
	 */
	public WebAppContainer withConnectionlessStart()
	{
		final String springJpa = "SPRING_JPA_";
		return this.withDisableFlyway()
			// Disable DB for actuator endpoint or complete endpoint will fail otherwise
			.withEnv("MANAGEMENT_HEALTH_DB_ENABLED", false)
			.withEnv(
				springJpa + "DATABASE-PLATFORM",
				"software.xdev.tci.demo.persistence.config.dialect.DefaultDialect")
			.withEnv(springJpa + "PROPERTIES_HIBERNATE_TEMP_USE_JDBC_METADATA_DEFAULTS", false);
	}
	
	public WebAppContainer withDisableFlyway()
	{
		return this.withEnv("SPRING_FLYWAY_ENABLED", false);
	}
	
	public WebAppContainer withEnv(final String key, final boolean value)
	{
		return this.withEnv(key, String.valueOf(value));
	}
	
	public WebAppContainer withDefaultWaitStrategy(
		final Duration startUpTimeout,
		final String actuatorUsername,
		final String actuatorPassword)
	{
		return this.waitingFor(new WaitAllStrategy()
			.withStartupTimeout(startUpTimeout)
			.withStrategy(
				new HttpWaitStrategy()
					.forPort(WebAppContainer.DEFAULT_HTTP_PORT)
					.forPath("/robots.txt")
					.forStatusCode(HttpStatus.SC_OK)
					.withReadTimeout(Duration.ofSeconds(10))
			)
			.withStrategy(
				new HttpWaitStrategy()
					.forPort(WebAppContainer.DEFAULT_HTTP_PORT)
					.forPath("/actuator/health")
					.withBasicCredentials(actuatorUsername, actuatorPassword)
					.forStatusCode(HttpStatus.SC_OK)
					.withReadTimeout(Duration.ofSeconds(10))
			));
	}
	
	@SuppressWarnings("unused")
	private void remoteDebug(final int port)
	{
		this.addEnv("JAVA_OPTS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:" + port);
		this.addFixedExposedPort(port, port);
	}
}
