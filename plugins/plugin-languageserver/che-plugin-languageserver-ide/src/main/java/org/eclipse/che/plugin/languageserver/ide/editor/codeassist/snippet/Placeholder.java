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
 * Expression representing placeholders and tab stops.
 *
 * @author Thomas Mäder
 */
public class Placeholder extends Expression {

  private Expression value;
  private int id;

  public Placeholder(int startChar, int endChar, int id, Expression value) {
    super(startChar, endChar);
    this.id = id;
    this.value = value;
  }

  public int getId() {
    return id;
  }

  public Expression getValue() {
    return value;
  }

  @Override
  public void accept(ExpressionVisitor v) {
    v.visit(this);
  }
}
