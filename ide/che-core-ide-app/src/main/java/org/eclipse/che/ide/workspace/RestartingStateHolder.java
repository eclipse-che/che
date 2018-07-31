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
package org.eclipse.che.ide.workspace;

import com.google.inject.Singleton;

/** Just a holder to track restarting of a workspace */
@Singleton
class RestartingStateHolder {
  private boolean restarting;

  void setRestartingState(boolean restarting) {
    this.restarting = restarting;
  }

  boolean isRestarting() {
    return restarting;
  }
}
