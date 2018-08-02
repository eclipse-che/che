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
package org.eclipse.che.api.core.util;

/**
 * Holder for a value of type <code>T</code>.
 *
 * @author andrew00x
 */
public final class ValueHolder<T> {
  private T value;

  public ValueHolder(T value) {
    this.value = value;
  }

  public ValueHolder() {}

  public synchronized T get() {
    return value;
  }

  public synchronized void set(T value) {
    this.value = value;
  }
}
