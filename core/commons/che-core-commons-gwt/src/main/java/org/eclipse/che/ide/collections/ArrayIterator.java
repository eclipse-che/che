// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implementation that iterates array without gaps in index,
 *
 * @param <T>
 *         items type
 */
public class ArrayIterator<T> implements Iterator<T> {

    private       int      index;
    private final Array<T> items;
    private       boolean  hasRemovedSinceNextCall;

    public ArrayIterator(Array<T> items) {
        this.items = items;
    }

    @Override
    public boolean hasNext() {
        return index < items.size();
    }

    @Override
    public T next() {
        if (index == items.size()) {
            throw new NoSuchElementException();
        }

        T result = items.get(index);
        index++;

        hasRemovedSinceNextCall = false;

        return result;
    }

    @Override
    public void remove() {
        if (hasRemovedSinceNextCall || index == 0) {
            throw new IllegalStateException();
        }

        index--;
        items.remove(index);
        hasRemovedSinceNextCall = true;
    }
}
