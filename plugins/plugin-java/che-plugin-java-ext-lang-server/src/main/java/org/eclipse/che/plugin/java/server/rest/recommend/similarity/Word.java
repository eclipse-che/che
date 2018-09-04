package org.eclipse.che.plugin.java.server.rest.recommend.similarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.open.crc.intt.IdentifierNameTokeniser;
import uk.ac.open.crc.intt.IdentifierNameTokeniserFactory;

public class Word {

  public static String[] splitWords(String string) {
    IdentifierNameTokeniserFactory factory = new IdentifierNameTokeniserFactory();
    factory.setSeparatorCharacters("$");
    IdentifierNameTokeniser tokeniser = factory.create();
    List<String> tokens;
    tokens = tokeniser.tokenise(string);
    ArrayList<String> words = new ArrayList<String>();
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).contains("_")) {
        String str = tokens.get(i);
        while (str.contains("_")) {
          int pos = str.indexOf("_");
          if (pos == 0) {
            words.add("_");
            if (str.length() > 1) {
              str = str.substring(1);
            } else {
              str = "";
            }
          } else if (pos == str.length() - 1) {
            words.add(str.substring(0, str.length() - 1));
            words.add("_");
            str = "";
          } else {
            words.add(str.substring(0, pos));
            words.add("_");
            str = str.substring(pos + 1);
          }
        }
      } else {
        words.add(tokens.get(i));
      }
    }

    String[] splitWords = new String[words.size()];

    for (int j = 0; j < words.size(); j++) {
      splitWords[j] = words.get(j);
    }

    return splitWords;
  }

  public static String[] splitWordsToLowerCase(String string) {
    IdentifierNameTokeniserFactory factory = new IdentifierNameTokeniserFactory();
    factory.setSeparatorCharacters("$");
    IdentifierNameTokeniser tokeniser = factory.create();
    List<String> tokens;
    tokens = tokeniser.tokenise(string);

    ArrayList<String> words = new ArrayList<String>();
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).contains("_")) {
        String str = tokens.get(i);
        while (str.contains("_")) {
          int pos = str.indexOf("_");
          if (pos == 0) {
            words.add("_");
            if (str.length() > 1) {
              str = str.substring(1);
            } else {
              str = "";
            }
          } else if (pos == str.length() - 1) {
            words.add(str.substring(0, str.length() - 1));
            words.add("_");
            str = "";
          } else {
            words.add(str.substring(0, pos));
            words.add("_");
            str = str.substring(pos + 1);
          }
        }
      } else {
        words.add(tokens.get(i));
      }
    }

    String[] splitWords = new String[words.size()];

    for (int j = 0; j < words.size(); j++) {
      splitWords[j] = words.get(j).toLowerCase();
    }

    return splitWords;
  }

  public static Map<String, String> splitAXB(String originalName, String subsequentName) {
    Map<String, String> map = new HashMap<String, String>();
    String[] originalNames = splitWords(originalName);
    String[] subsequentNames = splitWords(subsequentName);
    int minLenth = Math.min(originalNames.length, subsequentNames.length);
    int aIndex = -1;
    int bIndex = -1;

    for (int i = 0; i < minLenth; i++) {
      if (originalNames[i].equals(subsequentNames[i])) {
        aIndex = i;
      } else {
        break;
      }
    }
    String a = "";
    if (aIndex >= 0) {
      for (int j = 0; j <= aIndex; j++) {
        a += originalNames[j];
      }
    }
    map.put("A", a);

    reserved(originalNames);
    reserved(subsequentNames);
    for (int i = 0; i < minLenth; i++) {
      if (originalNames[i].equals(subsequentNames[i])) {
        bIndex = i;
      } else {
        break;
      }
    }
    String b = "";
    if (bIndex >= 0) {
      for (int j = bIndex; j >= 0; j--) {
        b += originalNames[j];
      }
    }
    map.put("B", b);

    reserved(originalNames);
    reserved(subsequentNames);
    String x = "";
    String y = "";
    for (int i = aIndex + 1; i < (originalNames.length - 1 - bIndex); i++) {
      x += originalNames[i];
    }
    for (int i = aIndex + 1; i < (subsequentNames.length - 1 - bIndex); i++) {
      y += subsequentNames[i];
    }
    map.put("X", x);
    map.put("Y", y);
    return map;
  }

  public static Map<String, String> splitCXD(
      String originalName, String subsequentName, String candidateName) {
    Map<String, String> map = new HashMap<String, String>();
    Map<String, String> originalMap = Word.splitAXB(originalName, subsequentName);
    String a = originalMap.get("A");
    String b = originalMap.get("B");
    String x = originalMap.get("X");
    if (!candidateName.contains(x)) return map;

    if (x.equals("")) {
      double maxSimilarity = 0.0;
      String[] candidateNames = splitWords(candidateName);
      String c = "";
      String d = "";
      for (int i = 0; i <= candidateNames.length; i++) {
        String tempC = "";
        for (int j = 0; j < i; j++) {
          tempC += candidateNames[j];
        }

        String tempD = "";
        for (int j = i; j < candidateNames.length; j++) {
          tempD += candidateNames[j];
        }

        double similarity = Similarity.calculate(a, b, tempC, tempD);
        if (similarity > maxSimilarity) {
          maxSimilarity = similarity;
          c = tempC;
          d = tempD;
        }
      }
      map.put("C", c);
      map.put("D", d);
    } else {
      String tempC = "";
      String tempD = "";
      if (candidateName.indexOf(x) > 0)
        tempC = candidateName.substring(0, candidateName.indexOf(x));

      String reservedCandidateName = reserved(candidateName);
      String reservedX = reserved(x);
      if (reservedCandidateName.indexOf(reservedX) > 0) {
        String reservedD =
            reservedCandidateName.substring(0, reservedCandidateName.indexOf(reservedX));
        tempD = reserved(reservedD);
      }

      String[] tempCs = splitWords(tempC);
      String[] tempDs = splitWords(tempD);

      String c = "";
      for (int j = 0; j < tempCs.length; j++) {
        c += tempCs[j];
      }
      map.put("C", c);

      String d = "";
      for (int j = 0; j < tempDs.length; j++) {
        d += tempDs[j];
      }
      map.put("D", d);
    }

    return map;
  }

  public static void reserved(String[] array) {
    String temp;
    int len = array.length;
    for (int i = 0; i < len / 2; i++) {
      temp = array[i];
      array[i] = array[len - 1 - i];
      array[len - 1 - i] = temp;
    }
  }

  public static String reserved(String str) {
    char[] array = str.toCharArray();
    char temp;
    int len = array.length;
    for (int i = 0; i < len / 2; i++) {
      temp = array[i];
      array[i] = array[len - 1 - i];
      array[len - 1 - i] = temp;
    }
    String reservedStr = "";
    for (int i = 0; i < len; i++) {
      reservedStr += array[i];
    }
    return reservedStr;
  }

  public static void main(String[] args) {
    String oldName = "unique";
    String newName = "alternateUniqueKey";
    String cName = "H_B_C_GG";
    String[] oldNames = splitWords(oldName);
    String[] newNames = splitWords(newName);
    // cName.substring(0, cName.length() -1);
    for (String str : oldNames) {
      System.out.println(str);
    }

    Diff diff = new Diff(oldNames, newNames);
    List diffOut = diff.diff();
    Difference h = ((Difference) diffOut.get(0));
    System.out.println(diffOut);
  }
}
