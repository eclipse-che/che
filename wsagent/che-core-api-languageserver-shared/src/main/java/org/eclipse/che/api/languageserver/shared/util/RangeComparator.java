/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.languageserver.shared.util;

import java.util.Comparator;
import java.util.function.Function;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class RangeComparator implements Comparator<Range> {
  Comparator<Position> positionComparator = new PositionComparator();

  @Override
  public int compare(Range o1, Range o2) {
    int starts = positionComparator.compare(o1.getStart(), o2.getStart());
    if (starts != 0) {
      return starts;
    }
    return positionComparator.compare(o1.getEnd(), o2.getEnd());
  }

  public static <X, Y> Comparator<X> transform(Comparator<Y> comparator, Function<X, Y> f) {
    return new Comparator<X>() {

      @Override
      public int compare(X o1, X o2) {
        return comparator.compare(f.apply(o1), f.apply(o2));
      }
    };
  }
}
