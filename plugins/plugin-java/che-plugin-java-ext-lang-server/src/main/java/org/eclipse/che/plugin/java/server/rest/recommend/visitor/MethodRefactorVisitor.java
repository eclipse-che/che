package org.eclipse.che.plugin.java.server.rest.recommend.visitor;

import org.eclipse.che.plugin.java.server.rest.recommend.common.Config;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.model.ResultModel;
import org.eclipse.che.plugin.java.server.rest.recommend.similarity.Recommend;
import org.eclipse.che.plugin.java.server.rest.recommend.similarity.Similarity;
import org.eclipse.che.plugin.java.server.rest.recommend.util.Util;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class MethodRefactorVisitor extends ASTVisitor {

  private ResultModel result;
  private double maxSimilarity = 0.0;
  private DataModel renameData;

  private String visitingTypeName = null;
  private boolean isMethodSelf = false;

  private String packageName = "";
  private String typeName = "";
  private String methodName = "";

  public MethodRefactorVisitor() {
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
    String candidateName = node.getName().toString();

    double similarity = Similarity.calculateSim(renameData, candidateName);
    if (similarity > maxSimilarity
        || (similarity == maxSimilarity
            && maxSimilarity > 0.0
            && Similarity.isMoreSim(candidateName, result, renameData))) {
      maxSimilarity = similarity;
      if (maxSimilarity >= Config.getMIN_RECOMMEND_SIMILARITY()) {
        String suggestion = Recommend.detect(renameData, candidateName);

        SimpleName simpleName = node.getName();
        result.setRecommendStartPosition(simpleName.getStartPosition());
        result.setRecommendPackageName(this.packageName);
        result.setRecommendTypeName(this.typeName);
        result.setRecommendOriginalName(candidateName);
        result.setRecommendSubsequentName(suggestion);
        result.setRecommendRefactorType("type");
        result.setSim(maxSimilarity);
      }
    }

    visitingTypeName = node.getName().toString();
    return true;
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
    if (this.methodName.equals(renameData.getMethodName())) {
      isMethodSelf = true;
    }
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
          result.setRecommendRefactorType("localVariable");
          result.setSim(maxSimilarity);
        }
      }
    }
    if (isMethodSelf) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    isMethodSelf = false;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {

    String candidateName = node.getName().toString();
    double similarity = Similarity.calculateSim(renameData, candidateName);
    if (similarity > maxSimilarity
        || (similarity == maxSimilarity
            && maxSimilarity > 0.0
            && Similarity.isMoreSim(candidateName, result, renameData))) {
      maxSimilarity = similarity;
      if (maxSimilarity >= Config.getMIN_RECOMMEND_SIMILARITY()) {
        String suggestion = Recommend.detect(renameData, candidateName);

        SimpleName simpleName = node.getName();
        result.setRecommendStartPosition(simpleName.getStartPosition());
        result.setRecommendPackageName(this.packageName);
        result.setRecommendTypeName(this.typeName);
        result.setRecommendOriginalName(candidateName);
        result.setRecommendSubsequentName(suggestion);
        result.setRecommendRefactorType("localVariable");
        result.setSim(maxSimilarity);
      }
    }
    return true;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {

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
          result.setRecommendRefactorType("localVariable");
          result.setSim(maxSimilarity);
        }
      }
    }

    return true;
  }

  @Override
  public boolean visit(MethodInvocation node) {

    String candidateName = node.getName().toString();
    IMethodBinding iMethod = node.resolveMethodBinding();
    if (iMethod == null) return false;
    ITypeBinding iType = iMethod.getDeclaringClass();
    if (iType == null) return false;
    if (!iType.isFromSource()) return false;
    String packageName = "";
    if (iType.getPackage() != null && iType.getPackage().getName() != null) {
      packageName = iType.getPackage().getName();
    }
    String typeName = iMethod.getDeclaringClass().getName();
    if (iMethod.getJavaElement() == null) return false;
    double similarity = Similarity.calculateSim(renameData, candidateName);
    if (similarity > maxSimilarity
        || (similarity == maxSimilarity
            && maxSimilarity > 0.0
            && Similarity.isMoreSim(candidateName, result, renameData))) {

      if (!Util.isOverriding(iMethod, renameData)) {
        maxSimilarity = similarity;
        if (maxSimilarity >= Config.getMIN_RECOMMEND_SIMILARITY()) {
          String suggestion = Recommend.detect(renameData, candidateName);

          result.setRecommendStartPosition(
              ((MethodDeclaration) iMethod.getMethodDeclaration()).getStartPosition());
          result.setRecommendPackageName(packageName);
          result.setRecommendTypeName(typeName);
          result.setRecommendOriginalName(candidateName);
          result.setRecommendSubsequentName(suggestion);
          result.setRecommendRefactorType("method");
          result.setSim(maxSimilarity);
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(FieldAccess node) {

    if (!(node.getExpression() instanceof ThisExpression)) {
      String candidateName = node.getName().toString();
      ITypeBinding iType = node.resolveTypeBinding();
      if (!iType.isFromSource()) return true;
      String packageName = iType.getPackage().getName();
      String typeName = iType.getName();

      double similarity = Similarity.calculateSim(renameData, candidateName);
      if (similarity > maxSimilarity
          || (similarity == maxSimilarity
              && maxSimilarity > 0.0
              && Similarity.isMoreSim(candidateName, result, renameData))) {
        maxSimilarity = similarity;
        if (maxSimilarity >= Config.getMIN_RECOMMEND_SIMILARITY()) {
          String suggestion = Recommend.detect(renameData, candidateName);

          SimpleName simpleName = node.getName();
          result.setRecommendStartPosition(simpleName.getStartPosition());
          result.setRecommendPackageName(packageName);
          result.setRecommendTypeName(typeName);
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
