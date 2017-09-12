package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public abstract class SimpleExpression extends Expression {
  public SimpleExpression(int startChar, int endChar) {
    super(startChar, endChar);
  }
}
