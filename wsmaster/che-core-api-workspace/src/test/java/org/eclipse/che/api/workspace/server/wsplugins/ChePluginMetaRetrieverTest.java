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

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginMeta;
import org.eclipse.che.api.workspace.shared.Constants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ChePluginMetaRetrieverTest {

  private static final String BASE_REGISTRY = "https://che-plugin-registry.openshift.io";
  private PluginMetaRetriever metaRetriever;

  @BeforeClass
  public void setUp() throws Exception {
    metaRetriever = spy(new PluginMetaRetriever(BASE_REGISTRY));
    doReturn(null).when(metaRetriever).getBody(any(URI.class), any());
    doNothing().when(metaRetriever).validateMeta(any(), anyString(), anyString());
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
