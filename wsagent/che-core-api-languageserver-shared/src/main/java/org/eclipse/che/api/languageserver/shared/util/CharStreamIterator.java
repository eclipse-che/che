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

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.eclipse.lsp4j.Position;

/**
 * Line aware iterator over a stream of characters.
 *
 * @author Thomas Mäder
 */
public class CharStreamIterator {
  public static BiConsumer<Integer, Integer> NULL_CONSUMER =
      new BiConsumer<Integer, Integer>() {
        @Override
        public void accept(Integer t, Integer u) {}
      };

  private int currentReadLine = 0;
  private int currentReadChar = 0;
  private int currentOffset = 0;
  private int ch;
  private Supplier<Integer> source;

  public CharStreamIterator(Supplier<Integer> source) {
    this.source = source;
    ch = source.get();
  }

  public void advanceTo(Position start, BiConsumer<Integer, Integer> dest) {
    while (ch >= 0
        && (currentReadLine < start.getLine() || currentReadChar < start.getCharacter())) {
      dest.accept(ch, currentOffset);
      if (ch == '\r') {
        currentReadLine++;
        currentReadChar = 0;
        ch = source.get();
        currentOffset++;
        if (ch == '\n') {
          dest.accept(ch, currentOffset);
          ch = source.get();
          currentOffset++;
        }
      } else if (ch == '\n') {
        currentReadLine++;
        currentReadChar = 0;
        ch = source.get();
        currentOffset++;
      } else {
        currentReadChar++;
        ch = source.get();
        currentOffset++;
      }
    }
  }
}
