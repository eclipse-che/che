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
package org.eclipse.ltk.core.refactoring;

/**
 * Comparator class to compare two refactoring status entries. The general
 * contract of this class is equivalent to the one of class
 * {@link java.util.Comparator}from the Java Collection Framework.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 *
 * @since 3.1
 */
public interface IRefactoringStatusEntryComparator {

	/**
	 * Compares two refactoring status entries for order. Returns a negative
	 * integer, zero, or a positive integer as the first status entry is less
	 * than, equal to, or greater than the second.
	 * <p>
	 * The implementor must ensure that <tt>sgn(compare(x, y)) ==
	 * -sgn(compare(y, x))</tt>
	 * for all <tt>x</tt> and <tt>y</tt>.
	 * </p>
	 * <p>
	 * The implementor must ensure that the relation is transitive:
	 * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
	 * <tt>compare(x, z)&gt;0</tt>.
	 * </p>
	 * <p>
	 * Furthermore, the implementer must ensure that <tt>compare(x, y)==0</tt>
	 * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
	 * <tt>z</tt>.
	 * </p>
	 *
	 * @param entry1 the first refactoring status entry to be compared.
	 * @param entry2 the second refactoring status entry to be compared.
	 * @return a negative integer, zero, or a positive integer as the first
	 *         status entry is less than, equal to, or greater than the second.
	 */
	public int compare(RefactoringStatusEntry entry1, RefactoringStatusEntry entry2);
}
