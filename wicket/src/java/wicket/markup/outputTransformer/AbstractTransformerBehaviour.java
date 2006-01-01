/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.outputTransformer;

import wicket.AbstractBehaviour;
import wicket.Component;
import wicket.RequestCycle;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.response.StringResponse;

/**
 * A IBehaviour which can be added to any component. It allows to post-process
 * (transform) the markup generated by the component.
 * 
 * @see wicket.markup.outputTransformer.AbstractOutputTransformerContainer
 * 
 * @author Juergen Donnerstag
 * 
 * TODO IBehaviour does not have an event which gets called in case of an
 * exception. Hence the response object might not be restored.
 */
public abstract class AbstractTransformerBehaviour extends AbstractBehaviour
		implements
			ITransformer
{
	private static final long serialVersionUID = 1L;

	private Response webResponse;

	/**
	 * Construct.
	 */
	public AbstractTransformerBehaviour()
	{
	}

	/**
	 * Create a new response object which is used to store the markup generated
	 * by the child objects.
	 * 
	 * @return Response object. Must not be null
	 */
	protected Response newResponse()
	{
		return new StringResponse();
	}

	/**
	 * @see wicket.IBehaviour#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		final RequestCycle requestCycle = RequestCycle.get();

		// Temporarily replace the web response with a String response
		this.webResponse = requestCycle.getResponse();

		// Create a new response object
		final Response response = newResponse();
		if (response == null)
		{
			throw new IllegalStateException("newResponse() must not return null");
		}

		// and make it the current one
		requestCycle.setResponse(response);
	}

	/**
	 * @see wicket.IBehaviour#rendered(wicket.Component)
	 */
	public void rendered(final Component component)
	{
		final RequestCycle requestCycle = RequestCycle.get();

		try
		{
			Response response = requestCycle.getResponse();

			// Tranform the data
			CharSequence output = transform(component, response.toString());
			this.webResponse.write(output.toString());
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException("Error while transforming the output: " + this, ex);
		}

		// Restore the original response object
		requestCycle.setResponse(this.webResponse);
	}

	/**
	 * 
	 * @see wicket.markup.outputTransformer.ITransformer#transform(wicket.Component,
	 *      java.lang.String)
	 */
	public abstract CharSequence transform(final Component component, final String output)
			throws Exception;
}
