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
package org.eclipse.che.api.workspace.devfile.server;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static org.eclipse.che.api.workspace.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.devfile.server.convert.DevfileConverter;
import org.eclipse.che.api.workspace.devfile.server.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.devfile.server.convert.component.editor.EditorComponentProvisioner;
import org.eclipse.che.api.workspace.devfile.server.convert.component.editor.EditorComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.devfile.server.convert.component.plugin.PluginComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.devfile.server.convert.component.plugin.PluginProvisioner;
import org.eclipse.che.api.workspace.devfile.server.validator.ComponentIntegrityValidator.NoopComponentIntegrityValidator;
import org.eclipse.che.api.workspace.devfile.server.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.DevfileToWorkspaceConfigConverter;

/** @author Sergii Leshchenko */
public class DevfileModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DevfileSchemaValidator.class);
    bind(DevfileService.class);

    Multibinder<ComponentProvisioner> workspaceToDevfileAppliers =
        newSetBinder(binder(), ComponentProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(EditorComponentProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(PluginProvisioner.class);

    MapBinder<String, ComponentToWorkspaceApplier> componentToWorkspaceApplier =
        newMapBinder(binder(), String.class, ComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(Constants.EDITOR_COMPONENT_TYPE)
        .to(EditorComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(PLUGIN_COMPONENT_TYPE)
        .to(PluginComponentToWorkspaceApplier.class);

    bind(DevfileToWorkspaceConfigConverter.class).to(DevfileConverter.class);

    DevfileBindings.onComponentIntegrityValidatorBinder(
        binder(),
        binder -> {
          binder.addBinding(PLUGIN_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
          binder.addBinding(EDITOR_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
        });
  }
}
