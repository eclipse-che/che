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
 * Expression starting with a dollar sign. This expression type exists to facilitate parser
 * implementation.
 *
 * @author Thomas Mäder
 */
public class DollarExpression extends Expression {

  private Expression value;
  private boolean brace;

  public DollarExpression(int startChar, int endChar, Expression value, boolean brace) {
    super(startChar, endChar);
    this.value = value;
    this.brace = brace;
  }

  public Expression getValue() {
    return value;
  }

  public boolean isBrace() {
    return brace;
  }

  @Override
  public void accept(ExpressionVisitor v) {
    v.visit(this);
  }
}
