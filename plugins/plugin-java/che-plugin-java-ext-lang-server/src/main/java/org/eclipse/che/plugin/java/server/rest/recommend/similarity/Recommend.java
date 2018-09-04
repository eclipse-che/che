package org.eclipse.che.plugin.java.server.rest.recommend.similarity;

import java.util.List;
import org.eclipse.che.plugin.java.server.rest.recommend.model.DataModel;

public class Recommend {

  public static String detect(
      DataModel renameData, List<DataModel> ignoreRenameDataList, String candidateName) {
    String suggest = "";
    suggest = detect(renameData, candidateName);
    return suggest;
  }

  public static String detect(DataModel renameData, String candidateName) {

    String originalName = renameData.getOriginalName();
    String subsequentName = renameData.getSubsequentName();
    return detect(originalName, subsequentName, candidateName);
  }

  public static String detect(String originalName, String subsequentName, String candidateName) {
    String[] originalNames = Word.splitWords(originalName);
    String[] originalNamesdiff = Word.splitWordsToLowerCase(originalName);
    String[] subsequentNames = Word.splitWords(subsequentName);
    String[] subsequentNamesDiff = Word.splitWordsToLowerCase(subsequentName);
    String[] candidateNames = Word.splitWords(candidateName);

    List diffOut = Similarity.getDiff(originalNamesdiff, subsequentNamesDiff);
    if (diffOut == null || diffOut.size() == 0) return null;

    String[] splitOriginalNames = Similarity.splitOriginalName(diffOut, originalNames);
    String[] splitCandidateNames =
        Similarity.splitCandidateName(diffOut, originalNames, candidateNames);
    if (splitOriginalNames == null || splitCandidateNames == null) return null;
    if (splitOriginalNames.length == 0 || splitCandidateNames.length == 0) return null;
    int length = diffOut.size();
    String suggest = "";
    int startPos = 0;
    for (int i = 0; i < length; i++) {
      Difference diff = (Difference) diffOut.get(i);
      String str = splitCandidateNames[i];
      if (diff.getAddedEnd() == -1) {
        String[] strs = Word.splitWords(str);
        int len = diff.getDeletedEnd() - diff.getDeletedStart() + 1;
        if (strs.length > len) {
          for (int j = 0; j < strs.length - len; j++) {
            suggest = suggest + strs[j];
          }
        }
      } else if (diff.getDeletedEnd() == -1) {
        suggest = suggest + str;
        for (int j = diff.getAddedStart(); j <= diff.getAddedEnd(); j++) {
          suggest = suggest + subsequentNames[j];
        }
      } else {
        String[] strs = Word.splitWords(str);
        int len = diff.getDeletedEnd() - diff.getDeletedStart() + 1;
        if (strs.length > len) {
          for (int j = 0; j < strs.length - len; j++) {
            suggest = suggest + strs[j];
          }
        }
        for (int j = diff.getAddedStart(); j <= diff.getAddedEnd(); j++) {
          suggest = suggest + subsequentNames[j];
        }
      }
    }

    suggest = suggest + splitCandidateNames[length];
    if (suggest != "") {
      suggest = suggest.subSequence(0, 1).toString().toLowerCase() + suggest.substring(1);
    }
    return suggest;
  }
}
