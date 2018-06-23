package org.eclipse.che.plugin.java.server.rest.recommend.model;

import org.eclipse.core.resources.IFile;

public class ResultModel {

  public IFile renameIFile = null;
  public int renameLine = 0;
  public String renameOriginalName = "";

  public String recommendRefactorType = "";
  public String recommendOriginalName = "";
  public String recommendProjectName = "";
  public String recommendPackageName = "";
  public String recommendTypeName = "";
  public int recommendStartPosition = 0;
  public int recommendLine = 0;

  public String recommendSubsequentName = "";

  public double sim = 0.0;
}
