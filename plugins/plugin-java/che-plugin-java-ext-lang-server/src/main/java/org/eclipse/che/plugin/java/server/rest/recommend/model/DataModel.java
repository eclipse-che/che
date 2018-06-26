package org.eclipse.che.plugin.java.server.rest.recommend.model;

import org.eclipse.jdt.core.IJavaElement;

public class DataModel {
  public String projectName = "";
  public String packageName = "";
  public String typeName = "";
  public String methodName = "";
  public String refactorType = "";
  public String originalName = "";
  public String subsequentName = "";
  public IJavaElement javaElement = null;
}
