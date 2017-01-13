/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.annotation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TypedPosition;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;

/**
 * An iterator iteration over a Positions and mapping positions to
 * annotations using a provided map if the provided map contains the element.
 *
 */
final class AnnotationsIterator implements Iterator<Annotation> {

    private Annotation next;

    private final List<TypedPosition> positions;

    private int index;

    private final Map<Position, Annotation> map;

    /**
     * @param positions
     *         positions to iterate over
     * @param map
     *         a map to map positions to annotations
     */
    public AnnotationsIterator(final List<TypedPosition> positions,
                               final Map<Position, Annotation> map) {
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