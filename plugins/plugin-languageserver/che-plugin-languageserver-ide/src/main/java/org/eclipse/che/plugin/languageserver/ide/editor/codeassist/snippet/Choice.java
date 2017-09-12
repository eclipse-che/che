package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

import java.util.List;

public class Choice extends SimpleExpression {

  private List<String> choices;

  public Choice(int startChar, int endChar, List<String> choices) {
    super(startChar,endChar);
    this.choices = choices;
  }
  
  public List<String> getChoices() {
    return choices;
  }
}