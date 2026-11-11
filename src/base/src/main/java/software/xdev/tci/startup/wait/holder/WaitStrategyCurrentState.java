/*
 * Copyright © 2024 XDEV Software (https://xdev.software)
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
package software.xdev.tci.startup.wait.holder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class WaitStrategyCurrentState
{
	private String errorMessage;
	private Exception exception;
	private boolean running = true;
	
	private final Object key;
	private final List<WaitStrategyCurrentState> children = new ArrayList<>();
	
	public WaitStrategyCurrentState(final Object key)
	{
		this.key = key;
	}
	
	public WaitStrategyCurrentState withErrorMessage(final String errorMessage)
	{
		return this.with(errorMessage, null);
	}
	
	public WaitStrategyCurrentState withException(final Exception exception)
	{
		return this.with(null, exception);
	}
	
	protected WaitStrategyCurrentState with(final String errorMessage, final Exception exception)
	{
		this.errorMessage = errorMessage;
		this.exception = exception;
		return this;
	}
	
	public WaitStrategyCurrentState newChild(final Object key)
	{
		final WaitStrategyCurrentState newHolder = new WaitStrategyCurrentState(key);
		this.children.add(newHolder);
		return newHolder;
	}
	
	public WaitStrategyCurrentState successfullyFinished()
	{
		this.children.clear();
		this.running = false;
		return this.with(null, null);
	}
	
	public Optional<Exception> exception()
	{
		if(this.exception == null)
		{
			if(this.errorMessage != null)
			{
				this.exception = new IllegalStateException("Failed: " + this.errorMessage);
			}
			else if(this.running && this.children.isEmpty())
			{
				this.exception = new IllegalStateException("Still running");
			}
		}
		
		final List<Exception> childExceptions = this.children.stream()
			.map(WaitStrategyCurrentState::exception)
			.filter(Optional::isPresent)
			.map(Optional::orElseThrow)
			.toList();
		
		if(this.exception == null && childExceptions.isEmpty())
		{
			return Optional.empty();
		}
		
		final String msg = this.key + " failed";
		final WaitStrategyStateException ex = this.exception != null
			? new WaitStrategyStateException(msg, this.exception)
			: new WaitStrategyStateException(msg);
		childExceptions.forEach(ex::addSuppressed);
		return Optional.of(ex);
	}
	
	static class WaitStrategyStateException extends RuntimeException
	{
		protected WaitStrategyStateException(final String message)
		{
			super(message);
		}
		
		protected WaitStrategyStateException(final String message, final Throwable cause)
		{
			super(message, cause);
		}
	}
}
