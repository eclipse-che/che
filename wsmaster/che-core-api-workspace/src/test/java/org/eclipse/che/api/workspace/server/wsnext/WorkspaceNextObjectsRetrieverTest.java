/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_NEXT_FEATURES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsnext.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsnext.model.CheFeature;
import org.eclipse.che.api.workspace.server.wsnext.model.CheFeatureSpec;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginParameter;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePluginReference;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsnext.model.ObjectMeta;
import org.eclipse.che.api.workspace.server.wsnext.model.Volume;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceNextObjectsRetrieverTest {

  private static final String TEST_FEATURE_NAME = "org.eclipse.che.example-plugin1";
  private static final String TEST_FEATURE_2_NAME = "org.eclipse.che.example-plugin2";
  private static final String TEST_FEATURE_VERSION = "1.0.2";
  private static final String TEST_FEATURE_2_VERSION = "0.0.16";
  private static final String TEST_PLUGIN = "my-plugin";
  private static final String TEST_PLUGIN_2 = "my-test-plugin-2";
  private static final String TEST_PLUGIN_VERSION = "0.0.0";
  private static final String TEST_PLUGIN_VERSION_2 = "1.0.0-SNAPSHOT";
  private static final String TEST_PLUGIN_ENV_NAME = "MY_ENV";
  private static final String TEST_PLUGIN_ENV_NAME_2 = "MY_ENV_2";
  private static final String TEST_PLUGIN_ENV_NAME_3 = "MY_ENV_3";
  private static final String TEST_PLUGIN_ENV_VALUE = "MY_VALUE";
  private static final String TEST_PLUGIN_PARAM_NAME = "MY_PARAMETER_1";
  private static final String TEST_PLUGIN_PARAM_2_NAME = "TEST_PARAM_2";
  private static final String TEST_PLUGIN_PARAM_PLACEHOLDER = "${" + TEST_PLUGIN_PARAM_NAME + "}";
  private static final String TEST_PLUGIN_PARAM_2_PLACEHOLDER =
      "${" + TEST_PLUGIN_PARAM_2_NAME + "}";
  private static final String TEST_PLUGIN_PARAM_VALUE = "http://github.com/plugin1";
  private static final String TEST_PLUGIN_PARAM_VALUE_2 = "http://github.com/plugin2";
  private static final String TEST_PLUGIN_IMAGE = "test/image:tag";
  private static final String TEST_SERVER_NAME = "serv1";
  private static final int TEST_SERVER_PORT = 9090;
  private static final Map<String, String> TEST_SERVER_ATTRIBUTES = ImmutableMap.of("type", "ide");
  private static final String TEST_VOLUME_NAME = "vol1";
  private static final String TEST_VOLUME_PATH = "/vol1/test";
  private static final String API_ENDPOINT = "http://localhost:3000";
  private static final Map<String, String> SINGLE_FEATURE_ATTRIBUTES =
      singletonMap(WORKSPACE_NEXT_FEATURES, TEST_FEATURE_NAME + "/" + TEST_FEATURE_VERSION);

  WorkspaceNextObjectsRetriever retriever;

  @BeforeMethod
  public void setUp() throws Exception {
    retriever = spy(new WorkspaceNextObjectsRetriever(API_ENDPOINT));
  }

  @Test
  public void doesNothingWhenFeatureApiIsNotSet() throws Exception {
    retriever = new WorkspaceNextObjectsRetriever(null);

    Collection<ChePlugin> plugins = retriever.get(SINGLE_FEATURE_ATTRIBUTES);

    assertTrue(plugins.isEmpty());
  }

  @Test
  public void doesNothingWhenAttributesAreEmpty() throws Exception {
    Collection<ChePlugin> plugins = retriever.get(emptyMap());

    assertTrue(plugins.isEmpty());
  }

  @Test
  public void doesNothingWhenAttributesAreNull() throws Exception {
    Collection<ChePlugin> plugins = retriever.get(null);

    assertTrue(plugins.isEmpty());
  }

  @Test
  public void doesNothingWhenThereIsNoFeaturesAttribute() throws Exception {
    Collection<ChePlugin> plugins = retriever.get(singletonMap("not_feature_attribute", "value"));

    assertTrue(plugins.isEmpty());
  }

  @Test
  public void doesNothingWhenFeaturesAttributeIsEmpty() throws Exception {
    Collection<ChePlugin> plugins = retriever.get(singletonMap(WORKSPACE_NEXT_FEATURES, ""));

    assertTrue(plugins.isEmpty());
  }

  @Test(
    dataProvider = "incorrectFeatures",
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "Features format is illegal. Problematic feature entry:.*"
  )
  public void throwsExceptionWhenFeatureEntryIsIncorrect(String featuresAttributeValue)
      throws Exception {
    retriever.get(singletonMap(WORKSPACE_NEXT_FEATURES, featuresAttributeValue));
  }

  @DataProvider(name = "incorrectFeatures")
  public static Object[][] incorrectFeatures() {
    return new Object[][] {
      {"feature1"},
      {"feature1/"},
      {"/version1"},
      {"/"},
      {"feature1:version1"},
      {"/feature1/version1"},
      {"/feature1/version1/"},
      {"feature1//version1"},
      {"feature1//version1,"},
      {",feature1//version1"},
      {"feature1/version1,feature2//version2"},
      {"feature1/version1,/feature2/version2"},
      {"feature1/version1,feature2:version2"},
    };
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Invalid Workspace.Next configuration: feature .* is duplicated"
  )
  public void throwsExceptionWhenFeatureIsDuplicated() throws Exception {
    retriever.get(singletonMap(WORKSPACE_NEXT_FEATURES, "feature1/version1,feature1/version1"));
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "Error occurred on retrieval of feature .*. Error: test error"
  )
  public void throwsExceptionWhenFeatureRetrievalFails() throws Exception {
    doThrow(new IOException("test error"))
        .when(retriever)
        .getBody(any(URI.class), eq(CheFeature.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Error occurred on retrieval of ChePlugin .*. Error: test error"
  )
  public void throwsExceptionWhenPluginRetrievalFails() throws Exception {
    CheFeature cheFeature = testFeature();
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doThrow(new IOException("test error"))
        .when(retriever)
        .getBody(any(URI.class), eq(ChePlugin.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "ChePlugin '.*/.*' configuration is invalid. Version is missing."
  )
  public void throwExceptionIfPluginVersionIsNull() throws Exception {
    CheFeature cheFeature = testFeature();
    ChePlugin plugin = testPlugin();
    plugin.setVersion(null);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "ChePlugin '.*/.*' configuration is invalid. Version is missing."
  )
  public void throwExceptionIfPluginVersionIsEmpty() throws Exception {
    CheFeature cheFeature = testFeature();
    ChePlugin plugin = testPlugin();
    plugin.setVersion("");
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "ChePlugin '.*/.*' configuration is invalid. Name is missing."
  )
  public void throwExceptionIfPluginNameIsNull() throws Exception {
    CheFeature cheFeature = testFeature();
    ChePlugin plugin = testPlugin();
    plugin.setName(null);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "ChePlugin '.*/.*' configuration is invalid. Name is missing."
  )
  public void throwExceptionIfPluginNameIsEmpty() throws Exception {
    CheFeature cheFeature = testFeature();
    ChePlugin plugin = testPlugin();
    plugin.setName("");
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Parameters not supported by che plugins found: .*" + TEST_PLUGIN_PARAM_NAME + ".*"
  )
  public void throwExceptionIfValueForPluginParameterNotFound() throws Exception {
    CheFeature cheFeature =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    ChePlugin plugin = testPluginWithEnv(TEST_PLUGIN_ENV_NAME, TEST_PLUGIN_ENV_VALUE);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "Containers contain duplicated exposed ports."
  )
  public void throwExceptionIfContainerHasDuplicatedPorts() throws Exception {
    ChePlugin plugin = testPlugin();
    List<CheContainerPort> containerPorts = plugin.getContainers().get(0).getPorts();
    CheContainerPort containerPort = new CheContainerPort().exposedPort(101010);
    containerPorts.add(containerPort);
    containerPorts.add(containerPort);
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));
    CheFeature cheFeature = testFeature();
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Ports in containers and endpoints don't match. Difference: .*"
  )
  public void throwExceptionIfContainersAndEndpointsPortsDoNotMatch() throws Exception {
    ChePlugin plugin = testPlugin();
    List<CheContainerPort> containerPorts = plugin.getContainers().get(0).getPorts();
    CheContainerPort containerPort = new CheContainerPort().exposedPort(101010);
    containerPorts.add(containerPort);
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));
    CheFeature cheFeature = testFeature();
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test
  public void testFeaturesWithoutParameters() throws Exception {
    CheFeature cheFeature = testFeature();
    ChePlugin plugin = testPlugin();
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    Collection<ChePlugin> plugins = retriever.get(SINGLE_FEATURE_ATTRIBUTES);

    assertEquals(plugins, singletonList(expectedPlugin()));
  }

  @Test
  public void testFeaturesWithParameter() throws Exception {
    CheFeature cheFeature =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    ChePlugin plugin = testPluginWithEnv(TEST_PLUGIN_ENV_NAME, TEST_PLUGIN_PARAM_PLACEHOLDER);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    Collection<ChePlugin> plugins = retriever.get(SINGLE_FEATURE_ATTRIBUTES);

    assertEquals(
        plugins,
        singletonList(expectedPluginWithEnv(TEST_PLUGIN_ENV_NAME, TEST_PLUGIN_PARAM_VALUE)));
  }

  @Test
  public void testFeaturesWithTheSamePluginsAndTheSameParameters() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE_2);
    ChePlugin plugin = testPluginWithEnv(TEST_PLUGIN_ENV_NAME_2, TEST_PLUGIN_PARAM_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));

    Collection<ChePlugin> plugins = retriever.get(featuresAttributes);

    // we don't know the order of values in env var from feature, so we have to match it separately
    // - can't assert whole object
    assertEquals(plugins.size(), 1);
    ChePlugin actualPlugin = plugins.iterator().next();
    assertEquals(actualPlugin.getContainers().size(), 1);
    List<EnvVar> envVars = actualPlugin.getContainers().iterator().next().getEnv();
    envVars
        .stream()
        .filter(envVar -> envVar.getName().equals(TEST_PLUGIN_ENV_NAME_2))
        .forEach(
            envVar ->
                assertEqualsNoOrder(
                    envVar.getValue().split(","),
                    new String[] {TEST_PLUGIN_PARAM_VALUE_2, TEST_PLUGIN_PARAM_VALUE}));
  }

  @Test
  public void testFeaturesWithTheSamePluginsButDifferentParameters() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_2_NAME, TEST_PLUGIN_PARAM_VALUE_2);
    ChePlugin plugin =
        testPlugin(
            TEST_PLUGIN_ENV_NAME_2,
            TEST_PLUGIN_PARAM_PLACEHOLDER,
            TEST_PLUGIN_ENV_NAME_3,
            TEST_PLUGIN_PARAM_2_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(plugin).when(retriever).getBody(any(URI.class), eq(ChePlugin.class));
    ChePlugin expectedPlugin =
        expectedPluginWithEnv(
            TEST_PLUGIN_ENV_NAME_2,
            TEST_PLUGIN_PARAM_VALUE,
            TEST_PLUGIN_ENV_NAME_3,
            TEST_PLUGIN_PARAM_VALUE_2);

    Collection<ChePlugin> plugins = retriever.get(featuresAttributes);

    assertEquals(plugins, singletonList(expectedPlugin));
  }

  @Test
  public void testFeaturesWithDifferentPluginsButTheSameParameter() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_PLUGIN_2, TEST_PLUGIN_VERSION_2, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    ChePlugin plugin1 =
        testPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN,
            TEST_PLUGIN_VERSION,
            TEST_PLUGIN_ENV_NAME_2,
            TEST_PLUGIN_PARAM_PLACEHOLDER);
    ChePlugin plugin2 =
        testPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN_2,
            TEST_PLUGIN_VERSION_2,
            TEST_PLUGIN_ENV_NAME_3,
            TEST_PLUGIN_PARAM_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(plugin1)
        .when(retriever)
        .getBody(eq(getPluginURI(TEST_PLUGIN, TEST_PLUGIN_VERSION)), eq(ChePlugin.class));
    doReturn(plugin2)
        .when(retriever)
        .getBody(eq(getPluginURI(TEST_PLUGIN_2, TEST_PLUGIN_VERSION_2)), eq(ChePlugin.class));
    ChePlugin expectedPlugin1 =
        expectedPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_ENV_NAME_2, TEST_PLUGIN_PARAM_VALUE);
    ChePlugin expectedPlugin2 =
        expectedPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN_2, TEST_PLUGIN_VERSION_2, TEST_PLUGIN_ENV_NAME_3, TEST_PLUGIN_PARAM_VALUE);

    Collection<ChePlugin> plugins = retriever.get(featuresAttributes);

    assertEqualsNoOrder(plugins.toArray(), new Object[] {expectedPlugin1, expectedPlugin2});
  }

  @Test
  public void testFeaturesWithDifferentPluginsAndDifferentParameters() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_PARAM_NAME, TEST_PLUGIN_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_PLUGIN_2,
            TEST_PLUGIN_VERSION_2,
            TEST_PLUGIN_PARAM_2_NAME,
            TEST_PLUGIN_PARAM_VALUE_2);
    ChePlugin plugin1 =
        testPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN,
            TEST_PLUGIN_VERSION,
            TEST_PLUGIN_ENV_NAME_2,
            TEST_PLUGIN_PARAM_PLACEHOLDER);
    ChePlugin plugin2 =
        testPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN_2,
            TEST_PLUGIN_VERSION_2,
            TEST_PLUGIN_ENV_NAME_3,
            TEST_PLUGIN_PARAM_2_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(plugin1)
        .when(retriever)
        .getBody(eq(getPluginURI(TEST_PLUGIN, TEST_PLUGIN_VERSION)), eq(ChePlugin.class));
    doReturn(plugin2)
        .when(retriever)
        .getBody(eq(getPluginURI(TEST_PLUGIN_2, TEST_PLUGIN_VERSION_2)), eq(ChePlugin.class));
    ChePlugin expectedPlugin1 =
        expectedPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN, TEST_PLUGIN_VERSION, TEST_PLUGIN_ENV_NAME_2, TEST_PLUGIN_PARAM_VALUE);
    ChePlugin expectedPlugin2 =
        expectedPluginWithSpecifiedPluginAndEnv(
            TEST_PLUGIN_2,
            TEST_PLUGIN_VERSION_2,
            TEST_PLUGIN_ENV_NAME_3,
            TEST_PLUGIN_PARAM_VALUE_2);

    Collection<ChePlugin> plugins = retriever.get(featuresAttributes);

    assertEqualsNoOrder(plugins.toArray(), new Object[] {expectedPlugin1, expectedPlugin2});
  }

  private ChePlugin expectedPlugin() {
    return expectedPlugin(TEST_PLUGIN, TEST_PLUGIN_VERSION);
  }

  private ChePlugin expectedPlugin(String pluginName, String pluginVersion) {
    ChePlugin expectedPlugin = new ChePlugin();
    expectedPlugin.setName(pluginName);
    CheContainer expectedContainer = new CheContainer();
    expectedContainer.setEnv(
        new ArrayList<>(
            singletonList(new EnvVar().name(TEST_PLUGIN_ENV_NAME).value(TEST_PLUGIN_ENV_VALUE))));
    expectedContainer.setImage(TEST_PLUGIN_IMAGE);
    expectedContainer.setVolumes(
        singletonList(new Volume().name(TEST_VOLUME_NAME).mountPath(TEST_VOLUME_PATH)));
    expectedContainer.setPorts(
        new ArrayList<>(singletonList(new CheContainerPort().exposedPort(TEST_SERVER_PORT))));
    expectedPlugin.setContainers(new ArrayList<>(singletonList(expectedContainer)));
    expectedPlugin.setEndpoints(
        new ArrayList<>(
            singletonList(
                new ChePluginEndpoint()
                    .name(TEST_SERVER_NAME)
                    .targetPort(TEST_SERVER_PORT)
                    .attributes(TEST_SERVER_ATTRIBUTES))));
    expectedPlugin.setVersion(pluginVersion);
    return expectedPlugin;
  }

  @SuppressWarnings("SameParameterValue")
  private ChePlugin expectedPluginWithEnv(String envName, String envValue) {
    ChePlugin plugin = expectedPlugin();
    plugin.getContainers().get(0).getEnv().add(new EnvVar().name(envName).value(envValue));
    return plugin;
  }

  private ChePlugin expectedPluginWithSpecifiedPluginAndEnv(
      String pluginName, String pluginVersion, String envName, String envValue) {
    ChePlugin plugin = expectedPlugin(pluginName, pluginVersion);
    plugin.getContainers().get(0).getEnv().add(new EnvVar().name(envName).value(envValue));
    return plugin;
  }

  @SuppressWarnings("SameParameterValue")
  private ChePlugin expectedPluginWithEnv(
      String envName, String envValue, String envName2, String envValue2) {
    ChePlugin plugin = expectedPlugin();
    plugin.getContainers().get(0).getEnv().add(new EnvVar().name(envName).value(envValue));
    plugin.getContainers().get(0).getEnv().add(new EnvVar().name(envName2).value(envValue2));
    return plugin;
  }

  private static CheFeature testFeature() {
    CheFeature cheFeature = new CheFeature();
    CheFeatureSpec cheFeatureSpec = new CheFeatureSpec();
    ChePluginReference theiaPluginReference = new ChePluginReference();
    theiaPluginReference.setName(TEST_PLUGIN);
    theiaPluginReference.setVersion(TEST_PLUGIN_VERSION);
    theiaPluginReference.setParameters(new ArrayList<>());
    cheFeatureSpec.setServices(new ArrayList<>(singletonList(theiaPluginReference)));
    cheFeatureSpec.setVersion(TEST_FEATURE_VERSION);
    cheFeature.setSpec(cheFeatureSpec);
    cheFeature.setMetadata(new ObjectMeta().name(TEST_FEATURE_NAME));
    return cheFeature;
  }

  private static CheFeature testFeature(
      String pluginName, String pluginVersion, String paramName, String paramValue) {
    ChePluginReference theiaPluginReference = new ChePluginReference();
    theiaPluginReference.setName(pluginName);
    theiaPluginReference.setVersion(pluginVersion);
    theiaPluginReference.setParameters(
        new ArrayList<>(singletonList(new ChePluginParameter().name(paramName).value(paramValue))));
    CheFeature feature = testFeature();
    feature.getSpec().getServices().add(theiaPluginReference);
    return feature;
  }

  private static ChePlugin testPlugin() {
    return testPlugin(TEST_PLUGIN, TEST_PLUGIN_VERSION);
  }

  private static ChePlugin testPlugin(String pluginName, String pluginVersion) {
    ChePlugin plugin = new ChePlugin();
    CheContainer container = new CheContainer();
    container.setCommands(new ArrayList<>());
    container.setEnv(
        new ArrayList<>(
            ImmutableList.of(
                new EnvVar().name(TEST_PLUGIN_ENV_NAME).value(TEST_PLUGIN_ENV_VALUE))));
    container.setImage(TEST_PLUGIN_IMAGE);
    container.setPorts(
        new ArrayList<>(singletonList(new CheContainerPort().exposedPort(TEST_SERVER_PORT))));
    container.setVolumes(
        new ArrayList<>(
            singletonList(new Volume().name(TEST_VOLUME_NAME).mountPath(TEST_VOLUME_PATH))));
    plugin.setContainers(new ArrayList<>(singletonList(container)));
    plugin.setEndpoints(
        new ArrayList<>(
            singletonList(
                new ChePluginEndpoint()
                    .name(TEST_SERVER_NAME)
                    .targetPort(TEST_SERVER_PORT)
                    .attributes(TEST_SERVER_ATTRIBUTES))));
    plugin.setVersion(pluginVersion);
    plugin.setName(pluginName);
    return plugin;
  }

  private static ChePlugin testPluginWithEnv(String envVarName, String envVarValue) {
    ChePlugin plugin = testPlugin();
    for (CheContainer container : plugin.getContainers()) {
      container.getEnv().add(new EnvVar().name(envVarName).value(envVarValue));
    }
    return plugin;
  }

  private ChePlugin testPluginWithSpecifiedPluginAndEnv(
      String pluginName, String pluginVersion, String envVarName, String envVarValue) {
    ChePlugin plugin = testPlugin(pluginName, pluginVersion);
    for (CheContainer container : plugin.getContainers()) {
      container.getEnv().add(new EnvVar().name(envVarName).value(envVarValue));
    }
    return plugin;
  }

  @SuppressWarnings("SameParameterValue")
  private ChePlugin testPlugin(
      String envVarName, String envVarValue, String envVarName2, String envVarValue2) {
    ChePlugin plugin = testPlugin();
    for (CheContainer container : plugin.getContainers()) {
      container.getEnv().add(new EnvVar().name(envVarName).value(envVarValue));
      container.getEnv().add(new EnvVar().name(envVarName2).value(envVarValue2));
    }
    return plugin;
  }

  private URI getFeatureURI(String featureName, String featureVersion) throws URISyntaxException {
    return new URI(API_ENDPOINT + "/feature/" + featureName + '/' + featureVersion);
  }

  private URI getPluginURI(String testPlugin, String testPluginVersion) throws URISyntaxException {
    return new URI(API_ENDPOINT + "/service/" + testPlugin + '/' + testPluginVersion);
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, String> featuresAttribute(
      String feature1Name, String feature1Version, String feature2Name, String feature2Version) {
    return ImmutableMap.of(
        WORKSPACE_NEXT_FEATURES,
        feature1Name + "/" + feature1Version + "," + feature2Name + "/" + feature2Version);
  }
}
