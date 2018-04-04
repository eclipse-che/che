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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

public class CharStreamEditor {
  private static final Comparator<TextEdit> COMPARATOR =
      RangeComparator.transform(new RangeComparator(), TextEdit::getRange);
  private ArrayList<TextEdit> edits;
  private int currentReadLine = 0;
  private int currentReadChar = 0;

  private int currentWriteLine = 0;
  private int currentWriteChar = 0;

  private int ch;
  private Supplier<Integer> source;
  private Consumer<Integer> sink;
  public static final Consumer<Integer> NULL = ch -> {};

  public CharStreamEditor(
      Collection<TextEdit> edits, Supplier<Integer> source, Consumer<Integer> dest) {
    this.edits = new ArrayList<>(edits);
    this.edits.sort(COMPARATOR);
    this.source = source;
    this.sink = ch -> writeAndCount(ch, dest);
  }

  public List<TextEdit> transform() {
    List<TextEdit> inverse = new ArrayList<>();

    Iterator<TextEdit> editIterator = edits.iterator();
    ch = source.get();
    while (editIterator.hasNext()) {
      TextEdit edit = editIterator.next();

      advanceTo(edit.getRange().getStart(), sink);
      Position undoStart = new Position(currentWriteLine, currentWriteChar);
      for (int i = 0; i < edit.getNewText().length(); i++) {
        sink.accept((int) edit.getNewText().charAt(i));
      }
      StringBuilder replaced = new StringBuilder();
      advanceTo(edit.getRange().getEnd(), forStringBuilder(replaced));
      Position undoEnd = new Position(currentWriteLine, currentWriteChar);
      inverse.add(new TextEdit(new Range(undoStart, undoEnd), replaced.toString()));
    }
    // all edits have been processed. Copy the rest of the chars.
    while (ch >= 0) {
      sink.accept(ch);
      ch = source.get();
    }
    return inverse;
  }

  private void writeAndCount(int ch, Consumer<Integer> dest) {
    dest.accept(ch);
    if (ch == '\r') {
      // we recognize \r\n, \n
    } else if (ch == '\n') {
      currentWriteLine++;
      currentWriteChar = 0;
    } else {
      currentWriteChar++;
    }
  }

  private void advanceTo(Position start, Consumer<Integer> dest) {
    while (ch >= 0
        && (currentReadLine < start.getLine() || currentReadChar < start.getCharacter())) {
      dest.accept(ch);
      if (ch == '\r') {
        currentReadLine++;
        currentReadChar = 0;
        ch = source.get();
        if (ch == '\n') {
          dest.accept(ch);
          ch = source.get();
        }
      } else if (ch == '\n') {
        currentReadLine++;
        currentReadChar = 0;
        ch = source.get();
      } else {
        currentReadChar++;
        ch = source.get();
      }
    }
  }

  public static Consumer<Integer> forStringBuilder(StringBuilder b) {
    return new Consumer<Integer>() {
      @Override
      public void accept(Integer ch) {
        b.append((char) ch.intValue());
      }
    };
  }

  public static Supplier<Integer> forReader(Reader reader) {
    return new Supplier<Integer>() {
      @Override
      public Integer get() {
        try {
          return reader.read();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public static Consumer<Integer> forWriter(Writer w) {
    return new Consumer<Integer>() {

      @Override
      public void accept(Integer ch) {
        try {
          w.write(ch);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
}
