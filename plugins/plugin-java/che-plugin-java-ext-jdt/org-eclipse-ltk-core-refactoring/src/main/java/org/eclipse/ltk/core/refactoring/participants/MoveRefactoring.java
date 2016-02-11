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
 * A generic move refactoring. The actual refactoring is done
 * by the move processor passed to the constructor.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MoveRefactoring extends ProcessorBasedRefactoring {

	private MoveProcessor fProcessor;

	/**
	 * Creates a new move refactoring with the given move processor.
	 *
	 * @param processor the move processor
	 */
	public MoveRefactoring(MoveProcessor processor) {
		super(processor);
		Assert.isNotNull(processor);
		fProcessor= processor;
	}

	/**
	 * Returns the move processor associated with this move refactoring.
	 *
	 * @return returns the move processor associated with this move refactoring
	 */
	public MoveProcessor getMoveProcessor() {
		return fProcessor;
	}

	/**
	 * {@inheritDoc}
	 */
	public RefactoringProcessor getProcessor() {
		return fProcessor;
	}
}
