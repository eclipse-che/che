/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * A generic delete refactoring. The actual refactoring is done
 * by the delete processor passed to the constructor.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DeleteRefactoring extends ProcessorBasedRefactoring {

	private DeleteProcessor fProcessor;

	/**
	 * Constructs a new delete refactoring for the given processor.
	 *
	 * @param processor the delete processor
	 */
	public DeleteRefactoring(DeleteProcessor processor) {
		super(processor);
		Assert.isNotNull(processor);
		fProcessor= processor;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}
}
