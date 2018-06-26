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

  public ResultModel result = null;
  public List<ResultModel> results = null;

  public ITypeBinding typeBinding = null;
  public ITypeBinding[] superInterfaces = null;
  public ITypeBinding superClass = null;
  public String originalPackageName = "";

  public Parser() {
    result = new ResultModel();
    results = new ArrayList<ResultModel>();
  }

  public void parse(DataModel renameData) {
    if (renameData.refactorType.equals("org.eclipse.jdt.ui.recommend.method")) {
      ICompilationUnit icu = Util.getCompilationUnit(renameData);
      if (icu == null) return;
      methodRefactorParse(renameData, icu);

    } else if (renameData.refactorType.equals("org.eclipse.jdt.ui.recommend.field")) {

      ICompilationUnit icu = Util.getCompilationUnit(renameData);

      if (icu != null) fieldRefactorParse(renameData, icu);

    } else if (renameData.refactorType.equals("org.eclipse.jdt.ui.recommend.local.variable")) {
      ICompilationUnit icu = Util.getCompilationUnit(renameData);
      if (icu != null) localVariableRefactorParse(renameData, icu);

    } else if (renameData.refactorType.equals("org.eclipse.jdt.ui.recommend.type")) {
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
      if (candidateResult.sim > maxSimilarity
          || (candidateResult.sim == maxSimilarity
              && maxSimilarity > 0.0
              && Similarity.isMoreSim(candidateResult, result))) {
        maxSimilarity = candidateResult.sim;
        result = candidateResult;
      }
    }
  }

  private void methodRefactorParse(DataModel renameData, ICompilationUnit compilationUnit) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    MethodRefactorVisitor visitor = new MethodRefactorVisitor();
    visitor.projectName = compilationUnit.getJavaProject().getProject().getName();
    visitor.renameData = renameData;
    unit.accept(visitor);
    ResultModel result = visitor.result;
    result.recommendProjectName = renameData.projectName;
    result.renameOriginalName = renameData.originalName;
    results.add(result);
  }

  private void localVariableRefactorParse(DataModel renameData, ICompilationUnit compilationUnit) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    LocalVariableRefactorVisitor visitor = new LocalVariableRefactorVisitor();
    visitor.projectName = compilationUnit.getJavaProject().getProject().getName();
    visitor.renameData = renameData;
    unit.accept(visitor);
    ResultModel result = visitor.result;
    result.recommendProjectName = renameData.projectName;
    result.renameOriginalName = renameData.originalName;
    results.add(result);
  }

  private void fieldRefactorParse(DataModel renameData, ICompilationUnit compilationUnit) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    FieldRefactorVisitor visitor = new FieldRefactorVisitor();
    visitor.projectName = compilationUnit.getJavaProject().getProject().getName();
    visitor.renameData = renameData;
    unit.accept(visitor);

    ResultModel result = visitor.result;
    result.recommendProjectName = renameData.projectName;
    result.renameOriginalName = renameData.originalName;
    results.add(result);
  }

  private void typeRefactorParse(
      DataModel renameData, ICompilationUnit compilationUnit, boolean isTypeSelf) {
    CompilationUnit unit = Util.createCompilationUnit(compilationUnit);
    if (unit == null) return;

    TypeRefactorVisitor visitor = new TypeRefactorVisitor();
    visitor.projectName = compilationUnit.getJavaProject().getProject().getName();
    visitor.renameData = renameData;
    visitor.isTypeSelf = isTypeSelf;
    if (!isTypeSelf) {
      visitor.typeBinding = typeBinding;
      visitor.superInterfaces = superInterfaces;
      visitor.superClass = superClass;
      visitor.originalPackageName = originalPackageName;
    }
    unit.accept(visitor);
    if (isTypeSelf) {
      typeBinding = visitor.typeBinding;
      superInterfaces = visitor.superInterfaces;
      superClass = visitor.superClass;
      originalPackageName = visitor.originalPackageName;
    }
    ResultModel result = visitor.result;
    result.recommendProjectName = renameData.projectName;
    result.renameOriginalName = renameData.originalName;
    results.add(result);
  }
}
