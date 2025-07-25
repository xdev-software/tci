/*
 * Copyright © 2025 XDEV Software (https://xdev.software)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package software.xdev.tci.oidc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.rnorth.ducttape.unreliables.Unreliables;

import software.xdev.tci.TCI;
import software.xdev.tci.envperf.EnvironmentPerformance;
import software.xdev.tci.misc.http.HttpClientCloser;
import software.xdev.tci.oidc.containers.BaseOIDCServerContainer;
import software.xdev.tci.oidc.containers.OIDCServerContainer;


@SuppressWarnings("java:S119")
public abstract class BaseOIDCTCI<
	SELF extends BaseOIDCTCI<SELF, C>,
	C extends BaseOIDCServerContainer<C>>
	extends TCI<C>
{
	protected static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
	
	public static final String CLIENT_ID = OIDCServerContainer.DEFAULT_CLIENT_ID;
	public static final String CLIENT_SECRET = OIDCServerContainer.DEFAULT_CLIENT_SECRET;
	
	public static final String DEFAULT_DOMAIN = "example.local";
	
	public static final String DEFAULT_USER_EMAIL = "test@" + DEFAULT_DOMAIN;
	public static final String DEFAULT_USER_NAME = "Testuser";
	public static final String DEFAULT_USER_PASSWORD = "pwd";
	
	protected boolean shouldAddDefaultUser = true;
	protected String defaultUserEmail = DEFAULT_USER_EMAIL;
	protected String defaultUserName = DEFAULT_USER_NAME;
	protected String defaultUserPassword = DEFAULT_USER_PASSWORD;
	
	protected BaseOIDCTCI(final C container, final String networkAlias)
	{
		super(container, networkAlias);
	}
	
	@Override
	public void start(final String containerName)
	{
		super.start(containerName);
		if(this.shouldAddDefaultUser)
		{
			this.addUser(this.getDefaultUserEmail(), this.getDefaultUserName(), this.getDefaultUserPassword());
		}
		
		// Warm up; Otherwise slow initial response may cause a timeout during tests
		this.warmUpWellKnownJWKsEndpoint();
	}
	
	public String getDefaultUserEmail()
	{
		return this.defaultUserEmail;
	}
	
	public String getDefaultUserName()
	{
		return this.defaultUserName;
	}
	
	public String getDefaultUserPassword()
	{
		return this.defaultUserPassword;
	}
	
	public static String getInternalHttpBaseEndPoint(final String networkAlias)
	{
		return "http://" + networkAlias + ":" + OIDCServerContainer.PORT;
	}
	
	public String getInternalHttpBaseEndPoint()
	{
		return getInternalHttpBaseEndPoint(this.getNetworkAlias());
	}
	
	public String getExternalHttpBaseEndPoint()
	{
		return this.getContainer().getExternalHttpBaseEndPoint();
	}
	
	@SuppressWarnings("PMD.UseTryWithResources") // Java 17 support
	public void warmUpWellKnownJWKsEndpoint()
	{
		final int slownessFactor = EnvironmentPerformance.cpuSlownessFactor();
		final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(1L + slownessFactor))
			.build();
		
		try
		{
			Unreliables.retryUntilSuccess(
				5 + slownessFactor,
				() -> httpClient.send(
					HttpRequest.newBuilder(URI.create(
							this.getExternalHttpBaseEndPoint() + "/.well-known/openid-configuration/jwks"))
						.timeout(Duration.ofSeconds(10L + slownessFactor * 5L))
						.GET()
						.build(),
					HttpResponse.BodyHandlers.discarding()));
		}
		finally
		{
			HttpClientCloser.close(httpClient);
		}
	}
	
	public void addUser(
		final String email,
		final String name,
		final String pw)
	{
		this.apiAddUser(this.createDefaultBodyForAddUser(email, name, pw));
	}
	
	protected void apiAddUser(final String jsonBody)
	{
		try(final CloseableHttpClient client = this.createDefaultHttpClient())
		{
			final HttpPost post = new HttpPost(this.getContainer().getExternalHttpBaseEndPoint() + "/api/v1/user");
			post.setEntity(new StringEntity(jsonBody));
			post.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
			post.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
			
			final ClassicHttpResponse response = client.execute(post, r -> r);
			if(response.getCode() != HttpStatus.SC_OK)
			{
				throw new IllegalStateException("Unable to create user; Expected statuscode 200 but got "
					+ response.getCode()
					+ "; Reason: " + response.getReasonPhrase());
			}
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException(ioe);
		}
	}
	
	protected String createDefaultBodyForAddUser(
		final String email,
		final String name,
		final String pw)
	{
		return """
			{
			  "SubjectId":"%s",
			  "Username":"%s",
			  "Password":"%s",
			  "Claims": [
			    {
			      "Type": "name",
			      "Value": "%s",
			      "ValueType": "string"
			    },
			    {
			      "Type": "email",
			      "Value": "%s",
			      "ValueType": "string"
			    }
			  ]
			}
			""".formatted(
			UUID.randomUUID().toString(),
			email,
			pw,
			name,
			email
		);
	}
	
	protected CloseableHttpClient createDefaultHttpClient()
	{
		return HttpClientBuilder.create()
			.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
				.setDefaultConnectionConfig(ConnectionConfig.custom()
					.setConnectTimeout(Timeout.of(DEFAULT_TIMEOUT))
					.setSocketTimeout(Timeout.of(DEFAULT_TIMEOUT))
					.build())
				.build())
			.build();
	}
	
	// region Configure
	
	protected SELF withShouldAddDefaultUser(final boolean shouldAddDefaultUser)
	{
		this.shouldAddDefaultUser = shouldAddDefaultUser;
		return this.self();
	}
	
	public SELF withDefaultUserEmail(final String defaultUserEmail)
	{
		this.defaultUserEmail = defaultUserEmail;
		return this.self();
	}
	
	public SELF withDefaultUserName(final String defaultUserName)
	{
		this.defaultUserName = defaultUserName;
		return this.self();
	}
	
	public SELF withDefaultUserPassword(final String defaultUserPassword)
	{
		this.defaultUserPassword = defaultUserPassword;
		return this.self();
	}
	
	// endregion
	
	@SuppressWarnings("unchecked")
	public SELF self()
	{
		return (SELF)this;
	}
}
