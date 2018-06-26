package org.eclipse.che.plugin.java.server.rest.recommend.parser;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.model.ResultModel;
import org.eclipse.che.plugin.java.server.rest.recommend.similarity.Similarity;
import org.eclipse.che.plugin.java.server.rest.recommend.util.Util;
import org.eclipse.che.plugin.java.server.rest.recommend.visitor.FieldRefactorVisitor;
import org.eclipse.che.plugin.java.server.rest.recommend.visitor.LocalVariableRefactorVisitor;
import org.eclipse.che.plugin.java.server.rest.recommend.visitor.MethodRefactorVisitor;
import org.eclipse.che.plugin.java.server.rest.recommend.visitor.TypeRefactorVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class Parser {

  private ResultModel result;
  private List<ResultModel> results;

  private ITypeBinding typeBinding;
  private ITypeBinding[] superInterfaces;
  private ITypeBinding superClass;
  private String originalPackageName = "";

  public Parser() {
    result = new ResultModel();
    results = new ArrayList<ResultModel>();
  }

  public void setResult(ResultModel result) {
    this.result = result;
  }

  public ResultModel getResult() {
    return result;
  }

  public void parse(DataModel renameData) {
    if (renameData.getRefactorType().equals("org.eclipse.jdt.ui.recommend.method")) {
      ICompilationUnit icu = Util.getCompilationUnit(renameData);
      if (icu == null) return;
      methodRefactorParse(renameData, icu);

    } else if (renameData.getRefactorType().equals("org.eclipse.jdt.ui.recommend.field")) {

      ICompilationUnit icu = Util.getCompilationUnit(renameData);

      if (icu != null) fieldRefactorParse(renameData, icu);

    } else if (renameData.getRefactorType().equals("org.eclipse.jdt.ui.recommend.local.variable")) {
      ICompilationUnit icu = Util.getCompilationUnit(renameData);
      if (icu != null) localVariableRefactorParse(renameData, icu);

    } else if (renameData.getRefactorType().equals("org.eclipse.jdt.ui.recommend.type")) {
      ICompilationUnit icu = Util.getCompilationUnit1(renameData);
      if (icu == null) return;
      typeRefactorParse(renameData, icu, true);

      List<ICompilationUnit> iCompilationUnits = Util.getAllCompilationUnit(renameData);
      for (ICompilationUnit iCompilationUnit : iCompilationUnits) {
        if (iCompilationUnit == null) continue;
        if (iCompilationUnit.getHandleIdentifier() == null
            || icu.getHandleIdentifier() == null
            || iCompilationUnit
                .getHandleIdentifier()
                .toString()
                .equals(icu.getHandleIdentifier().toString())) {
          continue;
        }
        typeRefactorParse(renameData, iCompilationUnit, false);
      }
    } else {
      return;
    }
    double maxSimilarity = 0.0;
    for (ResultModel candidateResult : results) {
      if (candidateResult.getSim() > maxSimilarity
          || (candidateResult.getSim() == maxSimilarity
              && maxSimilarity > 0.0
              && Similarity.isMoreSim(candidateResult, result))) {
        maxSimilarity = candidateResult.getSim();
        result = candidateResult;
      }
    }
  }

  private void methodRefactorParse(DataModel renameData, ICompilationUnit compilationUnit) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    MethodRefactorVisitor visitor = new MethodRefactorVisitor();
    visitor.setRenameData(renameData);
    unit.accept(visitor);
    ResultModel result = visitor.getResult();
    result.setRecommendProjectName(renameData.getProjectName());
    result.setRenameOriginalName(renameData.getOriginalName());
    results.add(result);
  }

  private void localVariableRefactorParse(DataModel renameData, ICompilationUnit compilationUnit) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    LocalVariableRefactorVisitor visitor = new LocalVariableRefactorVisitor();
    visitor.setRenameData(renameData);
    unit.accept(visitor);
    ResultModel result = visitor.getResult();
    result.setRecommendProjectName(renameData.getProjectName());
    result.setRenameOriginalName(renameData.getOriginalName());
    results.add(result);
  }

  private void fieldRefactorParse(DataModel renameData, ICompilationUnit compilationUnit) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    FieldRefactorVisitor visitor = new FieldRefactorVisitor();
    visitor.setRenameData(renameData);
    unit.accept(visitor);

    ResultModel result = visitor.getResult();
    result.setRecommendProjectName(renameData.getProjectName());
    result.setRenameOriginalName(renameData.getOriginalName());
    results.add(result);
  }

  private void typeRefactorParse(
      DataModel renameData, ICompilationUnit compilationUnit, boolean isTypeSelf) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    TypeRefactorVisitor visitor = new TypeRefactorVisitor();
    visitor.setRenameData(renameData);
    visitor.setTypeSelf(isTypeSelf);
    if (!isTypeSelf) {
      visitor.setTypeBinding(typeBinding);
      visitor.setSuperInterfaces(superInterfaces);
      visitor.setSuperClass(superClass);
      visitor.setOriginalPackageName(originalPackageName);
    }
    unit.accept(visitor);
    if (isTypeSelf) {
      typeBinding = visitor.getTypeBinding();
      superInterfaces = visitor.getSuperInterfaces();
      superClass = visitor.getSuperClass();
      originalPackageName = visitor.getOriginalPackageName();
    }
    ResultModel result = visitor.getResult();
    result.setRecommendProjectName(renameData.getProjectName());
    result.setRenameOriginalName(renameData.getOriginalName());
    results.add(result);
  }
}
