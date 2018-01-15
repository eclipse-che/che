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
package org.eclipse.che.ide.api.editor.annotation;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;

/** Iterator that returns the annotations for a given region. */
final class RegionIterator implements Iterator<Annotation> {

  private final Iterator<Annotation> parentIterator;

  private final boolean canEndAfter;

  private final boolean canStartBefore;

  private final AnnotationModel model;

  private Annotation next;

  private final Position region;

  /**
   * Iterator that returns all annotations from the parent iterator which have a position in the
   * given model inside the given region.
   *
   * @param parentIterator iterator containing all annotations
   * @param model the model to use to retrieve positions from for each annotation
   * @param offset start position of the region
   * @param length length of the region
   * @param canStartBefore include annotations starting before region
   * @param canEndAfter include annotations ending after region
   */
  public RegionIterator(
      Iterator<Annotation> parentIterator,
      AnnotationModel model,
      int offset,
      int length,
      boolean canStartBefore,
      boolean canEndAfter) {
    this.parentIterator = parentIterator;
    this.model = model;
    this.region = new Position(offset, length);
    this.canEndAfter = canEndAfter;
    this.canStartBefore = canStartBefore;
    next = findNext();
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public Annotation next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    final Annotation result = next;
    next = findNext();
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  private Annotation findNext() {
    while (parentIterator.hasNext()) {
      final Annotation next = parentIterator.next();
      final Position position = model.getPosition(next);
      if (position != null) {
        final int offset = position.getOffset();
        if (isWithinRegion(offset, position.getLength())) {
          return next;
        }
      }
    }
    return null;
  }

  private boolean isWithinRegion(int start, int length) {
    if (canStartBefore && canEndAfter) {
      return region.overlapsWith(start, length);
    } else if (canStartBefore) {
      return region.includes(start + length - (length > 0 ? 1 : 0));
    } else if (canEndAfter) {
      return region.includes(start);
    } else {
      return region.includes(start) && region.includes(start + length - (length > 0 ? 1 : 0));
    }
  }
}
