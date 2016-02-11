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

/**
 * A special processor that performs move operations. A move processor is
 * responsible for actually moving the elements. Additionally it may update
 * other resources of the same domain which are affected by the move operation.
 * For example, a Java type move processor also updates all references in Java
 * code to the type to be moved.
 * <p>
 * This class should be subclassed by clients wishing to provide a special move
 * processor.
 * </p>
 * <p>
 * The main purpose of this class is type safety for the generic move
 * refactoring
 * </p>
 *
 * @since 3.0
 */
public abstract class MoveProcessor extends RefactoringProcessor {

}
