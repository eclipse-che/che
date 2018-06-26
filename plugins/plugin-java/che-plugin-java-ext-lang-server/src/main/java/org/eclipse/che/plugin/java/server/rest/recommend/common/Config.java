package org.eclipse.che.plugin.java.server.rest.recommend.common;

public class Config {
  private static final double MIN_SAME_WORD_SIMILARITY = 1.0;
  private static final double MIN_RECOMMEND_SIMILARITY = 0.0;

  public static double getMIN_SAME_WORD_SIMILARITY() {
    return MIN_SAME_WORD_SIMILARITY;
  }

  public static double getMIN_RECOMMEND_SIMILARITY() {
    return MIN_RECOMMEND_SIMILARITY;
  }
}
