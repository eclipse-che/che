package org.eclipse.che.plugin.java.server.rest.recommend.model;

import org.eclipse.jdt.core.IJavaElement;

public class DataModel {
  private String projectName;
  private String packageName;
  private String typeName;
  private String methodName;
  private String refactorType;
  private String originalName;
  private String subsequentName;
  private IJavaElement javaElement;

  public DataModel() {
    projectName = "";
    packageName = "";
    typeName = "";
    methodName = "";
    refactorType = "";
    originalName = "";
    subsequentName = "";
  }

  public void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public void setRefactorType(String refactorType) {
    this.refactorType = refactorType;
  }

  public void setOriginalName(String originalName) {
    this.originalName = originalName;
  }

  public void setSubsequentName(String subsequentName) {
    this.subsequentName = subsequentName;
  }

  public void setJavaElement(IJavaElement javaElement) {
    this.javaElement = javaElement;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getRefactorType() {
    return refactorType;
  }

  public String getOriginalName() {
    return originalName;
  }

  public String getSubsequentName() {
    return subsequentName;
  }
}
