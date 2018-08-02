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
package org.eclipse.che.ide.terminal;

import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;

/** @author Alexander Andrienko */
@Singleton
public class TerminalInitializePromiseHolder {

  private Promise<Void> initializerPromise;

  public void setInitializerPromise(Promise<Void> initializerPromise) {
    this.initializerPromise = initializerPromise;
  }

  public Promise<Void> getInitializerPromise() {
    if (initializerPromise == null) {
      throw new RuntimeException("Terminal initializer not set");
    }
    return initializerPromise;
  }
}
