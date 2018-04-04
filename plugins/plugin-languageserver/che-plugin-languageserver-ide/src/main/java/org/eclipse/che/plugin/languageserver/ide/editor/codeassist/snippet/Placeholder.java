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
