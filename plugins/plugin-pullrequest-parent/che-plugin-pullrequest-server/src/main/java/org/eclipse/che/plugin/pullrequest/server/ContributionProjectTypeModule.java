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
package org.eclipse.che.plugin.pullrequest.server;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.inject.DynaModule;

/**
 * The contribution project type module bindings.
 *
 * @author Kevin Pollet
 */
@DynaModule
public class ContributionProjectTypeModule extends AbstractModule {
  @Override
  protected void configure() {
    final Multibinder<ProjectTypeDef> projectTypeMultibinder =
        Multibinder.newSetBinder(binder(), ProjectTypeDef.class);
    projectTypeMultibinder.addBinding().to(ContributionProjectType.class);
    bind(ContributionProjectTypeCleaner.class);
  }
}
