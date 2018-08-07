/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
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
