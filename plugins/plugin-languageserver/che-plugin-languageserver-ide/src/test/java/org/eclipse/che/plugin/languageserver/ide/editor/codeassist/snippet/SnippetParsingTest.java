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
  public void choice() {
    testParseAndRender(
        "${3:|foo,bar,zoz|}", e(Snippet.class, e(Placeholder.class, e(Choice.class))));
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
    assertEquals(snippetText, out.toString());
    assertSame(testExpression, snippet);
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
}
