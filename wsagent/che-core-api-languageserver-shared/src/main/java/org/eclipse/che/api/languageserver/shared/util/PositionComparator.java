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
import org.eclipse.lsp4j.Position;

public class PositionComparator implements Comparator<Position> {

  @Override
  public int compare(Position o1, Position o2) {
    int lines = o1.getLine() - o2.getLine();
    if (lines != 0) {
      return lines;
    }
    return o1.getCharacter() - o2.getCharacter();
  }
}
