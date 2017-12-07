package org.eclipse.che.api.languageserver.shared.util;

import java.util.Comparator;
import org.eclipse.che.jdt.ls.extension.api.dto.LinearRange;

public class LinearRangeComparator implements Comparator<LinearRange> {

  public static final LinearRangeComparator INSTANCE = new LinearRangeComparator();

  @Override
  public int compare(LinearRange o1, LinearRange o2) {
    int res = o1.getOffset() - o2.getOffset();
    if (res != 0) {
      return res;
    } else {
      return o1.getLength() - o2.getLength();
    }
  }
}
