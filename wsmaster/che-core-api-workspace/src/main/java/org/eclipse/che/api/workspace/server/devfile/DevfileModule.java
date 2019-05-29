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
package org.eclipse.che.api.workspace.server.devfile;

import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;

import com.google.inject.AbstractModule;
import org.eclipse.che.api.workspace.server.DevfileToWorkspaceConfigConverter;
import org.eclipse.che.api.workspace.server.devfile.convert.DevfileConverter;
import org.eclipse.che.api.workspace.server.devfile.convert.component.editor.EditorComponentProvisioner;
import org.eclipse.che.api.workspace.server.devfile.convert.component.editor.EditorComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.convert.component.plugin.PluginComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.convert.component.plugin.PluginProvisioner;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator.NoopComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;

/** @author Sergii Leshchenko */
public class DevfileModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DevfileSchemaValidator.class);
    bind(DevfileService.class);

    DevfileBindings.addComponentProvisioners(
        binder(), EditorComponentProvisioner.class, PluginProvisioner.class);

    DevfileBindings.onWorkspaceApplierBinder(
        binder(),
        binder -> {
          binder.addBinding(EDITOR_COMPONENT_TYPE).to(EditorComponentToWorkspaceApplier.class);
          binder.addBinding(PLUGIN_COMPONENT_TYPE).to(PluginComponentToWorkspaceApplier.class);
        });

    bind(DevfileToWorkspaceConfigConverter.class).to(DevfileConverter.class);

    DevfileBindings.onComponentIntegrityValidatorBinder(
        binder(),
        binder -> {
          binder.addBinding(PLUGIN_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
          binder.addBinding(EDITOR_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
        });
  }
}
