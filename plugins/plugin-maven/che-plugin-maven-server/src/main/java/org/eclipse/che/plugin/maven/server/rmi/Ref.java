/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.rmi;

/**
 * Mutable version of {@link java.util.Optional}
 *
 * @author Evgen Vidolob
 */
public class Ref<T> {
  private T value;

  private Ref(T value) {
    this.value = value;
  }

  public static <T> Ref<T> ofNull() {
    return new Ref<>(null);
  }

  public static <T> Ref<T> of(T value) {
    return new Ref<>(value);
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

  public boolean isNull() {
    return value == null;
  }
}
