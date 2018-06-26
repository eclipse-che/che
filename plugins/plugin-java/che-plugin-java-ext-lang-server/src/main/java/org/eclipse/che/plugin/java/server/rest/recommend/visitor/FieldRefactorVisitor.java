package org.eclipse.che.plugin.java.server.rest.recommend.visitor;

import org.eclipse.che.plugin.java.server.rest.recommend.common.Config;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.model.ResultModel;
import org.eclipse.che.plugin.java.server.rest.recommend.similarity.Recommend;
import org.eclipse.che.plugin.java.server.rest.recommend.similarity.Similarity;
import org.eclipse.che.plugin.java.server.rest.recommend.util.Util;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class FieldRefactorVisitor extends ASTVisitor {

  private ResultModel result;
  private double maxSimilarity = 0.0;
  private DataModel renameData;
  private String visitingTypeName = null;
  private String packageName = "";
  private String typeName = "";
  private String methodName = "";

  public FieldRefactorVisitor() {
    super();
    result = new ResultModel();
  }

  public void setRenameData(DataModel renameData) {
    this.renameData = renameData;
  }

  public ResultModel getResult() {
    return result;
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    this.packageName = node.getName().toString();
    return true;
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    if (visitingTypeName != null) return false;
    this.typeName = node.getName().toString();
    ITypeBinding typeBinding = node.resolveBinding();
    if (typeBinding != null) {
      visitingTypeName = node.getName().toString();
      return true;
    }
    return false;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    if (visitingTypeName != null && visitingTypeName.equals(node.getName().toString())) {
      visitingTypeName = null;
    }
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    Object obj = node.fragments().get(0);
    if (obj instanceof VariableDeclarationFragment) {
      String candidateName = ((VariableDeclarationFragment) obj).getName().toString();
      double similarity = Similarity.calculateSim(renameData, candidateName);
      if (similarity > maxSimilarity
          || (similarity == maxSimilarity
              && maxSimilarity > 0.0
              && Similarity.isMoreSim(candidateName, result, renameData))) {
        maxSimilarity = similarity;
        if (maxSimilarity >= Config.getMIN_RECOMMEND_SIMILARITY()) {
          String suggestion = Recommend.detect(renameData, candidateName);

          SimpleName simpleName = ((VariableDeclarationFragment) obj).getName();
          result.setRecommendStartPosition(simpleName.getStartPosition());
          result.setRecommendPackageName(this.packageName);
          result.setRecommendTypeName(this.typeName);
          result.setRecommendOriginalName(candidateName);
          result.setRecommendSubsequentName(suggestion);
          result.setRecommendRefactorType("field");
          result.setSim(maxSimilarity);
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    IMethodBinding iMethod = node.resolveBinding();
    if (iMethod == null || iMethod.getJavaElement() == null) {
      return false;
    }
    this.methodName =
        iMethod
            .getJavaElement()
            .toString()
            .substring(0, iMethod.getJavaElement().toString().indexOf("{") - 1);
    String candidateName = node.getName().toString();

    double similarity = Similarity.calculateSim(renameData, candidateName);
    if (similarity > maxSimilarity
        || (similarity == maxSimilarity
            && maxSimilarity > 0.0
            && Similarity.isMoreSim(candidateName, result, renameData))) {
      if (!Util.isOverriding(iMethod, renameData)) {
        maxSimilarity = similarity;
        if (maxSimilarity >= Config.getMIN_RECOMMEND_SIMILARITY()) {
          String suggestion = Recommend.detect(renameData, candidateName);

          SimpleName simpleName = node.getName();
          result.setRecommendStartPosition(simpleName.getStartPosition());
          result.setRecommendPackageName(this.packageName);
          result.setRecommendTypeName(this.typeName);
          result.setRecommendOriginalName(candidateName);
          result.setRecommendSubsequentName(suggestion);
          result.setRecommendRefactorType("method");
          result.setSim(maxSimilarity);
        }
      }
    }

    return true;
  }
}
