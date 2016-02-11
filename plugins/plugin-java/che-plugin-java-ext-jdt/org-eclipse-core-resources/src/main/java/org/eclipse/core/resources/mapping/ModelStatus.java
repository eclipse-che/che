/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources.mapping;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Status;

/**
 * A status returned by a model from the resource operation validator.
 * The severity indicates the severity of the possible side effects
 * of the operation. Any severity other than <code>OK</code> should be
 * shown to the user. The message should be a human readable message that
 * will allow the user to make a decision as to whether to continue with the 
 * operation. The model provider id should indicate which model is flagging the
 * the possible side effects.
 * <p>
 * Clients may instantiate or subclass this class.
 * </p>
 * 
 * @since 3.2
 */
public class ModelStatus extends Status {

	private final String modelProviderId;

	/**
	 * Create a model status.
	 * 
	 * @param severity the severity
	 * @param pluginId the plugin id
	 * @param modelProviderId the model provider id
	 * @param message the message
	 */
	public ModelStatus(int severity, String pluginId, String modelProviderId, String message) {
		super(severity, pluginId, 0, message, null);
		Assert.isNotNull(modelProviderId);
		this.modelProviderId = modelProviderId;
	}

	/**
	 * Return the id of the model provider from which this status originated.
	 * 
	 * @return the id of the model provider from which this status originated
	 */
	public String getModelProviderId() {
		return modelProviderId;
	}
}
