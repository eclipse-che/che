package org.eclipse.che.plugin.java.server.rest.recommend.model;

public class ResultModel {

  private String renameOriginalName = "";

  private String recommendRefactorType = "";
  private String recommendOriginalName = "";
  private String recommendProjectName = "";
  private String recommendPackageName = "";
  private String recommendTypeName = "";
  private int recommendStartPosition = 0;

  private String recommendSubsequentName = "";

  private double sim = 0.0;

  public void setRecommendOriginalName(String recommendOriginalName) {
    this.recommendOriginalName = recommendOriginalName;
  }

  public void setRecommendPackageName(String recommendPackageName) {
    this.recommendPackageName = recommendPackageName;
  }

  public void setRecommendProjectName(String recommendProjectName) {
    this.recommendProjectName = recommendProjectName;
  }

  public void setRecommendRefactorType(String recommendRefactorType) {
    this.recommendRefactorType = recommendRefactorType;
  }

  public void setRecommendStartPosition(int recommendStartPosition) {
    this.recommendStartPosition = recommendStartPosition;
  }

  public void setRecommendSubsequentName(String recommendSubsequentName) {
    this.recommendSubsequentName = recommendSubsequentName;
  }

  public void setRecommendTypeName(String recommendTypeName) {
    this.recommendTypeName = recommendTypeName;
  }

  public void setRenameOriginalName(String renameOriginalName) {
    this.renameOriginalName = renameOriginalName;
  }

  public void setSim(double sim) {
    this.sim = sim;
  }

  public double getSim() {
    return sim;
  }

  public int getRecommendStartPosition() {
    return recommendStartPosition;
  }

  public String getRecommendOriginalName() {
    return recommendOriginalName;
  }

  public String getRecommendRefactorType() {
    return recommendRefactorType;
  }

  public String getRecommendSubsequentName() {
    return recommendSubsequentName;
  }

  public String getRenameOriginalName() {
    return renameOriginalName;
  }
}
