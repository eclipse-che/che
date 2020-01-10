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
package org.eclipse.che.tomcat.lifecycle;

import java.util.logging.Logger;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;

/** Apache Tomcat LifecycleListener which shuts down Tomcat after a failed WAR deployment. */
public class FailFastLifecycleListener implements LifecycleListener {

  private static final Logger LOG = Logger.getLogger(FailFastLifecycleListener.class.getName());

  @Override
  public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
    Lifecycle lifecycle = lifecycleEvent.getLifecycle();
    LifecycleState state = lifecycle.getState();

    if (LifecycleState.FAILED == state) {
      LOG.severe("Deployment of '" + lifecycle + "' failed: Exiting Tomcat now");
      System.exit(1);
    }
  }
}
