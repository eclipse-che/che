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
package org.eclipse.che.plugin.maven.client.project;

import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.IN_PROGRESS;
import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.NOT_RESOLVED;
import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.RESOLVED;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_ID;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder;
import org.eclipse.che.plugin.maven.client.MavenJsonRpcHandler;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;

/**
 * Keeper for the state of Resolving Project process. 'Resolving Project process' for a Maven
 * project means reimporting maven model.
 *
 * <ul>
 *   Makes it possible to:
 *   <li>keep the state of Resolving Project process
 *   <li>get the state of Resolving Project process when you need
 *   <li>notify the corresponding listener when the state of Resolving Project process has been
 *       changed
 * </ul>
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ResolvingMavenProjectStateHolder implements ResolvingProjectStateHolder {
  private ResolvingProjectState state;
  private HashSet<ResolvingProjectStateListener> listeners;

  @Inject
  public ResolvingMavenProjectStateHolder(
      MavenJsonRpcHandler mavenJsonRpcHandler, EventBus eventBus) {
    this.state = NOT_RESOLVED;
    this.listeners = new HashSet<>();

    mavenJsonRpcHandler.addStartStopHandler(this::handleStartStop);
  }

  @Override
  public ResolvingProjectState getState() {
    return state;
  }

  @Override
  public String getProjectType() {
    return MAVEN_ID;
  }

  @Override
  public void addResolvingProjectStateListener(ResolvingProjectStateListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeResolvingProjectStateListener(ResolvingProjectStateListener listener) {
    listeners.remove(listener);
  }

  private void handleStartStop(StartStopNotification startStopNotification) {
    if (startStopNotification.isStart()) {
      state = IN_PROGRESS;
    } else {
      state = RESOLVED;
    }
    notifyListenersTimer.cancel();
    notifyListenersTimer.schedule(200);
  }

  /**
   * We need to have some delay to avoid a flashing when a resolving project state has been changed
   */
  private Timer notifyListenersTimer =
      new Timer() {
        @Override
        public void run() {
          for (ResolvingProjectStateListener listener : listeners) {
            listener.onResolvingProjectStateChanged(state);
          }
        }
      };
}
