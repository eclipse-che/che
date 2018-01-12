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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.eclipse.che.dto.shared.JsonArray;

public class JsonArrayImpl<T> implements JsonArray<T> {
  private static final Gson gson =
      new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

  private final List<T> delegate;

  public JsonArrayImpl(List<T> delegate) {
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
  public boolean contains(Object o) {
    return delegate.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return delegate.iterator();
  }

  @Override
  public Object[] toArray() {
    return delegate.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return delegate.toArray(a);
  }

  public boolean add(T t) {
    return delegate.add(t);
  }

  @Override
  public boolean remove(Object o) {
    return delegate.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return delegate.containsAll(c);
  }

  public boolean addAll(Collection<? extends T> c) {
    return delegate.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends T> c) {
    return delegate.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return delegate.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return delegate.retainAll(c);
  }

  @Override
  public void clear() {
    delegate.clear();
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
  public T get(int index) {
    return delegate.get(index);
  }

  public T set(int index, T element) {
    return delegate.set(index, element);
  }

  public void add(int index, T element) {
    delegate.add(index, element);
  }

  @Override
  public T remove(int index) {
    return delegate.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return delegate.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return delegate.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return delegate.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return delegate.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return delegate.subList(fromIndex, toIndex);
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
