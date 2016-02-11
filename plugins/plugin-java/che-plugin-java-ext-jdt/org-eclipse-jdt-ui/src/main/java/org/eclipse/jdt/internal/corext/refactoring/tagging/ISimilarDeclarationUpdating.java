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
package org.eclipse.jdt.internal.corext.refactoring.tagging;

/**
 * Interface implemented by processors able to rename similar declarations.
 *
 * @since 3.2
 */
public interface ISimilarDeclarationUpdating {

	/**
	 * Checks if this refactoring object is capable of updating similar declarations
	 * of the renamed element.
	 *
	 * This can be disabled globally by setting the product configuration property
	 * "org.eclipse.jdt.ui.refactoring.handlesSimilarDeclarations" to "false".
	 */
	public boolean canEnableSimilarDeclarationUpdating();

	/**
	 * If <code>canEnableSimilarElementUpdating</code> returns
	 * <code>true</code>, then this method is used to inform the refactoring
	 * object whether similar declarations should be updated. This call can be
	 * ignored if <code>canEnableSimilarElementUpdating</code> returns
	 * <code>false</code>.
	 */
	public void setUpdateSimilarDeclarations(boolean update);

	/**
	 * If <code>canEnableSimilarElementUpdating</code> returns
	 * <code>true</code>, then this method is used to ask the refactoring
	 * object whether similar declarations should be updated. This call can be
	 * ignored if <code>canEnableSimilarElementUpdating</code> returns
	 * <code>false</code>.
	 */
	public boolean getUpdateSimilarDeclarations();

	/**
	 * If <code>canEnableSimilarElementUpdating</code> returns
	 * <code>true</code>, then this method is used to set the match strategy
	 * for determining similarly named elements.
	 *
	 * @param selectedStrategy one of the STRATEGY_* constants in {@link org.eclipse.jdt.internal.corext.refactoring.rename.RenamingNameSuggestor}
	 */
	public void setMatchStrategy(int selectedStrategy);

	/**
	 * If <code>canEnableSimilarElementUpdating</code> returns
	 * <code>true</code>, then this method is used to ask the refactoring
	 * object which match strategy is used for determining similar elements.
	 *
	 * @return one of the STRATEGY_* constants in {@link org.eclipse.jdt.internal.corext.refactoring.rename.RenamingNameSuggestor}
	 */
	public int getMatchStrategy();

}