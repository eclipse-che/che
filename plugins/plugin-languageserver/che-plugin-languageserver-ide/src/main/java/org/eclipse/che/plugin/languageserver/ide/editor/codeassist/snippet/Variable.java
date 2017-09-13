package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public class Variable extends SimpleExpression {
  private String name;
  private Expression value;

  public Variable(int startChar, int endChar, String name, Expression value) {
    super(startChar, endChar);
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  @Override
  public void accept(ExpressionVisitor v) {
    v.visit(this);
  }
}
