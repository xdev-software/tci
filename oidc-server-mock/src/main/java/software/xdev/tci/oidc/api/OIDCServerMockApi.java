package software.xdev.tci.oidc.api;

import java.util.concurrent.atomic.AtomicLong;


public abstract class OIDCServerMockApi
{
	protected final AtomicLong currentSubjectId = new AtomicLong(0);
	
	/**
	 * @return The subjectId
	 */
	public abstract String addUser(String email, String name, String pw);
	
	protected String nextSubjectId()
	{
		return String.valueOf(this.currentSubjectId.incrementAndGet());
	}
}
