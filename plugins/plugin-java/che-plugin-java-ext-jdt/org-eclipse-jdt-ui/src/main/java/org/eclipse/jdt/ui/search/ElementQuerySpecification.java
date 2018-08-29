/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2008 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.ui.search;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;

/**
 * Describes a search query by giving the {@link IJavaElement} to search for.
 *
 * <p>This class is not intended to be instantiated or subclassed by clients.
 *
 * @see org.eclipse.jdt.ui.search.QuerySpecification
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ElementQuerySpecification extends QuerySpecification {
  private IJavaElement fElement;

  /**
   * A constructor.
   *
   * @param javaElement The java element the query should search for.
   * @param limitTo The kind of occurrence the query should search for.
   * @param scope The scope to search in.
   * @param scopeDescription A human readable description of the search scope.
   */
  public ElementQuerySpecification(
      IJavaElement javaElement, int limitTo, IJavaSearchScope scope, String scopeDescription) {
    super(limitTo, scope, scopeDescription);
    fElement = javaElement;
  }

  /**
   * Returns the element to search for.
   *
   * @return The element to search for.
   */
  public IJavaElement getElement() {
    return fElement;
  }
}
