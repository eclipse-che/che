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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ExpressionPrinter implements ExpressionVisitor {
  private Writer out;

  public ExpressionPrinter(Writer out) {
    this.out = out;
  }

  public static void iterate(Expression e, PrintWriter out) {
    e.accept(new ExpressionPrinter(out));
  }

  @Override
  public void visit(Choice e) {
    try {
      out.write('|');
      boolean first = true;
      for (String choice : e.getChoices()) {
        if (first) {
          first = false;
        } else {
          out.write(",");
        }
        out.write(choice);
      }
      out.write('|');
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }

  @Override
  public void visit(Placeholder e) {
    try {
      out.write(String.valueOf(e.getId()));
      if (e.getValue() != null) {
        out.write(':');
        e.getValue().accept(this);
      }
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }

  @Override
  public void visit(Snippet e) {
    e.getExpressions().forEach(expr -> expr.accept(this));
  }

  @Override
  public void visit(Text e) {
    try {
      out.write(e.getValue());
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }

  @Override
  public void visit(Variable e) {
    try {
      out.write(e.getName());
      if (e.getValue() != null) {
        out.write(':');
        e.getValue().accept(this);
      }
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }

  @Override
  public void visit(DollarExpression e) {
    try {
      out.write('$');
      if (e.isBrace()) {
        out.write('{');
      }
      e.getValue().accept(this);
      if (e.isBrace()) {
        out.write('}');
      }
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
  }
}
