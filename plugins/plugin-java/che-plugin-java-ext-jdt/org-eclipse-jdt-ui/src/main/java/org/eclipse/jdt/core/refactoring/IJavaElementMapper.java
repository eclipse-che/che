/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.core.refactoring;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;

/**
 * An <code>IJavaElementMapper</code> provides methods to map an original elements to its refactored
 * counterparts.
 *
 * <p>An <code>IJavaElementMapper</code> can be obtained via {@link
 * RefactoringProcessor#getAdapter(Class)}.
 *
 * @since 1.1
 */
public interface IJavaElementMapper {

  /**
   * Returns the refactored Java element for the given element. The returned Java element might not
   * yet exist when the method is called. Note that local variables <strong>cannot</strong> be
   * mapped using this method.
   *
   * <p>
   *
   * @param element the element to be refactored
   * @return the refactored element for the given element
   */
  IJavaElement getRefactoredJavaElement(IJavaElement element);
}
