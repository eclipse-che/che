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
package org.eclipse.che.api.workspace.server.wsplugins;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Angel Misevski <amisevsk@redhat.com> */
@Listeners(MockitoTestNGListener.class)
public class PluginMetaRetrieverTest {

  private static final String BASE_REGISTRY = "https://che-plugin-registry.openshift.io";
  private PluginMetaRetriever metaRetriever;

  @BeforeClass
  public void setUp() throws Exception {
    metaRetriever = spy(new PluginMetaRetriever(BASE_REGISTRY));
    doReturn(null).when(metaRetriever).getBody(any(URI.class), any());
    doNothing().when(metaRetriever).validateMeta(any(), anyString(), anyString());
  }

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
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, "plugin1")
            .build();

    metaRetriever.get(attributes);

    fail(
        "PluginMetaRetriever should throw Exception when attributes includes "
            + "plugins and no registry is defined");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void shouldThrowExceptionWhenEditorNotEmptyAndRegistryNotDefined() throws Exception {
    Map<String, String> attributes =
        ImmutableMap.<String, String>builder()
            .put(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "editor")
            .build();

    metaRetriever.get(attributes);

    fail(
        "PluginMetaRetriever should throw Exception when attributes includes "
            + "editor and no registry is defined");
  }

  @DataProvider
  public Object[][] pluginMetaRetrieverWithAndWithoutRegistry() {
    return new Object[][] {
      {new PluginMetaRetriever(BASE_REGISTRY)}, {new PluginMetaRetriever(null)}
    };
  }

  @Test(dataProvider = "pluginProvider")
  public void shouldGetMetaByCorrectURLUsingBaseRegistry(
      Map<String, String> attributes, String expectedUri) throws Exception {
    metaRetriever.get(attributes);

    verify(metaRetriever).getBody(eq(new URI(expectedUri)), eq(PluginMeta.class));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Multiple editors found in workspace config attributes."
              + " It is not supported. Please, use one editor only.")
  public void shouldThrowExceptionWhenMultipleEditorsSpecified() throws Exception {

    metaRetriever.get(createAttributes("", "theia:1.0, idea:2.0"));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Plugin format is illegal. Problematic plugin entry:.*")
  public void shouldThrowExceptionWhenPluginFormatBad() throws Exception {

    metaRetriever.get(createAttributes("my-plugin:4.0, my_new_plugin:part:1.0", ""));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Invalid Che tooling plugins configuration: plugin .* is duplicated")
  public void shouldThrowExceptionWhenPluginIsDuplicated() throws Exception {

    metaRetriever.get(
        createAttributes(
            "http://registry.myregistry1.com:8080/my-plugin:4.0, "
                + "http://registry2.myregistry2.com:8080/my-plugin:4.0",
            ""));
  }

  @DataProvider(name = "pluginProvider")
  public static Object[][] pluginProvider() {
    return new Object[][] {
      {createAttributes("my-plugin:4.0", ""), BASE_REGISTRY + "/plugins/my-plugin/4.0/meta.yaml"},
      {
        createAttributes("http://registry.myregistry.com:8080/my-plugin:4.0", ""),
        "http://registry.myregistry.com:8080/my-plugin/4.0/meta.yaml"
      },
      {
        createAttributes("https://myregistry.com/registry/my.plugin:4.0", ""),
        "https://myregistry.com/registry/my.plugin/4.0/meta.yaml"
      }
    };
  }

  private static Map<String, String> createAttributes(String plugins, String editor) {
    return ImmutableMap.of(
        Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE,
        plugins,
        Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE,
        editor);
  }
}
