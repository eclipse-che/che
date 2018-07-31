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
package org.eclipse.che.commons.schedule.executor;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.eclipse.che.commons.schedule.Launcher;
import org.eclipse.che.inject.lifecycle.InternalScheduleModule;

/**
 * Guice deployment module.
 *
 * @author Sergii Kabashniuk
 */
public class ScheduleModule implements Module {
  @Override
  public void configure(Binder binder) {
    binder.bind(Launcher.class).to(ThreadPullLauncher.class).asEagerSingleton();
    binder.install(new InternalScheduleModule());
  }
}
