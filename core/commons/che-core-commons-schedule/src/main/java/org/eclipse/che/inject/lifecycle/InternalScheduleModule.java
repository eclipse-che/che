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
package org.eclipse.che.inject.lifecycle;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.eclipse.che.commons.schedule.Launcher;

/**
 * Launch method marked with @ScheduleCron @ScheduleDelay and @ScheduleRate annotations using
 * Launcher
 *
 * <p>Note do not inject this module. Use {@link
 * org.eclipse.che.commons.schedule.executor.ScheduleModule}
 *
 * @author Sergii Kabashniuk
 */
public class InternalScheduleModule extends LifecycleModule {

  @Override
  protected void configure() {
    bindListener(
        Matchers.any(),
        new ScheduleTypeListener(getProvider(Launcher.class), getProvider(Injector.class)));
  }

  private static class ScheduleTypeListener implements TypeListener {
    private final Provider<Launcher> launcher;
    private final Provider<Injector> injector;

    private ScheduleTypeListener(Provider<Launcher> launcher, Provider<Injector> injector) {
      this.launcher = launcher;
      this.injector = injector;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
      encounter.register(new ScheduleInjectionListener<I>(launcher, injector));
    }
  }
}
