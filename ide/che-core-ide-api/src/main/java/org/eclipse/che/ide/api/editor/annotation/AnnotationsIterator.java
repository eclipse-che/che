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
package org.eclipse.che.ide.api.editor.annotation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;

/**
 * An iterator iteration over a Positions and mapping positions to annotations using a provided map
 * if the provided map contains the element.
 */
final class AnnotationsIterator implements Iterator<Annotation> {

  private Annotation next;

  private final List<Position> positions;

  private int index;

  private final Map<Position, Annotation> map;

  /**
   * @param positions positions to iterate over
   * @param map
   */
  public AnnotationsIterator(final List<Position> positions, final Map<Position, Annotation> map) {
    this.positions = positions;
    this.index = 0;
    this.map = map;
    next = findNext();
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public Annotation next() {
    final Annotation result = next;
    next = findNext();
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private Annotation findNext() {
    while (index < positions.size()) {
      final Position position = positions.get(index);
      index++;
      if (map.containsKey(position)) {
        return map.get(position);
      }
    }

    return null;
  }
}
