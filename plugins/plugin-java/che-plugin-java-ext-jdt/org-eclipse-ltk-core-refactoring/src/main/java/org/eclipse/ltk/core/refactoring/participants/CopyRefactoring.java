/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring.participants;

import org.eclipse.core.runtime.Assert;

/**
 * A generic copy refactoring. The actual refactoring is done
 * by the copy processor passed to the constructor.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.1
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CopyRefactoring extends ProcessorBasedRefactoring {

	private CopyProcessor fProcessor;

	/**
	 * Creates a new copy refactoring with the given copy processor.
	 *
	 * @param processor the copy processor
	 */
	public CopyRefactoring(CopyProcessor processor) {
		super(processor);
		Assert.isNotNull(processor);
		fProcessor= processor;
	}

	/**
	 * Returns the copy processor associated with this copy refactoring.
	 *
	 * @return returns the copy processor associated with this copy refactoring
	 */
	public CopyProcessor getCopyProcessor() {
		return fProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}
}
