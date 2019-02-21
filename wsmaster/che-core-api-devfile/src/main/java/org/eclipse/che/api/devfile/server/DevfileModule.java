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
package org.eclipse.che.api.devfile.server;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.devfile.server.convert.tool.ToolProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.tool.dockerimage.DockerimageToolProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.dockerimage.DockerimageToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.tool.editor.EditorToolProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.editor.EditorToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.tool.kubernetes.KubernetesToolProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.kubernetes.KubernetesToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.tool.plugin.PluginProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.plugin.PluginToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;

/** @author Sergii Leshchenko */
public class DevfileModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DevfileSchemaValidator.class);
    bind(DevfileService.class);

    Multibinder<ToolProvisioner> workspaceToDevfileAppliers =
        newSetBinder(binder(), ToolProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(EditorToolProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(PluginProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(DockerimageToolProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(KubernetesToolProvisioner.class);

    MapBinder<String, ToolToWorkspaceApplier> toolToWorkspaceApplier =
        newMapBinder(binder(), String.class, ToolToWorkspaceApplier.class);
    toolToWorkspaceApplier.addBinding(EDITOR_TOOL_TYPE).to(EditorToolToWorkspaceApplier.class);
    toolToWorkspaceApplier.addBinding(PLUGIN_TOOL_TYPE).to(PluginToolToWorkspaceApplier.class);
    toolToWorkspaceApplier
        .addBinding(KUBERNETES_TOOL_TYPE)
        .to(KubernetesToolToWorkspaceApplier.class);
    toolToWorkspaceApplier
        .addBinding(DOCKERIMAGE_TOOL_TYPE)
        .to(DockerimageToolToWorkspaceApplier.class);
  }
}
