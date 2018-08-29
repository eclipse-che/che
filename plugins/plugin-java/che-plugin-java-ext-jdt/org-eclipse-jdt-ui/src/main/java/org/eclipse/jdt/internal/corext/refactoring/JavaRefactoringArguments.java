/**
 * ***************************************************************************** Copyright (c) 2005,
 * 2011 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.refactoring;

import java.util.Map;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

/**
 * A wrapper around the Map received from {@link JavaRefactoringDescriptor} to access and convert
 * the options.
 */
public final class JavaRefactoringArguments {

  /** The attribute map (element type: <code>&lt;String, String&gt;</code>) */
  private final Map<String, String> fAttributes;

  /** The name of the project, or <code>null</code> for the workspace */
  private String fProject;

  /**
   * Creates a new java refactoring arguments from arguments
   *
   * @param project the project, or <code>null</code> for the workspace
   * @param arguments the arguments
   */
  public JavaRefactoringArguments(String project, Map<String, String> arguments) {
    fProject = project;
    fAttributes = arguments;
  }

  /**
   * Returns the attribute with the specified name.
   *
   * @param name the name of the attribute
   * @return the attribute value, or <code>null</code>
   */
  public String getAttribute(final String name) {
    return fAttributes.get(name);
  }

  /**
   * Returns the name of the project.
   *
   * @return the name of the project, or <code>null</code> for the workspace
   */
  public String getProject() {
    return fProject;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return getClass().getName() + fAttributes.toString();
  }
}
