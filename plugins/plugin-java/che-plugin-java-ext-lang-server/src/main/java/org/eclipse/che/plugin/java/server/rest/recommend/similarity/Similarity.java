package org.eclipse.che.plugin.java.server.rest.recommend.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.plugin.java.server.rest.recommend.common.Config;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;
import org.eclipse.che.plugin.java.server.rest.recommend.model.ResultModel;

public class Similarity {

  public static double calculateSim(
      DataModel renameData, List<DataModel> ignoreRenameDataList, String candidateName) {

    double totalSim = 0.0;
    for (DataModel ignorerenameData : ignoreRenameDataList) {
      totalSim = totalSim + calculateSim(ignorerenameData, candidateName);
    }

    double sim = totalSim / (1.0 * ignoreRenameDataList.size());
    return sim;
  }

  public static double calculateSim(DataModel ignorerenameData, String candidateName) {

    String originalName = ignorerenameData.getOriginalName();
    String subsequentName = ignorerenameData.getSubsequentName();

    String[] originalNames = Word.splitWords(originalName);
    String[] originalNamesDiff = Word.splitWordsToLowerCase(originalName);
    String[] subsequentNames = Word.splitWords(subsequentName);
    String[] subsequentNamesDiff = Word.splitWordsToLowerCase(subsequentName);
    String[] candidateNames = Word.splitWords(candidateName);

    List diffOut = getDiff(originalNamesDiff, subsequentNamesDiff);
    if (diffOut == null || diffOut.size() == 0) return 0.0;
    String[] splitOriginalNames = splitOriginalName(diffOut, originalNames);
    String[] splitCandidateNames = splitCandidateName(diffOut, originalNames, candidateNames);
    double sim = calculateSim(splitOriginalNames, splitCandidateNames);
    return sim;
  }

  private static double calculateSim(String[] splitOriginalNames, String[] splitCandidateNames) {
    if (splitOriginalNames == null || splitCandidateNames == null) return 0;
    if (splitOriginalNames.length == 0 || splitCandidateNames.length == 0) return 0;
    double totalSimilarity = 0.0;
    for (int i = 0; i < splitOriginalNames.length; i++) {
      totalSimilarity =
          totalSimilarity + calculateSim(splitOriginalNames[i], splitCandidateNames[i]);
    }
    double sim = totalSimilarity / (1.0 * splitOriginalNames.length);
    return sim;
  }

  private static double calculateSim(String splitOriginalName, String splitCandidateName) {
    if (splitOriginalName.equals("") && splitCandidateName.equals("")) return 1.0;
    if (splitOriginalName.equals("")) return 0.0;
    if (splitCandidateName.equals("")) return 0.0;
    String[] splitOriginalNames = Word.splitWordsToLowerCase(splitOriginalName);
    String[] splitCandidateNames = Word.splitWordsToLowerCase(splitCandidateName);
    int originalLength = splitOriginalNames.length;
    int candidateLength = splitCandidateNames.length;
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < originalLength; i++) {
      set.add(splitOriginalNames[i]);
    }

    for (int i = 0; i < candidateLength; i++) {
      set.add(splitCandidateNames[i]);
    }
    double sim =
        2
            * (originalLength + candidateLength - set.size())
            / (1.0 * (originalLength + candidateLength));
    return sim;
  }

  public static List getDiff(String[] originalNames, String[] subsequentNames) {

    Diff diff = new Diff(originalNames, subsequentNames);
    List diffOut = diff.diff();
    return diffOut;
  }

  public static String[] splitOriginalName(List diffOut, String[] originalNames) {
    int length = diffOut.size();
    String[] splitOriginalNames = new String[length + 1];
    int startPos = 0;
    for (int i = 0; i < length; i++) {
      Difference diff = (Difference) diffOut.get(i);
      String str = "";
      if (diff.getAddedEnd() == -1) {
        for (int j = startPos; j <= diff.getDeletedEnd(); j++) {
          str = str + originalNames[j];
        }
        splitOriginalNames[i] = str;
        startPos = diff.getDeletedEnd() + 1;
      } else if (diff.getDeletedEnd() == -1) {
        for (int j = startPos; j < diff.getDeletedStart(); j++) {
          str = str + originalNames[j];
        }
        splitOriginalNames[i] = str;
        startPos = diff.getDeletedStart();
      } else {
        for (int j = startPos; j <= diff.getDeletedEnd(); j++) {
          str = str + originalNames[j];
        }
        splitOriginalNames[i] = str;
        startPos = diff.getDeletedEnd() + 1;
      }
    }

    if (startPos < originalNames.length) {
      String str = "";
      for (int j = startPos; j < originalNames.length; j++) {
        str = str + originalNames[j];
      }
      splitOriginalNames[length] = str;
    } else {
      splitOriginalNames[length] = "";
    }

    return splitOriginalNames;
  }

  public static String[] splitCandidateName(
      List diffOut, String[] originalNames, String[] candidateNames) {
    int length = diffOut.size();
    String[] splitCandidateNames = new String[length + 1];
    double maxSim = 0.0;
    List[] matchPointLists = getMatchPointList(diffOut, originalNames, candidateNames);

    List<String[]> splitCandidateNamesList =
        getSplitCandidateNamesList(candidateNames, matchPointLists);
    String[] splitOriginalName = splitOriginalName(diffOut, originalNames);
    if (splitCandidateNamesList == null || splitCandidateNamesList.size() == 0) return null;
    for (String[] splitCandidateName : splitCandidateNamesList) {
      double sim = calculateSim(splitOriginalName, splitCandidateName);
      if (sim > maxSim) {
        maxSim = sim;
        splitCandidateNames = splitCandidateName;
      }
    }
    return splitCandidateNames;
  }

  private static List[] getMatchPointList(
      List diffOut, String[] originalNames, String[] candidateNames) {
    int length = diffOut.size();
    List[] matchPointLists = new ArrayList[length];

    for (int i = 0; i < length; i++) {
      Difference diff = (Difference) diffOut.get(i);
      matchPointLists[i] = new ArrayList();
      if (diff.getAddedEnd() == -1) {
        int start = diff.getDeletedStart();
        int end = diff.getDeletedEnd();
        int len = end - start + 1;

        for (int j = 0; j < candidateNames.length - len + 1; j++) {
          for (int h = j, k = start; h < candidateNames.length && k <= end; k++, h++) {
            if (!candidateNames[h].equalsIgnoreCase(originalNames[k])) break;
            if (h < candidateNames.length && k == end) {
              if (j == 0 || start == 0) {
                if (j + len < candidateNames.length
                    && end + 1 < originalNames.length
                    && candidateNames[j + len].equalsIgnoreCase(originalNames[end + 1])) {
                  matchPointLists[i].add(j + len - 1);
                }
              } else if (j == candidateNames.length - len || end == originalNames.length - 1) {
                if (j - 1 >= 0
                    && start - 1 >= 0
                    && candidateNames[j - 1].equalsIgnoreCase(originalNames[start - 1])) {
                  matchPointLists[i].add(j + len - 1);
                }
              } else {
                if (candidateNames[j - 1].equalsIgnoreCase(originalNames[start - 1])
                    || candidateNames[j + len].equalsIgnoreCase(originalNames[end + 1])) {
                  matchPointLists[i].add(j + len - 1);
                }
              }
            }
          }
        }
      } else if (diff.getDeletedEnd() == -1) {
        int start = diff.getDeletedStart();
        for (int j = 0; j <= candidateNames.length; j++) {
          if (j == 0 || start == 0) {
            if (j == 0 && start == 0 && candidateNames[j].equalsIgnoreCase(originalNames[start])) {
              matchPointLists[i].add(j - 1);
            }
          } else if (j == candidateNames.length || start == originalNames.length) {
            if (j == candidateNames.length
                && start == originalNames.length
                && candidateNames[j - 1].equalsIgnoreCase(originalNames[start - 1])) {
              matchPointLists[i].add(j - 1);
            }
          } else {
            if (candidateNames[j].equalsIgnoreCase(originalNames[start])
                && candidateNames[j - 1].equalsIgnoreCase(originalNames[start - 1])) {
              matchPointLists[i].add(j - 1);
            }
          }
        }
      } else {
        int start = diff.getDeletedStart();
        int end = diff.getDeletedEnd();
        int len = end - start + 1;

        for (int j = 0; j < candidateNames.length - len + 1; j++) {
          for (int h = j, k = start; h < candidateNames.length && k <= end; k++, h++) {
            if (!candidateNames[h].equalsIgnoreCase(originalNames[k])) break;
            if (h < candidateNames.length && k == end) {
              if (j == 0 || start == 0) {
                if (j + len < candidateNames.length
                    && end + 1 < originalNames.length
                    && candidateNames[j + len].equalsIgnoreCase(originalNames[end + 1])) {
                  matchPointLists[i].add(j + len - 1);
                }
              } else if (j == candidateNames.length - len || end == originalNames.length - 1) {
                if (j - 1 >= 0
                    && start - 1 >= 0
                    && candidateNames[j - 1].equalsIgnoreCase(originalNames[start - 1])) {
                  matchPointLists[i].add(j + len - 1);
                }
              } else {
                if (candidateNames[j - 1].equalsIgnoreCase(originalNames[start - 1])
                    || candidateNames[j + len].equalsIgnoreCase(originalNames[end + 1])) {
                  matchPointLists[i].add(j + len - 1);
                }
              }
            }
          }
        }
      }
    }
    return matchPointLists;
  }

  private static List<String[]> getSplitCandidateNamesList(
      String[] candidateNames, List[] matchPointLists) {
    List<String[]> splitCandidateNamesList = new ArrayList<String[]>();

    if (matchPointLists == null || matchPointLists.length == 0) return null;
    int length = matchPointLists.length;

    if (length == 1) {
      String[] splitCandidateNames = new String[length + 1];
      if (matchPointLists[0] == null || matchPointLists[0].size() == 0) return null;
      for (int i = 0; i < matchPointLists[0].size(); i++) {
        int matchPoint0 = (Integer) matchPointLists[0].get(i);
        int start0 = 0;
        String str0 = "";
        if (matchPoint0 == -1) {
          splitCandidateNames[0] = "";
          for (int h = start0; h < candidateNames.length; h++) {
            str0 = str0 + candidateNames[h];
          }
          splitCandidateNames[1] = str0;
        } else if (matchPoint0 == candidateNames.length) {
          for (int h = start0; h < matchPoint0; h++) {
            str0 = str0 + candidateNames[h];
          }
          splitCandidateNames[0] = str0;
          splitCandidateNames[1] = "";
        } else {
          for (int h = start0; h <= matchPoint0; h++) {
            str0 = str0 + candidateNames[h];
          }
          splitCandidateNames[0] = str0;
          String str1 = "";
          for (int h = matchPoint0 + 1; h < candidateNames.length; h++) {
            str1 = str1 + candidateNames[h];
          }
          splitCandidateNames[1] = str1;
        }
        String[] splitCandidate = new String[length + 1];
        for (int p = 0; p < length + 1; p++) {
          splitCandidate[p] = splitCandidateNames[p];
        }
        splitCandidateNamesList.add(splitCandidate);
      }
    }

    if (length == 2) {
      String[] splitCandidateNames = new String[length + 1];

      if (matchPointLists[0] == null || matchPointLists[0].size() == 0) return null;
      for (int i = 0; i < matchPointLists[0].size(); i++) {
        int matchPoint0 = (Integer) matchPointLists[0].get(i);
        int start0 = 0;
        String str0 = "";
        if (matchPoint0 == -1) {
          splitCandidateNames[0] = "";
        } else if (matchPoint0 == candidateNames.length) {
          continue;
        } else {
          for (int h = start0; h <= matchPoint0; h++) {
            str0 = str0 + candidateNames[h];
          }
          splitCandidateNames[0] = str0;
        }

        if (matchPointLists[1] == null || matchPointLists[1].size() == 0) return null;
        for (int j = 0; j < matchPointLists[1].size(); j++) {
          int matchPoint1 = (Integer) matchPointLists[1].get(j);
          int start1 = matchPoint0 + 1;
          String str1 = "";
          if (matchPoint1 < start1) {
            continue;
          } else if (matchPoint1 == candidateNames.length) {
            for (int h = start1; h < matchPoint1; h++) {
              str1 = str1 + candidateNames[h];
            }
            splitCandidateNames[1] = str1;
            splitCandidateNames[2] = "";
          } else {
            for (int h = start1; h <= matchPoint1; h++) {
              str1 = str1 + candidateNames[h];
            }
            splitCandidateNames[1] = str1;
            String str2 = "";
            for (int h = matchPoint1 + 1; h < candidateNames.length; h++) {
              str2 = str2 + candidateNames[h];
            }
            splitCandidateNames[2] = str2;
          }
          String[] splitCandidate = new String[length + 1];
          for (int p = 0; p < length + 1; p++) {
            splitCandidate[p] = splitCandidateNames[p];
          }
          splitCandidateNamesList.add(splitCandidate);
        }
      }
    }

    if (length == 3) {
      String[] splitCandidateNames = new String[length + 1];

      if (matchPointLists[0] == null || matchPointLists[0].size() == 0) return null;
      for (int i = 0; i < matchPointLists[0].size(); i++) {
        int matchPoint0 = (Integer) matchPointLists[0].get(i);
        int start0 = 0;
        String str0 = "";
        if (matchPoint0 == -1) {
          splitCandidateNames[0] = "";
        } else if (matchPoint0 == candidateNames.length) {
          continue;
        } else {
          for (int h = start0; h <= matchPoint0; h++) {
            str0 = str0 + candidateNames[h];
          }
          splitCandidateNames[0] = str0;
        }

        if (matchPointLists[1] == null || matchPointLists[1].size() == 0) return null;
        for (int j = 0; j < matchPointLists[1].size(); j++) {
          int matchPoint1 = (Integer) matchPointLists[1].get(j);
          int start1 = matchPoint0 + 1;
          String str1 = "";
          if (matchPoint1 < start1) {
            continue;
          } else if (matchPoint1 == candidateNames.length) {
            continue;
          } else {
            for (int h = start1; h <= matchPoint1; h++) {
              str1 = str1 + candidateNames[h];
            }
            splitCandidateNames[1] = str1;
          }

          if (matchPointLists[2] == null || matchPointLists[2].size() == 0) return null;
          for (int k = 0; k < matchPointLists[2].size(); k++) {
            int matchPoint2 = (Integer) matchPointLists[2].get(k);
            int start2 = matchPoint1 + 1;
            String str2 = "";
            if (matchPoint2 < start2) {
              continue;
            } else if (matchPoint2 == candidateNames.length) {
              for (int h = start2; h < matchPoint2; h++) {
                str2 = str2 + candidateNames[h];
              }
              splitCandidateNames[2] = str2;
              splitCandidateNames[3] = "";
            } else {
              for (int h = start2; h <= matchPoint2; h++) {
                str2 = str2 + candidateNames[h];
              }
              splitCandidateNames[2] = str2;
              String str3 = "";
              for (int h = matchPoint2 + 1; h < candidateNames.length; h++) {
                str3 = str3 + candidateNames[h];
              }
              splitCandidateNames[3] = str3;
            }
            String[] splitCandidate = new String[length + 1];
            for (int p = 0; p < length + 1; p++) {
              splitCandidate[p] = splitCandidateNames[p];
            }
            splitCandidateNamesList.add(splitCandidate);
          }
        }
      }
    }

    if (length == 4) {
      String[] splitCandidateNames = new String[length + 1];

      if (matchPointLists[0] == null || matchPointLists[0].size() == 0) return null;
      for (int i = 0; i < matchPointLists[0].size(); i++) {
        int matchPoint0 = (Integer) matchPointLists[0].get(i);
        int start0 = 0;
        String str0 = "";
        if (matchPoint0 == -1) {
          splitCandidateNames[0] = "";
        } else if (matchPoint0 == candidateNames.length) {
          continue;
        } else {
          for (int h = start0; h <= matchPoint0; h++) {
            str0 = str0 + candidateNames[h];
          }
          splitCandidateNames[0] = str0;
        }

        if (matchPointLists[1] == null || matchPointLists[1].size() == 0) return null;
        for (int j = 0; j < matchPointLists[1].size(); j++) {
          int matchPoint1 = (Integer) matchPointLists[1].get(j);
          int start1 = matchPoint0 + 1;
          String str1 = "";
          if (matchPoint1 < start1) {
            continue;
          } else if (matchPoint1 == candidateNames.length) {
            continue;
          } else {
            for (int h = start1; h <= matchPoint1; h++) {
              str1 = str1 + candidateNames[h];
            }
            splitCandidateNames[1] = str1;
          }

          if (matchPointLists[2] == null || matchPointLists[2].size() == 0) return null;
          for (int k = 0; k < matchPointLists[2].size(); k++) {
            int matchPoint2 = (Integer) matchPointLists[2].get(k);
            int start2 = matchPoint1 + 1;
            String str2 = "";
            if (matchPoint2 < start2) {
              continue;
            } else if (matchPoint2 == candidateNames.length) {
              continue;
            } else {
              for (int h = start2; h <= matchPoint2; h++) {
                str2 = str2 + candidateNames[h];
              }
              splitCandidateNames[2] = str2;
            }

            if (matchPointLists[3] == null || matchPointLists[3].size() == 0) return null;
            for (int l = 0; l < matchPointLists[3].size(); l++) {
              int matchPoint3 = (Integer) matchPointLists[3].get(l);
              int start3 = matchPoint2 + 1;
              String str3 = "";
              if (matchPoint3 < start3) {
                continue;
              } else if (matchPoint3 == candidateNames.length) {
                for (int h = start3; h < matchPoint3; h++) {
                  str2 = str3 + candidateNames[h];
                }
                splitCandidateNames[3] = str3;
                splitCandidateNames[4] = "";
              } else {
                for (int h = start3; h <= matchPoint3; h++) {
                  str3 = str3 + candidateNames[h];
                }
                splitCandidateNames[3] = str3;
                String str4 = "";
                for (int h = matchPoint3 + 1; h < candidateNames.length; h++) {
                  str4 = str4 + candidateNames[h];
                }
                splitCandidateNames[4] = str4;
              }
              String[] splitCandidate = new String[length + 1];
              for (int p = 0; p < length + 1; p++) {
                splitCandidate[p] = splitCandidateNames[p];
              }
              splitCandidateNamesList.add(splitCandidate);
            }
          }
        }
      }
    }

    return splitCandidateNamesList;
  }

  public static boolean isMoreSim(ResultModel candidateResult, ResultModel result) {
    double candidateSim =
        calculateSimilary(
            candidateResult.getRenameOriginalName(), candidateResult.getRecommendOriginalName());
    double resultSim =
        calculateSimilary(result.getRenameOriginalName(), result.getRecommendOriginalName());
    if (candidateSim > resultSim) {
      return true;
    }
    return false;
  }

  public static boolean isMoreSim(String candidateName, ResultModel result, DataModel renameData) {
    double candidateSim = calculateSimilary(renameData.getOriginalName(), candidateName);
    double resultSim =
        calculateSimilary(renameData.getOriginalName(), result.getRecommendOriginalName());
    if (candidateSim > resultSim) {
      return true;
    }
    return false;
  }

  private static double calculateSimilary(String name1, String name2) {
    if (name1 == null || name2 == null) return 0.0;
    if (name1.equals("") && name2.equals("")) return 1.0;
    if (name1.equals("")) return 0.0;
    if (name2.equals("")) return 0.0;
    String[] splitOriginalNames = Word.splitWords(name1);
    String[] splitCandidateNames = Word.splitWords(name2);
    int originalLength = splitOriginalNames.length;
    int candidateLength = splitCandidateNames.length;
    Set<String> set = new HashSet<String>();
    for (int i = 0; i < originalLength; i++) {
      set.add(splitOriginalNames[i]);
    }

    for (int i = 0; i < candidateLength; i++) {
      set.add(splitCandidateNames[i]);
    }
    double sim =
        2
            * (originalLength + candidateLength - set.size())
            / (1.0 * (originalLength + candidateLength));
    return sim;
  }

  public static double calculate(
      DataModel renameData, List<DataModel> ignoreRenameDataList, String candidateName) {
    Double similarity = 0.0;

    Map<String, String[]> map = getCommonWords(ignoreRenameDataList);
    String[] commonA = map.get("commonA");
    String[] commonB = map.get("commonB");

    similarity = calculate(renameData, candidateName, commonA, commonB);

    return similarity;
  }

  private static double calculate(
      DataModel renameData, String candidateName, String[] commonA, String[] commonB) {
    String originalName = renameData.getOriginalName();
    String subsequentName = renameData.getSubsequentName();
    return calculate(originalName, subsequentName, candidateName, commonA, commonB);
  }

  private static double calculate(
      String originalName,
      String subsequentName,
      String candidateName,
      String[] commonA,
      String[] commonB) {
    Map<String, String> originalMap = Word.splitAXB(originalName, subsequentName);
    String x = originalMap.get("X");

    if (!candidateName.contains(x)) return 0.0;
    Map<String, String> candidateMap = Word.splitCXD(originalName, subsequentName, candidateName);
    String c = candidateMap.get("C");
    String d = candidateMap.get("D");

    String[] cWords = Word.splitWordsToLowerCase(c);
    String[] dWords = Word.splitWordsToLowerCase(d);

    if (x.equals("")) {
      Word.reserved(commonA);
      Word.reserved(cWords);
      double frontSimilarity = calculate(commonA, cWords);
      Word.reserved(commonA);
      Word.reserved(cWords);
      double backSimilarity = calculate(commonB, dWords);
      if (frontSimilarity == 0.0 || backSimilarity == 0.0) {
        return 0.0;
      }
    }

    Word.reserved(commonA);
    Word.reserved(cWords);
    double frontSimilarity = calculate(commonA, cWords);
    Word.reserved(commonA);
    Word.reserved(cWords);

    double backSimilarity = calculate(commonB, dWords);
    double similarity = (frontSimilarity + backSimilarity) / 2;

    return similarity;
  }

  public static double calculate(String a, String b, String c, String d) {

    String[] aWords = Word.splitWordsToLowerCase(a);
    String[] bWords = Word.splitWordsToLowerCase(b);
    String[] cWords = Word.splitWordsToLowerCase(c);
    String[] dWords = Word.splitWordsToLowerCase(d);

    Word.reserved(aWords);
    Word.reserved(cWords);
    double frontSimilarity = calculate(aWords, cWords);
    Word.reserved(aWords);
    Word.reserved(cWords);

    double backSimilarity = calculate(bWords, dWords);
    if (frontSimilarity == 0.0 || backSimilarity == 0.0) return 0.0;
    double similarity = (frontSimilarity + backSimilarity) / 2.0;

    return similarity;
  }

  private static double calculate(String[] originalWords, String[] candidateWords) {
    if (originalWords.length == 0 || candidateWords.length == 0) return 0;
    double totalSimilarity = 0.0;
    int startPos = 0;
    for (int i = 0; i < originalWords.length; i++) {
      for (int j = startPos; j < candidateWords.length; j++) {
        if (wordEqual(originalWords[i], candidateWords[j])) {
          totalSimilarity = totalSimilarity + Math.pow(2, -i) * Math.pow(2, -(j - i));
          startPos = j + 1;
          break;
        }
        if (j == candidateWords.length - 1) {
          startPos = j + 1;
        }
      }
    }

    return totalSimilarity / (1.0 * originalWords.length);
    // return totalSimilarity / (1.0 *candidateWords.length);
  }

  private static boolean wordEqual(String word1, String word2) {
    int distance = LevenshteinDistance.computeLevenshteinDistance(word1, word2);
    int maxLength = Math.max(word1.length(), word2.length());
    return (1 - distance / (1.0 * maxLength)) >= Config.getMIN_SAME_WORD_SIMILARITY();
  }

  private static Map<String, String[]> getCommonWords(List<DataModel> ignoreRenameDataList) {
    Map<String, String[]> map = new HashMap<String, String[]>();

    String[] commonA = null;
    String[] commonB = null;
    boolean firstFlag = true;

    for (DataModel ignoreRenameData : ignoreRenameDataList) {
      String originalName = ignoreRenameData.getOriginalName();
      String subsequentName = ignoreRenameData.getSubsequentName();
      Map<String, String> originalMap = Word.splitAXB(originalName, subsequentName);
      String a = originalMap.get("A");
      String b = originalMap.get("B");
      String[] aWords = Word.splitWordsToLowerCase(a);
      String[] bWords = Word.splitWordsToLowerCase(b);

      int k = 0;
      int l = 0;
      if (firstFlag) {
        commonA = aWords;
        commonB = bWords;
        firstFlag = false;
      } else {
        int startPos = 0;
        for (int i = 0; i < commonA.length; i++) {
          for (int j = startPos; j < aWords.length; j++) {
            if (commonA[i].equals(aWords[j])) {
              k++;
              startPos = j + 1;
            }
          }
        }

        String[] tempA = new String[k];
        startPos = 0;
        k = 0;
        for (int i = 0; i < commonA.length; i++) {
          for (int j = startPos; j < aWords.length; j++) {
            if (commonA[i].equals(aWords[j])) {
              tempA[k] = commonA[i];
              k++;
              startPos = j + 1;
            }
          }
        }

        startPos = 0;
        for (int i = 0; i < commonB.length; i++) {
          for (int j = startPos; j < bWords.length; j++) {
            if (commonB[i].equals(bWords[j])) {
              l++;
              startPos = j + 1;
            }
          }
        }

        String[] tempB = new String[l];
        l = 0;
        startPos = 0;
        for (int i = 0; i < commonB.length; i++) {
          for (int j = startPos; j < bWords.length; j++) {
            if (commonB[i].equals(bWords[j])) {
              tempB[l] = commonB[i];
              l++;
              startPos = j + 1;
            }
          }
        }

        commonA = tempA;
        commonB = tempB;
      }
    }

    map.put("commonA", commonA);
    map.put("commonB", commonB);

    return map;
  }
}
