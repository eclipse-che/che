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
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.DevfileManager;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.CommandConverter;
import org.eclipse.che.api.devfile.server.convert.DevfileConverter;
import org.eclipse.che.api.devfile.server.convert.ProjectConverter;
import org.eclipse.che.api.devfile.server.convert.tool.ToolProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;
import org.eclipse.che.api.devfile.server.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.factory.server.urlfactory.URLFetcher;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class DefaultFactoryParameterResolverTest {

  private static final String DEVFILE =
      ""
          + "specVersion: 0.0.1\n"
          + "name: test\n"
          + "tools:\n"
          + "- type: kubernetes\n"
          + "  name: tool\n"
          + "  local: ../localfile\n";

  @Mock private URLFetcher urlFetcher;
  @Mock private KubernetesRecipeParser kubernetesRecipeParser;

  @Test
  public void shouldResolveRelativeFiles() throws Exception {
    // given

    // we need to set up an "almost real" devfile converter which is a little bit involved
    DevfileSchemaValidator validator = new DevfileSchemaValidator(new DevfileSchemaProvider());
    DevfileIntegrityValidator integrityValidator =
        new DevfileIntegrityValidator(kubernetesRecipeParser);
    Set<ToolProvisioner> toolProvisioners = new HashSet<>();
    Map<String, ToolToWorkspaceApplier> appliers = new HashMap<>();
    ToolToWorkspaceApplier applier = mock(ToolToWorkspaceApplier.class);
    appliers.put("kubernetes", applier);

    doAnswer(
            i -> {
              // in here we mock that the tool applier requests the contents of the referenced
              // local file. That's all we need to happen
              FileContentProvider p = i.getArgument(2);
              Tool tool = i.getArgument(1);
              p.fetchContent(tool.getLocal());
              return null;
            })
        .when(applier)
        .apply(any(), any(), any());

    DevfileConverter devfileConverter =
        new DevfileConverter(
            new ProjectConverter(), new CommandConverter(), toolProvisioners, appliers);

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

    // when
    res.createFactory(factoryParameters);

    // then
    verify(urlFetcher).fetch(eq("scheme:/localfile"));
  }
}
