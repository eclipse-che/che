package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public interface VariableResolver {
  boolean isVar(String name);

  String resolve(String name);
}
