package org.eclipse.che.plugin.languageserver.ide.editor.codeassist.snippet;

public interface ExpressionVisitor {
  void visit(DollarExpression e);
  void visit(Choice e);
  void visit(Placeholder e);
  void visit(Snippet e);
  void visit(Text e);
  void visit(Variable e);
  
}
