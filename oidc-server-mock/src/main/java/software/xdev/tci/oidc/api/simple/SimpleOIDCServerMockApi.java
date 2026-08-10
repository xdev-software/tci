package software.xdev.tci.oidc.api.simple;

import software.xdev.tci.oidc.api.HttpClientBasedOIDCServerMockApi;


public class SimpleOIDCServerMockApi extends HttpClientBasedOIDCServerMockApi
{
	public SimpleOIDCServerMockApi(final String externalHttpBaseEndPoint)
	{
		super(externalHttpBaseEndPoint);
	}
	
	@Override
	public String addUser(
		final String email,
		final String name,
		final String pw)
	{
		final String subjectId = this.nextSubjectId();
		this.apiAddUser(this.createDefaultBodyForAddUser(subjectId, email, name, pw));
		return subjectId;
	}
	
	public void replaceUser(
		final String subjectId,
		final String email,
		final String name,
		final String pw)
	{
		this.apiReplaceUser(this.createDefaultBodyForAddUser(subjectId, email, name, pw));
	}
	
	protected String createDefaultBodyForAddUser(
		final String subjectId,
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
			subjectId,
			email,
			pw,
			name,
			email
		);
	}
}
