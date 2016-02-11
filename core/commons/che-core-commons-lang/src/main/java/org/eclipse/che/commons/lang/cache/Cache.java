/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.commons.lang.cache;

import java.util.Map.Entry;


/**
 * Cache abstraction.
 *
 * @deprecated Use Guava cache or other cache implementations
 *
 * */
@Deprecated
public interface Cache<K, V> extends Iterable<Entry<K, V>> {
    V get(K key);

    V put(K key, V value);

    V remove(K key);

    boolean contains(K key);

    void clear();

    int size();
}
