package org.eclipse.che.plugin.java.server.rest.recommend.similarity;

public class LevenshteinDistance {
  private static int minimum(int a, int b, int c) {
    return Math.min(Math.min(a, b), c);
  }

  public static int computeLevenshteinDistance(String str1, String str2) {
    int[][] distance = new int[str1.length() + 1][str2.length() + 1];

    for (int i = 0; i <= str1.length(); i++) distance[i][0] = i;
    for (int j = 1; j <= str2.length(); j++) distance[0][j] = j;

    for (int i = 1; i <= str1.length(); i++)
      for (int j = 1; j <= str2.length(); j++)
        distance[i][j] =
            minimum(
                distance[i - 1][j] + 1,
                distance[i][j - 1] + 1,
                distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

    return distance[str1.length()][str2.length()];
  }
}
