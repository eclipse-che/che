/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.server.devfile.convert.component.editor.EditorComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.convert.component.plugin.PluginComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator.NoopComponentIntegrityValidator;

/** @author Sergii Leshchenko */
public class DevfileModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DevfileEntityProvider.class);

    DevfileBindings.onWorkspaceApplierBinder(
        binder(),
        binder -> {
          binder.addBinding(EDITOR_COMPONENT_TYPE).to(EditorComponentToWorkspaceApplier.class);
          binder.addBinding(PLUGIN_COMPONENT_TYPE).to(PluginComponentToWorkspaceApplier.class);
        });

    DevfileBindings.onComponentIntegrityValidatorBinder(
        binder(),
        binder -> {
          binder.addBinding(PLUGIN_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
          binder.addBinding(EDITOR_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
        });
  }
}
