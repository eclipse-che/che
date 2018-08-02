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

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import org.junit.Test;

public class SnippetParsingTest {
  @Test
  public void simplePlaceHolder() {
    testParseAndRender("$1", e(Snippet.class, e(Placeholder.class)));
    testParseAndRender(
        "{\n$1\n}", e(Snippet.class, e(Text.class), e(Placeholder.class), e(Text.class)));
    testParseAndRender(
        "$1$3989$27",
        e(Snippet.class, e(Placeholder.class), e(Placeholder.class), e(Placeholder.class)));
    testParseAndRender("$1:Gurke 2", e(Snippet.class, e(Placeholder.class), e(Text.class)));
    testParseAndRender(
        "$1$2   $3",
        e(
            Snippet.class,
            e(Placeholder.class),
            e(Placeholder.class),
            e(Text.class),
            e(Placeholder.class)));
  }

  @Test
  public void simpleVariable() {
    testParseAndRender("$foo", e(Snippet.class, e(Variable.class)));
    testParseAndRender(
        "${foo:bar}", e(Snippet.class, e(Variable.class, e(Snippet.class, e(Text.class)))));
    testParseAndRender(
        "${foo:$5233}",
        e(Snippet.class, e(Variable.class, e(Snippet.class, e(Placeholder.class)))));
  }

  @Test
  public void testChoice() {
    testParseAndRender("${3:|foo,bar,zoz|}", snippet(placeholder(choice())));
    testParseAndRender(
        "{ ${1:somevalue}    ${2:|first,second,third|} ${3:some text $4}",
        snippet(
            text(),
            placeholder(snippet(text())),
            text(),
            placeholder(choice()),
            text(),
            placeholder(snippet(text(), placeholder()))));
  }

  @Test
  public void nestedPlaceHolder() {
    testParseAndRender(
        "${1:Gurke $2}",
        e(
            Snippet.class,
            e(Placeholder.class, e(Snippet.class, e(Text.class), e(Placeholder.class)))));
    testParseAndRender(
        "$1:Gurke ${2:3}",
        e(
            Snippet.class,
            e(Placeholder.class),
            e(Text.class),
            e(Placeholder.class, e(Snippet.class, e(Text.class)))));
  }

  @Test
  public void nestedVariable() {
    testParseAndRender(
        "${1:${foo:$3}}",
        e(
            Snippet.class,
            e(
                Placeholder.class,
                e(Snippet.class, e(Variable.class, e(Snippet.class, e(Placeholder.class)))))));
  }

  private void testParseAndRender(String snippetText, TestExpression testExpression) {
    Snippet snippet = new SnippetParser(snippetText).parse();
    StringWriter out = new StringWriter();
    snippet.accept(new ExpressionPrinter(out));
    System.out.println(out.toString());
    assertSame(testExpression, snippet);
    assertEquals(snippetText, out.toString());
  }

  private static class TestExpression {
    private Class<?> type;
    private List<TestExpression> children;

    public TestExpression(Class<?> type, List<TestExpression> children) {
      this.type = type;
      this.children = children;
    }

    public Class<?> getType() {
      return type;
    }

    public List<TestExpression> getChildren() {
      return children;
    }
  };

  private static class ExpressionAsserter implements ExpressionVisitor {
    private Stack<TestExpression> state = new Stack<>();

    public ExpressionAsserter(TestExpression expr) {
      state.push(expr);
    }

    @Override
    public void visit(DollarExpression e) {
      e.getValue().accept(this);
    }

    @Override
    public void visit(Choice e) {
      assertEquals(Choice.class, state.peek().getType());
    }

    @Override
    public void visit(Placeholder e) {
      TestExpression current = state.peek();
      assertEquals(Placeholder.class, current.getType());
      if (e.getValue() != null) {
        state.push(current.getChildren().get(0));
        e.getValue().accept(this);
        state.pop();
      }
    }

    @Override
    public void visit(Snippet e) {
      TestExpression current = state.peek();
      assertEquals(Snippet.class, current.getType());
      for (int i = 0; i < e.getExpressions().size(); i++) {
        state.push(current.getChildren().get(i));
        e.getExpressions().get(i).accept(this);
        state.pop();
      }
    }

    @Override
    public void visit(Text e) {
      TestExpression current = state.peek();
      assertEquals(Text.class, current.getType());
    }

    @Override
    public void visit(Variable e) {
      TestExpression current = state.peek();
      assertEquals(Variable.class, current.getType());
      if (e.getValue() != null) {
        state.push(current.getChildren().get(0));
        e.getValue().accept(this);
        state.pop();
      }
    }
  }

  public static void assertSame(TestExpression expected, Expression actual) {
    actual.accept(new ExpressionAsserter(expected));
  }

  private TestExpression e(Class<?> type, TestExpression... children) {
    return new TestExpression(type, Arrays.asList(children));
  }

  private TestExpression snippet(TestExpression... children) {
    return e(Snippet.class, children);
  }

  private TestExpression placeholder(TestExpression child) {
    return e(Placeholder.class, child);
  }

  private TestExpression placeholder() {
    return e(Placeholder.class);
  }

  private TestExpression text() {
    return e(Text.class);
  }

  private TestExpression choice() {
    return e(Choice.class);
  }
}
