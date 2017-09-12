package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

import java.util.List;

public class Snippet extends Expression {

  private List<Expression> expressions;

  public Snippet(int startChar, int endChar, List<Expression> expressions) {
    super(startChar, endChar);
    this.expressions = expressions;
  }

  public List<Expression> getExpressions() {
    return expressions;
  }
}
