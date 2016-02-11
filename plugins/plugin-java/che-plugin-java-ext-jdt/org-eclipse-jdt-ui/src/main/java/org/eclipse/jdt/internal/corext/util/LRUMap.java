/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.corext.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least-recently-used cache. When the map tops the maximum size configured in the constructor, it
 * automatically removes the oldest entry in terms of last access.
 * <p>
 * Invoking the <tt>put</tt> or <tt>get</tt> method results in an access to the corresponding entry.
 * The <tt>putAll</tt> method generates one entry access for each mapping in the specified map, in
 * the order that key-value mappings are provided by the specified map's entry set iterator. <i>No
 * other methods generate entry accesses.</i> In particular, operations on collection-views do
 * <i>not</i> affect the order of iteration of the backing map.
 * 
 * @param <K> type of the keys
 * @param <V> type of the values
 */
public class LRUMap<K,V> extends LinkedHashMap<K,V> {

	private static final long serialVersionUID= 1L;
	private final int fMaxSize;

	/**
	 * Creates an empty LRU map with the given maximum size.
	 * 
	 * @param maxSize the maximum number of elements in the cache
	 */
	public LRUMap(int maxSize) {
		super(maxSize, 0.75f, true);
		fMaxSize= maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
		return size() > fMaxSize;
	}
}
