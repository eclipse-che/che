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
 * Represents an expression in vscode snippet syntax. Start and end locations indicate first an last
 * character index of the expression.
 *
 * @author Thomas Mäder
 */
public abstract class Expression {
  private int startChar;
  private int endChar;

  public Expression(int startChar, int endChar) {
    this.startChar = startChar;
    this.endChar = endChar;
  }

  public int getStartChar() {
    return startChar;
  }

  public int getEndChar() {
    return endChar;
  }

  public abstract void accept(ExpressionVisitor v);
}
