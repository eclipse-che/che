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
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_COMPONENT_TYPE;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.devfile.server.convert.DevfileConverter;
import org.eclipse.che.api.devfile.server.convert.component.ComponentProvisioner;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.component.dockerimage.DockerimageComponentProvisioner;
import org.eclipse.che.api.devfile.server.convert.component.dockerimage.DockerimageComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.component.editor.EditorComponentProvisioner;
import org.eclipse.che.api.devfile.server.convert.component.editor.EditorComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.component.kubernetes.KubernetesComponentProvisioner;
import org.eclipse.che.api.devfile.server.convert.component.kubernetes.KubernetesComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.component.plugin.PluginComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.component.plugin.PluginProvisioner;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
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
    workspaceToDevfileAppliers.addBinding().to(DockerimageComponentProvisioner.class);
    workspaceToDevfileAppliers.addBinding().to(KubernetesComponentProvisioner.class);

    MapBinder<String, ComponentToWorkspaceApplier> componentToWorkspaceApplier =
        newMapBinder(binder(), String.class, ComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(EDITOR_COMPONENT_TYPE)
        .to(EditorComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(PLUGIN_COMPONENT_TYPE)
        .to(PluginComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(OPENSHIFT_COMPONENT_TYPE)
        .to(KubernetesComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(KUBERNETES_COMPONENT_TYPE)
        .to(KubernetesComponentToWorkspaceApplier.class);
    componentToWorkspaceApplier
        .addBinding(DOCKERIMAGE_COMPONENT_TYPE)
        .to(DockerimageComponentToWorkspaceApplier.class);

    bind(DevfileToWorkspaceConfigConverter.class).to(DevfileConverter.class);
  }
}
