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
package org.eclipse.che.api.workspace.server;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.PluginMetaRetriever;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Angel Misevski <amisevsk@redhat.com> */
@Listeners(MockitoTestNGListener.class)
public class PluginMetaRetrieverTest {

  private static final String REGISTRY_ATTRIBUTE = "plugin-registry";

  @Test(dataProvider = "pluginMetaRetrieverWithAndWithoutRegistry")
  public void shouldReturnEmptyListWhenAttributesNull(PluginMetaRetriever retriever)
      throws Exception {

    Collection<PluginMeta> metas = retriever.get(null);

    assertTrue(
        metas.isEmpty(), "PluginMetaRetriever should return empty list when attributes is null");
  }

  @Test(dataProvider = "pluginMetaRetrieverWithAndWithoutRegistry")
  public void shouldReturnEmptyListWhenNoPluginsOrEditors(PluginMetaRetriever retriever)
      throws Exception {
    Map<String, String> attributes = Collections.emptyMap();

    Collection<PluginMeta> metas = retriever.get(attributes);

    assertTrue(
        metas.isEmpty(), "PluginMetaRetriever should return empty list when attributes is empty");
  }

  @Test(dataProvider = "pluginMetaRetrieverWithAndWithoutRegistry")
  public void shouldReturnEmptyListWhenPluginsValueEmpty(PluginMetaRetriever retriever)
      throws Exception {
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, "")
            .build();

    Collection<PluginMeta> metas = retriever.get(attributes);

    assertTrue(
        metas.isEmpty(),
        "PluginMetaRetriever should return empty list when plugins value is empty");
  }

  @Test(dataProvider = "pluginMetaRetrieverWithAndWithoutRegistry")
  public void shouldReturnEmptyListWhenEditorValueEmpty(PluginMetaRetriever retriever)
      throws Exception {
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "")
            .build();

    Collection<PluginMeta> metas = retriever.get(attributes);

    assertTrue(
        metas.isEmpty(), "PluginMetaRetriever should return empty list when editor value is empty");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionWhenPluginsNotEmptyAndRegistryNotDefined() throws Exception {
    PluginMetaRetriever pluginMetaRetriever = new PluginMetaRetriever(REGISTRY_ATTRIBUTE);
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, "plugin1")
            .build();

    pluginMetaRetriever.get(attributes);

    fail(
        "PluginMetaRetriever should throw Exception when attributes includes "
            + "plugins and no registry is defined");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionWhenEditorNotEmptyAndRegistryNotDefined() throws Exception {
    PluginMetaRetriever pluginMetaRetriever = new PluginMetaRetriever(REGISTRY_ATTRIBUTE);
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "editor")
            .build();

    pluginMetaRetriever.get(attributes);

    fail(
        "PluginMetaRetriever should throw Exception when attributes includes "
            + "editor and no registry is defined");
  }

  @DataProvider
  public Object[][] pluginMetaRetrieverWithAndWithoutRegistry() {
    return new Object[][] {
      {new PluginMetaRetriever(REGISTRY_ATTRIBUTE)}, {new PluginMetaRetriever(null)}
    };
  }
}
