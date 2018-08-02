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

public class Text extends Expression {

  private String value;

  public Text(int startChar, int endChar, String value) {
    super(startChar, endChar);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public void accept(ExpressionVisitor v) {
    v.visit(this);
  }
}
