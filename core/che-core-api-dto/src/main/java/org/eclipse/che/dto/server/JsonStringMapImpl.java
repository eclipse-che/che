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
package org.eclipse.che.dto.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.dto.shared.JsonStringMap;

public class JsonStringMapImpl<T> implements JsonStringMap<T> {
  private static final Gson gson =
      new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

  private final Map<String, T> delegate;

  public JsonStringMapImpl(Map<String, T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public T get(Object key) {
    return delegate.get(key);
  }

  public T put(String key, T value) {
    return delegate.put(key, value);
  }

  @Override
  public T remove(Object key) {
    return delegate.remove(key);
  }

  public void putAll(Map<? extends String, ? extends T> m) {
    delegate.putAll(m);
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<String> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<T> values() {
    return delegate.values();
  }

  @Override
  public Set<Entry<String, T>> entrySet() {
    return delegate.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return delegate.equals(o);
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public String toJson() {
    return gson.toJson(this);
  }

  @Override
  public void toJson(Writer w) {
    gson.toJson(this, w);
  }

  @Override
  public JsonElement toJsonElement() {
    return gson.toJsonTree(this);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}
