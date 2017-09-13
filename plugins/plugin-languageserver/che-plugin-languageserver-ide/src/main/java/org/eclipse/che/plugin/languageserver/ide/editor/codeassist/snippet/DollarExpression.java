package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public class DollarExpression extends SimpleExpression {

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
