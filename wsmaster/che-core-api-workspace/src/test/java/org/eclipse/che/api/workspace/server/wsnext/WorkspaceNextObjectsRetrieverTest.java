/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext;

import static java.util.Collections.emptyList;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsnext.model.CheFeature;
import org.eclipse.che.api.workspace.server.wsnext.model.CheFeatureSpec;
import org.eclipse.che.api.workspace.server.wsnext.model.CheService;
import org.eclipse.che.api.workspace.server.wsnext.model.CheServiceParameter;
import org.eclipse.che.api.workspace.server.wsnext.model.CheServiceReference;
import org.eclipse.che.api.workspace.server.wsnext.model.CheServiceSpec;
import org.eclipse.che.api.workspace.server.wsnext.model.Container;
import org.eclipse.che.api.workspace.server.wsnext.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsnext.model.ObjectMeta;
import org.eclipse.che.api.workspace.server.wsnext.model.ResourceRequirements;
import org.eclipse.che.api.workspace.server.wsnext.model.Server;
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
  private static final String TEST_SERVICE = "my-service";
  private static final String TEST_SERVICE_2 = "my-test-service-2";
  private static final String TEST_SERVICE_VERSION = "0.0.0";
  private static final String TEST_SERVICE_VERSION_2 = "1.0.0-SNAPSHOT";
  private static final String TEST_SERVICE_ENV_NAME = "MY_ENV";
  private static final String TEST_SERVICE_ENV_NAME_2 = "MY_ENV_2";
  private static final String TEST_SERVICE_ENV_NAME_3 = "MY_ENV_3";
  private static final String TEST_SERVICE_ENV_VALUE = "MY_VALUE";
  private static final String TEST_SERVICE_PARAM_NAME = "MY_PARAMETER_1";
  private static final String TEST_SERVICE_PARAM_2_NAME = "TEST_PARAM_2";
  private static final String TEST_SERVICE_PARAM_PLACEHOLDER = "${" + TEST_SERVICE_PARAM_NAME + "}";
  private static final String TEST_SERVICE_PARAM_2_PLACEHOLDER =
      "${" + TEST_SERVICE_PARAM_2_NAME + "}";
  private static final String TEST_SERVICE_PARAM_VALUE = "http://github.com/plugin1";
  private static final String TEST_SERVICE_PARAM_VALUE_2 = "http://github.com/plugin2";
  private static final String TEST_SERVICE_IMAGE = "test/image:tag";
  private static final String TEST_SERVER_NAME = "serv1";
  private static final int TEST_SERVER_PORT = 9090;
  private static final String TEST_SERVER_PROTOCOL = "https";
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

    Collection<CheService> services = retriever.get(SINGLE_FEATURE_ATTRIBUTES);

    assertTrue(services.isEmpty());
  }

  @Test
  public void doesNothingWhenAttributesAreEmpty() throws Exception {
    Collection<CheService> services = retriever.get(emptyMap());

    assertTrue(services.isEmpty());
  }

  @Test
  public void doesNothingWhenAttributesAreNull() throws Exception {
    Collection<CheService> services = retriever.get(null);

    assertTrue(services.isEmpty());
  }

  @Test
  public void doesNothingWhenThereIsNoFeaturesAttribute() throws Exception {
    Collection<CheService> services = retriever.get(singletonMap("not_feature_attribute", "value"));

    assertTrue(services.isEmpty());
  }

  @Test
  public void doesNothingWhenFeaturesAttributeIsEmpty() throws Exception {
    Collection<CheService> services = retriever.get(singletonMap(WORKSPACE_NEXT_FEATURES, ""));

    assertTrue(services.isEmpty());
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
    expectedExceptionsMessageRegExp = "Error occurred on retrieval of service .*. Error: test error"
  )
  public void throwsExceptionWhenServiceRetrievalFails() throws Exception {
    CheFeature cheFeature = testFeature();
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doThrow(new IOException("test error"))
        .when(retriever)
        .getBody(any(URI.class), eq(CheService.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Service '.*/.*' configuration is invalid. Version is missing."
  )
  public void throwExceptionIfServiceVersionIsNull() throws Exception {
    CheFeature cheFeature = testFeature();
    CheService service = testService();
    service.getSpec().setVersion(null);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Service '.*/.*' configuration is invalid. Version is missing."
  )
  public void throwExceptionIfServiceVersionIsEmpty() throws Exception {
    CheFeature cheFeature = testFeature();
    CheService service = testService();
    service.getSpec().setVersion("");
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "Service '.*/.*' configuration is invalid. Name is missing."
  )
  public void throwExceptionIfServiceNameIsNull() throws Exception {
    CheFeature cheFeature = testFeature();
    CheService service = testService();
    service.getMetadata().setName(null);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp = "Service '.*/.*' configuration is invalid. Name is missing."
  )
  public void throwExceptionIfServiceNameIsEmpty() throws Exception {
    CheFeature cheFeature = testFeature();
    CheService service = testService();
    service.getMetadata().setName("");
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Parameters not supported by services found: .*" + TEST_SERVICE_PARAM_NAME + ".*"
  )
  public void throwExceptionIfValueForServiceParameterNotFound() throws Exception {
    CheFeature cheFeature =
        testFeature(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_PARAM_NAME, TEST_SERVICE_PARAM_VALUE);
    CheService service = testServiceWithEnv(TEST_SERVICE_ENV_NAME, TEST_SERVICE_ENV_VALUE);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    retriever.get(SINGLE_FEATURE_ATTRIBUTES);
  }

  @Test
  public void testFeaturesWithoutParameters() throws Exception {
    CheFeature cheFeature = testFeature();
    CheService service = testService();
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    Collection<CheService> services = retriever.get(SINGLE_FEATURE_ATTRIBUTES);

    assertEquals(services, singletonList(expectedService()));
  }

  @Test
  public void testFeaturesWithParameter() throws Exception {
    CheFeature cheFeature =
        testFeature(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_PARAM_NAME, TEST_SERVICE_PARAM_VALUE);
    CheService service = testServiceWithEnv(TEST_SERVICE_ENV_NAME, TEST_SERVICE_PARAM_PLACEHOLDER);
    doReturn(cheFeature).when(retriever).getBody(any(URI.class), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    Collection<CheService> services = retriever.get(SINGLE_FEATURE_ATTRIBUTES);

    assertEquals(
        services,
        singletonList(expectedServiceWithEnv(TEST_SERVICE_ENV_NAME, TEST_SERVICE_PARAM_VALUE)));
  }

  @Test
  public void testFeaturesWithTheSameServicesAndTheSameParameters() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_PARAM_NAME, TEST_SERVICE_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_SERVICE,
            TEST_SERVICE_VERSION,
            TEST_SERVICE_PARAM_NAME,
            TEST_SERVICE_PARAM_VALUE_2);
    CheService service =
        testServiceWithEnv(TEST_SERVICE_ENV_NAME_2, TEST_SERVICE_PARAM_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));

    Collection<CheService> services = retriever.get(featuresAttributes);

    // we don't know the order of values in env var from feature, so we have to match it separately
    // - can't assert whole object
    assertEquals(services.size(), 1);
    CheService actualService = services.iterator().next();
    assertEquals(actualService.getSpec().getContainers().size(), 1);
    List<EnvVar> envVars = actualService.getSpec().getContainers().iterator().next().getEnv();
    envVars
        .stream()
        .filter(envVar -> envVar.getName().equals(TEST_SERVICE_ENV_NAME_2))
        .forEach(
            envVar -> {
              assertEqualsNoOrder(
                  envVar.getValue().split(","),
                  new String[] {TEST_SERVICE_PARAM_VALUE_2, TEST_SERVICE_PARAM_VALUE});
            });
  }

  @Test
  public void testFeaturesWithTheSameServicesButDifferentParameters() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_PARAM_NAME, TEST_SERVICE_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_SERVICE,
            TEST_SERVICE_VERSION,
            TEST_SERVICE_PARAM_2_NAME,
            TEST_SERVICE_PARAM_VALUE_2);
    CheService service =
        testService(
            TEST_SERVICE_ENV_NAME_2,
            TEST_SERVICE_PARAM_PLACEHOLDER,
            TEST_SERVICE_ENV_NAME_3,
            TEST_SERVICE_PARAM_2_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(service).when(retriever).getBody(any(URI.class), eq(CheService.class));
    CheService expectedService =
        expectedServiceWithEnv(
            TEST_SERVICE_ENV_NAME_2,
            TEST_SERVICE_PARAM_VALUE,
            TEST_SERVICE_ENV_NAME_3,
            TEST_SERVICE_PARAM_VALUE_2);

    Collection<CheService> services = retriever.get(featuresAttributes);

    assertEquals(services, singletonList(expectedService));
  }

  @Test
  public void testFeaturesWithDifferentServicesButTheSameParameter() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_PARAM_NAME, TEST_SERVICE_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_SERVICE_2,
            TEST_SERVICE_VERSION_2,
            TEST_SERVICE_PARAM_NAME,
            TEST_SERVICE_PARAM_VALUE);
    CheService service1 =
        testServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE,
            TEST_SERVICE_VERSION,
            TEST_SERVICE_ENV_NAME_2,
            TEST_SERVICE_PARAM_PLACEHOLDER);
    CheService service2 =
        testServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE_2,
            TEST_SERVICE_VERSION_2,
            TEST_SERVICE_ENV_NAME_3,
            TEST_SERVICE_PARAM_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(service1)
        .when(retriever)
        .getBody(eq(getServiceURI(TEST_SERVICE, TEST_SERVICE_VERSION)), eq(CheService.class));
    doReturn(service2)
        .when(retriever)
        .getBody(eq(getServiceURI(TEST_SERVICE_2, TEST_SERVICE_VERSION_2)), eq(CheService.class));
    CheService expectedService1 =
        expectedServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_ENV_NAME_2, TEST_SERVICE_PARAM_VALUE);
    CheService expectedService2 =
        expectedServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE_2,
            TEST_SERVICE_VERSION_2,
            TEST_SERVICE_ENV_NAME_3,
            TEST_SERVICE_PARAM_VALUE);

    Collection<CheService> services = retriever.get(featuresAttributes);

    assertEquals(services, Arrays.asList(expectedService1, expectedService2));
  }

  @Test
  public void testFeaturesWithDifferentServicesAndDifferentParameters() throws Exception {
    Map<String, String> featuresAttributes =
        featuresAttribute(
            TEST_FEATURE_NAME, TEST_FEATURE_VERSION, TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION);
    CheFeature feature1 =
        testFeature(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_PARAM_NAME, TEST_SERVICE_PARAM_VALUE);
    CheFeature feature2 =
        testFeature(
            TEST_SERVICE_2,
            TEST_SERVICE_VERSION_2,
            TEST_SERVICE_PARAM_2_NAME,
            TEST_SERVICE_PARAM_VALUE_2);
    CheService service1 =
        testServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE,
            TEST_SERVICE_VERSION,
            TEST_SERVICE_ENV_NAME_2,
            TEST_SERVICE_PARAM_PLACEHOLDER);
    CheService service2 =
        testServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE_2,
            TEST_SERVICE_VERSION_2,
            TEST_SERVICE_ENV_NAME_3,
            TEST_SERVICE_PARAM_2_PLACEHOLDER);
    doReturn(feature1)
        .when(retriever)
        .getBody(eq(getFeatureURI(TEST_FEATURE_NAME, TEST_FEATURE_VERSION)), eq(CheFeature.class));
    doReturn(feature2)
        .when(retriever)
        .getBody(
            eq(getFeatureURI(TEST_FEATURE_2_NAME, TEST_FEATURE_2_VERSION)), eq(CheFeature.class));
    doReturn(service1)
        .when(retriever)
        .getBody(eq(getServiceURI(TEST_SERVICE, TEST_SERVICE_VERSION)), eq(CheService.class));
    doReturn(service2)
        .when(retriever)
        .getBody(eq(getServiceURI(TEST_SERVICE_2, TEST_SERVICE_VERSION_2)), eq(CheService.class));
    CheService expectedService1 =
        expectedServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE, TEST_SERVICE_VERSION, TEST_SERVICE_ENV_NAME_2, TEST_SERVICE_PARAM_VALUE);
    CheService expectedService2 =
        expectedServiceWithSpecifiedServiceAndEnv(
            TEST_SERVICE_2,
            TEST_SERVICE_VERSION_2,
            TEST_SERVICE_ENV_NAME_3,
            TEST_SERVICE_PARAM_VALUE_2);

    Collection<CheService> services = retriever.get(featuresAttributes);

    assertEquals(services, Arrays.asList(expectedService1, expectedService2));
  }

  private CheService expectedService() {
    return expectedService(TEST_SERVICE, TEST_SERVICE_VERSION);
  }

  private CheService expectedService(String serviceName, String serviceVersion) {
    CheService expectedService = new CheService();
    expectedService.setMetadata(new ObjectMeta().name(serviceName));
    CheServiceSpec expectedServiceSpec = new CheServiceSpec();
    Container expectedContainer = new Container();
    expectedContainer.setEnv(
        new ArrayList<>(
            singletonList(new EnvVar().name(TEST_SERVICE_ENV_NAME).value(TEST_SERVICE_ENV_VALUE))));
    expectedContainer.setImage(TEST_SERVICE_IMAGE);
    expectedContainer.setVolumes(
        singletonList(new Volume().name(TEST_VOLUME_NAME).mountPath(TEST_VOLUME_PATH)));
    expectedContainer.setServers(
        singletonList(
            new Server()
                .name(TEST_SERVER_NAME)
                .port(TEST_SERVER_PORT)
                .protocol(TEST_SERVER_PROTOCOL)
                .attributes(TEST_SERVER_ATTRIBUTES)));
    expectedContainer.setResources(new ResourceRequirements());
    expectedServiceSpec.setContainers(singletonList(expectedContainer));
    expectedServiceSpec.setVersion(serviceVersion);
    expectedService.setSpec(expectedServiceSpec);
    return expectedService;
  }

  private CheService expectedServiceWithEnv(String envName, String envValue) {
    CheService service = expectedService();
    service
        .getSpec()
        .getContainers()
        .get(0)
        .getEnv()
        .add(new EnvVar().name(envName).value(envValue));
    return service;
  }

  private CheService expectedServiceWithSpecifiedServiceAndEnv(
      String serviceName, String serviceVersion, String envName, String envValue) {
    CheService service = expectedService(serviceName, serviceVersion);
    service
        .getSpec()
        .getContainers()
        .get(0)
        .getEnv()
        .add(new EnvVar().name(envName).value(envValue));
    return service;
  }

  private CheService expectedServiceWithEnv(
      String envName, String envValue, String envName2, String envValue2) {
    CheService service = expectedService();
    service
        .getSpec()
        .getContainers()
        .get(0)
        .getEnv()
        .add(new EnvVar().name(envName).value(envValue));
    service
        .getSpec()
        .getContainers()
        .get(0)
        .getEnv()
        .add(new EnvVar().name(envName2).value(envValue2));
    return service;
  }

  private static CheFeature testFeature() {
    CheFeature cheFeature = new CheFeature();
    CheFeatureSpec cheFeatureSpec = new CheFeatureSpec();
    CheServiceReference theiaServiceReference = new CheServiceReference();
    theiaServiceReference.setName(TEST_SERVICE);
    theiaServiceReference.setVersion(TEST_SERVICE_VERSION);
    theiaServiceReference.setParameters(new ArrayList<>());
    cheFeatureSpec.setServices(new ArrayList<>(singletonList(theiaServiceReference)));
    cheFeatureSpec.setVersion(TEST_FEATURE_VERSION);
    cheFeature.setSpec(cheFeatureSpec);
    cheFeature.setMetadata(new ObjectMeta().name(TEST_FEATURE_NAME));
    return cheFeature;
  }

  private static CheFeature testFeature(
      String serviceName, String serviceVersion, String paramName, String paramValue) {
    CheServiceReference theiaServiceReference = new CheServiceReference();
    theiaServiceReference.setName(serviceName);
    theiaServiceReference.setVersion(serviceVersion);
    theiaServiceReference.setParameters(
        new ArrayList<>(
            singletonList(new CheServiceParameter().name(paramName).value(paramValue))));
    CheFeature feature = testFeature();
    feature.getSpec().getServices().add(theiaServiceReference);
    return feature;
  }

  private static CheService testService() {
    return testService(TEST_SERVICE, TEST_SERVICE_VERSION);
  }

  private static CheService testService(String serviceName, String serviceVersion) {
    CheService service = new CheService();
    CheServiceSpec cheServiceSpec = new CheServiceSpec();
    Container container = new Container();
    container.setCommands(emptyList());
    container.setEnv(
        new ArrayList<>(
            ImmutableList.of(
                new EnvVar().name(TEST_SERVICE_ENV_NAME).value(TEST_SERVICE_ENV_VALUE))));
    container.setImage(TEST_SERVICE_IMAGE);
    container.setResources(new ResourceRequirements());
    container.setServers(
        singletonList(
            new Server()
                .name(TEST_SERVER_NAME)
                .port(TEST_SERVER_PORT)
                .protocol(TEST_SERVER_PROTOCOL)
                .attributes(TEST_SERVER_ATTRIBUTES)));
    container.setVolumes(
        singletonList(new Volume().name(TEST_VOLUME_NAME).mountPath(TEST_VOLUME_PATH)));
    cheServiceSpec.setContainers(singletonList(container));
    cheServiceSpec.setVersion(serviceVersion);
    service.setSpec(cheServiceSpec);
    service.setMetadata(new ObjectMeta().name(serviceName));
    return service;
  }

  private static CheService testServiceWithEnv(String envVarName, String envVarValue) {
    CheService service = testService();
    for (Container container : service.getSpec().getContainers()) {
      container.getEnv().add(new EnvVar().name(envVarName).value(envVarValue));
    }
    return service;
  }

  private CheService testServiceWithSpecifiedServiceAndEnv(
      String serviceName, String serviceVersion, String envVarName, String envVarValue) {
    CheService service = testService(serviceName, serviceVersion);
    for (Container container : service.getSpec().getContainers()) {
      container.getEnv().add(new EnvVar().name(envVarName).value(envVarValue));
    }
    return service;
  }

  private CheService testService(
      String envVarName, String envVarValue, String envVarName2, String envVarValue2) {
    CheService service = testService();
    for (Container container : service.getSpec().getContainers()) {
      container.getEnv().add(new EnvVar().name(envVarName).value(envVarValue));
      container.getEnv().add(new EnvVar().name(envVarName2).value(envVarValue2));
    }
    return service;
  }

  private URI getFeatureURI(String featureName, String featureVersion) throws URISyntaxException {
    return new URI(API_ENDPOINT + "/feature/" + featureName + '/' + featureVersion);
  }

  private URI getServiceURI(String testService, String testServiceVersion)
      throws URISyntaxException {
    return new URI(API_ENDPOINT + "/service/" + testService + '/' + testServiceVersion);
  }

  private Map<String, String> featuresAttribute(
      String feature1Name, String feature1Version, String feature2Name, String feature2Version) {
    return ImmutableMap.of(
        WORKSPACE_NEXT_FEATURES,
        feature1Name + "/" + feature1Version + "," + feature2Name + "/" + feature2Version);
  }
}
