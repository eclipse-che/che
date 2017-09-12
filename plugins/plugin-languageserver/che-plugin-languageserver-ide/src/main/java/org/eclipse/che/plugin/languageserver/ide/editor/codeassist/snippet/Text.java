package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public class Text extends SimpleExpression {

  private String value;

  public Text(int startChar, int endChar, String value) {
    super(startChar, endChar);
    this.value= value;
  }
  
  public String getValue() {
    return value;
  } 
}
