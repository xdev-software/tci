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
package software.xdev.tci.oidc.containers;

import java.util.List;
import java.util.stream.Collectors;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;


@SuppressWarnings("java:S2160")
public class OIDCServerContainer extends GenericContainer<OIDCServerContainer>
{
	public static final int PORT = 8080;
	
	public static final String DEFAULT_CLIENT_ID = "client-id1";
	public static final String DEFAULT_CLIENT_SECRET = "client-secret1";
	
	protected String clientId = DEFAULT_CLIENT_ID;
	protected String clientSecret = DEFAULT_CLIENT_SECRET;
	protected List<String> additionalAllowedScopes;
	
	public OIDCServerContainer()
	{
		super(DockerImageName.parse("xdevsoftware/oidc-server-mock:1"));
		this.addExposedPort(PORT);
	}
	
	public OIDCServerContainer withDefaultEnvConfig()
	{
		this.addEnv("ASPNETCORE_ENVIRONMENT", "Development");
		this.addEnv("ASPNET_SERVICES_OPTIONS_INLINE", this.buildAspnetServicesOptionsInline());
		this.addEnv("SERVER_OPTIONS_INLINE", this.buildServerOptionsInline());
		this.addEnv("LOGIN_OPTIONS_INLINE", this.buildLoginOptionsInline());
		this.addEnv("LOGOUT_OPTIONS_INLINE", this.buildLogoutOptionsInline());
		this.addEnv("CLIENTS_CONFIGURATION_INLINE", this.buildClientsConfigurationInline());
		return this.self();
	}
	
	protected String buildAspnetServicesOptionsInline()
	{
		return """
			{
			  "ForwardedHeadersOptions": {
			    "ForwardedHeaders" : "All"
			  }
			}
			""";
	}
	
	protected String buildServerOptionsInline()
	{
		return """
			{
			  "AccessTokenJwtType": "JWT",
			  "Discovery": {
			    "ShowKeySet": true
			  },
			  "Authentication": {
			    "CookieSameSiteMode": "Lax",
			    "CheckSessionCookieSameSiteMode": "Lax"
			  }
			}
			""";
	}
	
	protected String buildLoginOptionsInline()
	{
		return """
			{
			  "AllowRememberLogin": false
			}
			""";
	}
	
	protected String buildLogoutOptionsInline()
	{
		return """
			{
			  "AutomaticRedirectAfterSignOut": true
			}
			""";
	}
	
	protected String buildClientsConfigurationInline()
	{
		return """
			[
			  {
			      "ClientId": "%s",
			      "ClientSecrets": [
			          "%s"
			      ],
			      "Description": "TimelineDesc",
			      "AllowedGrantTypes": [
			          "authorization_code",
			          "refresh_token"
			      ],
			      "RedirectUris": [
			          "*"
			      ],
			      "AllowedScopes": [
			          "openid",
			          "profile",
			          "email",
			          "offline_access"
			          %s
			      ],
			      "AlwaysIncludeUserClaimsInIdToken": true,
			      "AllowOfflineAccess": true,
			      "RequirePkce": false
			  }
			]
			""".formatted(
			this.clientId,
			this.clientSecret,
			this.additionalAllowedScopes != null && !this.additionalAllowedScopes.isEmpty()
				? ("," + this.additionalAllowedScopes.stream()
				.map(s -> "\"" + s + "\"")
				.collect(Collectors.joining(",")))
				: "");
	}
	
	public String getExternalHttpBaseEndPoint()
	{
		// noinspection HttpUrlsUsage
		return "http://"
			+ this.getHost()
			+ ":"
			+ this.getMappedPort(PORT);
	}
	
	// region Config
	
	public OIDCServerContainer withClientId(final String clientId)
	{
		this.clientId = clientId;
		return this;
	}
	
	public OIDCServerContainer withClientSecret(final String clientSecret)
	{
		this.clientSecret = clientSecret;
		return this;
	}
	
	public OIDCServerContainer withAdditionalAllowedScopes(final List<String> additionalAllowedScopes)
	{
		this.additionalAllowedScopes = additionalAllowedScopes;
		return this;
	}
	
	// endregion
}
