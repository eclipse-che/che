package org.eclipse.che.plugin.java.server.rest.recommend.visitor;

import java.util.List;
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

  public String projectName = null;
  public List<ResultModel> results = null;
  public ResultModel result = null;
  public double maxSimilarity = 0.0;
  public DataModel renameData = null;
  public String visitingTypeName = null;
  public boolean isTypeSelf = false;
  public boolean isSuperOrSub = false;
  public boolean isSamePackage = false;
  public ITypeBinding typeBinding = null;
  public ITypeBinding[] superInterfaces = null;
  public ITypeBinding superClass = null;
  public String originalPackageName = "";
  public String packageName = "";
  public String typeName = "";
  public String methodName = "";

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
        if (maxSimilarity >= Config.MIN_RECOMMEND_SIMILARITY) {
          String suggestion = Recommend.detect(renameData, candidateName);

          SimpleName simpleName = ((VariableDeclarationFragment) obj).getName();
          result.recommendStartPosition = simpleName.getStartPosition();
          result.recommendPackageName = this.packageName;
          result.recommendTypeName = this.typeName;
          result.recommendOriginalName = candidateName;
          result.recommendSubsequentName = suggestion;
          result.recommendRefactorType = "field";
          result.sim = maxSimilarity;
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
        if (maxSimilarity >= Config.MIN_RECOMMEND_SIMILARITY) {
          String suggestion = Recommend.detect(renameData, candidateName);

          SimpleName simpleName = node.getName();
          result.recommendStartPosition = simpleName.getStartPosition();
          result.recommendPackageName = this.packageName;
          result.recommendTypeName = this.typeName;
          result.recommendOriginalName = candidateName;
          result.recommendSubsequentName = suggestion;
          result.recommendRefactorType = "method";
          result.sim = maxSimilarity;
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
      if (maxSimilarity >= Config.MIN_RECOMMEND_SIMILARITY) {
        String suggestion = Recommend.detect(renameData, candidateName);

        SimpleName simpleName = node.getName();
        result.recommendStartPosition = simpleName.getStartPosition();
        result.recommendPackageName = packageName;
        result.recommendTypeName = typeName;
        result.recommendOriginalName = candidateName;
        result.recommendSubsequentName = suggestion;
        result.recommendRefactorType = "type";
        result.sim = maxSimilarity;
      }
    }
    return false;
  }
}
