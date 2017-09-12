package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.NotImplementedException;

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
  
  public final void accept(ExpressionVisitor v) {
    try {
      try {
        v.getClass().getMethod("visit", new Class[] {getClass()}).invoke(v, this);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    } catch (NoSuchMethodException | SecurityException e) {
      throw new NotImplementedException(e);
    }
  }
}
