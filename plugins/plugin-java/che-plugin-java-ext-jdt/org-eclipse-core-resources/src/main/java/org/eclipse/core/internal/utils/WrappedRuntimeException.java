/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.utils;

public class WrappedRuntimeException extends RuntimeException {

	/**
	 * All serializable objects should have a stable serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private Throwable target;

	public WrappedRuntimeException(Throwable target) {
		super();
		this.target = target;
	}

	public Throwable getTargetException() {
		return this.target;
	}

	@Override
	public String getMessage() {
		return target.getMessage();
	}
}
