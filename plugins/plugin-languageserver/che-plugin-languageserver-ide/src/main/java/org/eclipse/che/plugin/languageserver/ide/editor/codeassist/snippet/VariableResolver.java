/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

/**
 * Resolves variable values when evaluating a snippet.
 *
 * @author Thomas Mäder
 */
public interface VariableResolver {
  /**
   * Whether the given variable is a variable this resolver can resolve
   *
   * @param name the name of the variable
   * @return true if the name designates a known variable name.
   */
  boolean isVar(String name);

  /**
   * Returns the value of the variable at this time.
   *
   * @param name the variable name
   * @return the current value. Maybe null.
   */
  String resolve(String name);
}
