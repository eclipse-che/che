/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
