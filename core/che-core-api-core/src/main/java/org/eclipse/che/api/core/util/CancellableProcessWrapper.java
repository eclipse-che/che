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
 * Cancellable wrapper of {@code Process}.
 *
 * @author andrew00x
 */
public final class CancellableProcessWrapper implements Cancellable {
  private final Process process;
  private final Callback callback;

  public CancellableProcessWrapper(Process process) {
    this(process, null);
  }

  public CancellableProcessWrapper(Process process, Callback callback) {
    this.process = process;
    this.callback = callback;
  }

  @Override
  public void cancel() {
    ProcessUtil.kill(process);
    if (callback != null) {
      callback.cancelled(this);
    }
  }
}
