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
package org.eclipse.che.api.workspace.activity.inject;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.eclipse.che.api.workspace.activity.JpaWorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityDao;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityManager;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityMeterBinder;
import org.eclipse.che.api.workspace.activity.WorkspaceActivityService;
import org.eclipse.che.inject.DynaModule;

@DynaModule
public class WorkspaceActivityModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WorkspaceActivityService.class);
    bind(WorkspaceActivityManager.class);
    bind(WorkspaceActivityDao.class).to(JpaWorkspaceActivityDao.class);

    Multibinder<MeterBinder> meterMultibinder =
        Multibinder.newSetBinder(binder(), MeterBinder.class);

    meterMultibinder.addBinding().to(WorkspaceActivityMeterBinder.class);
  }
}
