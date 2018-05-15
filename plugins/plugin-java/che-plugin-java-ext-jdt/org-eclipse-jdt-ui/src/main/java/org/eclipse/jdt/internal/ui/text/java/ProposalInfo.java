/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;

public class ProposalInfo {

  private boolean fJavadocResolved = false;
  private String fJavadoc = null;

  protected IJavaElement fElement;

  public ProposalInfo(IMember member) {
    fElement = member;
  }

  protected ProposalInfo() {
    fElement = null;
  }

  /**
   * Returns the Java element.
   *
   * @throws org.eclipse.jdt.core.JavaModelException if accessing the java model fails
   * @return the Java element
   */
  public IJavaElement getJavaElement() throws JavaModelException {
    return fElement;
  }
}
