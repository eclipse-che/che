package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public class Placeholder extends SimpleExpression {

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
