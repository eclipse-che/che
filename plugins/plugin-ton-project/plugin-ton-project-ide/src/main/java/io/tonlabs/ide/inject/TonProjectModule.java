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
package io.tonlabs.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import io.tonlabs.ide.project.TonCProjectWizardRegistrar;
import io.tonlabs.ide.project.TonSolProjectWizardRegistrar;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.project.type.wizard.ProjectWizardRegistrar;

/** TON Project Gin Module for binding the project wizard and helper factories. */
@ExtensionGinModule
public class TonProjectModule extends AbstractGinModule {

  @Override
  protected void configure() {
    GinMultibinder.newSetBinder(this.binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(TonCProjectWizardRegistrar.class);

    GinMultibinder.newSetBinder(this.binder(), ProjectWizardRegistrar.class)
        .addBinding()
        .to(TonSolProjectWizardRegistrar.class);
  }
}
