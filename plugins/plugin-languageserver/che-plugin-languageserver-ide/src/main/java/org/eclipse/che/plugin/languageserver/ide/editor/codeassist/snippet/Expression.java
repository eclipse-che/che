package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

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
