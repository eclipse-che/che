/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring.tagging;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/** Represents processors in the JDT space that rename elements. */
public interface INameUpdating {

  /**
   * Sets new name for the entity that this refactoring is working on.
   *
   * @param newName the new name
   */
  public void setNewElementName(String newName);

  /**
   * Get the name for the entity that this refactoring is working on.
   *
   * @return returns the new name
   */
  public String getNewElementName();

  /**
   * Gets the current name of the entity that this refactoring is working on.
   *
   * @return returns the current name
   */
  public String getCurrentElementName();

  /**
   * Gets the original elements. Since an <code>INameUpdating</code> only renames one element, this
   * method must return an array containing exactly one element.
   *
   * @return an array containing exactly one element
   * @see org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor#getElements()
   */
  public Object[] getElements();

  /**
   * Gets the element after renaming, or <code>null</code> if not available.
   *
   * @return returns the new element or <code>null</code>
   * @throws CoreException thrown when the new element could not be evaluated
   */
  public Object getNewElement() throws CoreException;

  /**
   * Checks if the new name is valid for the entity that this refactoring renames.
   *
   * @param newName the new name
   * @return returns the resulting status
   * @throws CoreException Core exception is thrown when the validation could not be performed
   */
  public RefactoringStatus checkNewElementName(String newName) throws CoreException;
}
