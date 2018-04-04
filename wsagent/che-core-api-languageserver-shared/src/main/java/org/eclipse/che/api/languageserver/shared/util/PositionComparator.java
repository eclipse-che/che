/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
