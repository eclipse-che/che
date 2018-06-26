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

public class TypeRefactorVisitor extends ASTVisitor {

  private ResultModel result;
  private double maxSimilarity = 0.0;
  private DataModel renameData;
  private String visitingTypeName = null;
  private boolean isTypeSelf = false;
  private boolean isSamePackage = false;
  private ITypeBinding typeBinding = null;
  private ITypeBinding[] superInterfaces = null;
  private ITypeBinding superClass = null;
  private String originalPackageName = "";
  private String packageName = "";
  private String typeName = "";
  private String methodName = "";

  public void setRenameData(DataModel renameData) {
    this.renameData = renameData;
  }

  public void setResult(ResultModel result) {
    this.result = result;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public void setOriginalPackageName(String originalPackageName) {
    this.originalPackageName = originalPackageName;
  }

  public void setSuperClass(ITypeBinding superClass) {
    this.superClass = superClass;
  }

  public void setSuperInterfaces(ITypeBinding[] superInterfaces) {
    this.superInterfaces = superInterfaces;
  }

  public void setTypeBinding(ITypeBinding typeBinding) {
    this.typeBinding = typeBinding;
  }

  public void setTypeSelf(boolean typeSelf) {
    isTypeSelf = typeSelf;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getOriginalPackageName() {
    return originalPackageName;
  }

  public ResultModel getResult() {
    return result;
  }

  public ITypeBinding getSuperClass() {
    return superClass;
  }

  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  public ITypeBinding[] getSuperInterfaces() {
    return superInterfaces;
  }

  public TypeRefactorVisitor() {
    super();
    result = new ResultModel();
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    if (isTypeSelf) {
      this.originalPackageName = node.getName().toString();
    }
    this.packageName = node.getName().toString();
    if (this.packageName.equals(this.originalPackageName)) {
      isSamePackage = true;
    }
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
      if (isTypeSelf) {
        this.typeBinding = typeBinding;
        this.superInterfaces = typeBinding.getInterfaces();
        this.superClass = typeBinding.getSuperclass();
      } else if (isSamePackage) {
        return handleTypeName(node);
      } else {
        ITypeBinding[] superInterfaces = typeBinding.getInterfaces();
        ITypeBinding superClass = typeBinding.getSuperclass();
        if (this.superClass != null
            && typeBinding.getQualifiedName().equals(this.superClass.getQualifiedName())) {
          return handleTypeName(node);
        } else if (Util.isContain(this.superInterfaces, typeBinding)) {
          return handleTypeName(node);
        } else if (this.typeBinding != null
            && superInterfaces != null
            && Util.isContain(superInterfaces, this.typeBinding)) {
          return handleTypeName(node);
        } else if (this.typeBinding != null
            && superClass != null
            && superClass.getQualifiedName() != null
            && !superClass.getQualifiedName().equals("java.lang.Object")
            && this.typeBinding.getQualifiedName().equals(superClass.getQualifiedName())) {
          return handleTypeName(node);
        } else if (this.superClass != null
            && this.superClass.getQualifiedName() != null
            && superClass != null
            && superClass.getQualifiedName() != null
            && !superClass.getQualifiedName().equals("java.lang.Object")
            && this.superClass.getQualifiedName().equals(superClass.getQualifiedName())) {
          return handleTypeName(node);
        } else if (Util.isContain(this.superInterfaces, superInterfaces)) {
          return handleTypeName(node);
        } else {
          return false;
        }
      }
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
    if (isTypeSelf == false) return false;
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

  private boolean handleTypeName(TypeDeclaration node) {
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
        result.setRecommendPackageName(packageName);
        result.setRecommendTypeName(typeName);
        result.setRecommendOriginalName(candidateName);
        result.setRecommendSubsequentName(suggestion);
        result.setRecommendRefactorType("type");
        result.setSim(maxSimilarity);
      }
    }
    return false;
  }
}
