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
package org.eclipse.che.api.factory.server;

import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.devfile.DevfileManager;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.convert.CommandConverter;
import org.eclipse.che.api.workspace.server.devfile.convert.DefaultEditorProvisioner;
import org.eclipse.che.api.workspace.server.devfile.convert.DevfileConverter;
import org.eclipse.che.api.workspace.server.devfile.convert.ProjectConverter;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentFQNParser;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator.NoopComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.wsplugins.PluginFQNParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class DefaultFactoryParameterResolverTest {

  private static final String DEVFILE =
      ""
          + "apiVersion: 1.0.0\n"
          + "metadata:\n"
          + "  name: test\n"
          + "components:\n"
          + "- type: kubernetes\n"
          + "  alias: component\n"
          + "  reference: ../localfile\n";

  @Mock private URLFetcher urlFetcher;
  private ComponentFQNParser componentFQNParser = new ComponentFQNParser(new PluginFQNParser());

  @Test
  public void shouldResolveRelativeFiles() throws Exception {
    // given

    // we need to set up an "almost real" devfile converter which is a little bit involved
    DevfileSchemaValidator validator = new DevfileSchemaValidator(new DevfileSchemaProvider());

    Map<String, ComponentIntegrityValidator> validators = new HashMap<>();
    validators.put(EDITOR_COMPONENT_TYPE, new NoopComponentIntegrityValidator());
    validators.put(PLUGIN_COMPONENT_TYPE, new NoopComponentIntegrityValidator());
    validators.put(KUBERNETES_COMPONENT_TYPE, new NoopComponentIntegrityValidator());
    validators.put(OPENSHIFT_COMPONENT_TYPE, new NoopComponentIntegrityValidator());

    DevfileIntegrityValidator integrityValidator = new DevfileIntegrityValidator(validators);
    Set<ComponentProvisioner> componentProvisioners = new HashSet<>();
    Map<String, ComponentToWorkspaceApplier> appliers = new HashMap<>();
    ComponentToWorkspaceApplier applier = mock(ComponentToWorkspaceApplier.class);
    appliers.put("kubernetes", applier);

    doAnswer(
            i -> {
              // in here we mock that the component applier requests the contents of the referenced
              // local file. That's all we need to happen
              FileContentProvider p = i.getArgument(2);
              ComponentImpl component = i.getArgument(1);
              p.fetchContent(component.getReference());
              return null;
            })
        .when(applier)
        .apply(any(), any(), any());

    DevfileConverter devfileConverter =
        new DevfileConverter(
            new ProjectConverter(),
            new CommandConverter(),
            componentProvisioners,
            appliers,
            new DefaultEditorProvisioner(null, new String[] {}, componentFQNParser),
            new URLFetcher());

    WorkspaceManager workspaceManager = mock(WorkspaceManager.class);

    DevfileManager devfileManager =
        new DevfileManager(validator, integrityValidator, devfileConverter, workspaceManager);

    URLFactoryBuilder factoryBuilder =
        new URLFactoryBuilder("editor", "plugin", urlFetcher, devfileManager);

    DefaultFactoryParameterResolver res =
        new DefaultFactoryParameterResolver(factoryBuilder, urlFetcher);

    // set up our factory with the location of our devfile that is referencing our localfile
    Map<String, String> factoryParameters = new HashMap<>();
    factoryParameters.put(URL_PARAMETER_NAME, "scheme:/myloc/devfile");
    doReturn(DEVFILE).when(urlFetcher).fetchSafely(eq("scheme:/myloc/devfile"));
    doReturn("localfile").when(urlFetcher).fetch("scheme:/localfile");

    // when
    res.createFactory(factoryParameters);

    // then
    verify(urlFetcher).fetch(eq("scheme:/localfile"));
  }
}
