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
