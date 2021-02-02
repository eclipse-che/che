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

import static java.lang.String.format;
import static org.eclipse.che.api.factory.shared.Constants.URL_PARAMETER_NAME;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PLUGIN_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;
import org.eclipse.che.api.factory.server.urlfactory.URLFactoryBuilder;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.URLFileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.schema.DevfileSchemaProvider;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator.NoopComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
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

  @Test
  public void shouldResolveRelativeFiles() throws Exception {
    // given

    // we need to set up an "almost real" devfile converter which is a little bit involved
    DevfileSchemaValidator validator =
        new DevfileSchemaValidator(new DevfileSchemaProvider(), new DevfileVersionDetector());

    Map<String, ComponentIntegrityValidator> validators = new HashMap<>();
    validators.put(EDITOR_COMPONENT_TYPE, new NoopComponentIntegrityValidator());
    validators.put(PLUGIN_COMPONENT_TYPE, new NoopComponentIntegrityValidator());
    validators.put(KUBERNETES_COMPONENT_TYPE, new NoopComponentIntegrityValidator());
    validators.put(OPENSHIFT_COMPONENT_TYPE, new NoopComponentIntegrityValidator());

    DevfileIntegrityValidator integrityValidator = new DevfileIntegrityValidator(validators);

    DevfileParser devfileParser = new DevfileParser(validator, integrityValidator);

    URLFactoryBuilder factoryBuilder =
        new URLFactoryBuilder("editor", "plugin", devfileParser, new DevfileVersionDetector());

    DefaultFactoryParameterResolver res =
        new DefaultFactoryParameterResolver(factoryBuilder, urlFetcher);

    // set up our factory with the location of our devfile that is referencing our localfile
    Map<String, String> factoryParameters = new HashMap<>();
    factoryParameters.put(URL_PARAMETER_NAME, "http://myloc.com/aa/bb/devfile");
    doReturn(DEVFILE).when(urlFetcher).fetch(eq("http://myloc.com/aa/bb/devfile"));
    doReturn("localfile").when(urlFetcher).fetch("http://myloc.com/aa/localfile");

    // when
    res.createFactory(factoryParameters);

    // then
    verify(urlFetcher).fetch(eq("http://myloc.com/aa/localfile"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFilterAndProvideOverrideParameters() throws Exception {
    URLFactoryBuilder urlFactoryBuilder = mock(URLFactoryBuilder.class);
    URLFetcher urlFetcher = mock(URLFetcher.class);

    DefaultFactoryParameterResolver res =
        new DefaultFactoryParameterResolver(urlFactoryBuilder, urlFetcher);

    Map<String, String> factoryParameters = new HashMap<>();
    factoryParameters.put(URL_PARAMETER_NAME, "http://myloc/devfile");
    factoryParameters.put("override.param.foo", "bar");
    factoryParameters.put("override.param.bar", "foo");
    factoryParameters.put("ignored.non-override.property", "baz");

    ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
    // when
    res.createFactory(factoryParameters);

    verify(urlFactoryBuilder)
        .createFactoryFromDevfile(
            any(RemoteFactoryUrl.class), any(URLFileContentProvider.class), captor.capture());
    Map<String, String> filteredOverrides = captor.getValue();
    assertEquals(2, filteredOverrides.size());
    assertEquals("bar", filteredOverrides.get("param.foo"));
    assertEquals("foo", filteredOverrides.get("param.bar"));
    assertFalse(filteredOverrides.containsKey("ignored.non-override.property"));
  }

  @Test(dataProvider = "invalidURLsProvider")
  public void shouldThrowExceptionOnInvalidURL(String url, String message) throws Exception {
    URLFactoryBuilder urlFactoryBuilder = mock(URLFactoryBuilder.class);
    URLFetcher urlFetcher = mock(URLFetcher.class);

    DefaultFactoryParameterResolver res =
        new DefaultFactoryParameterResolver(urlFactoryBuilder, urlFetcher);

    Map<String, String> factoryParameters = new HashMap<>();
    factoryParameters.put(URL_PARAMETER_NAME, url);

    // when
    try {
      res.createFactory(factoryParameters);
      fail("Exception is expected");
    } catch (BadRequestException e) {
      assertEquals(
          e.getMessage(),
          format(
              "Unable to process provided factory URL. Please check its validity and try again. Parser message: %s",
              message));
    }
  }

  @DataProvider(name = "invalidURLsProvider")
  private Object[][] invalidUrlsProvider() {
    return new Object[][] {
      {"C:\\Users\\aa\\bb\\XX\\", "unknown protocol: c"},
      {
        "https://github.com/ .git",
        "Illegal character in path at index 19: https://github.com/ .git"
      },
      {"unknown:///abc.dce", "unknown protocol: unknown"}
    };
  }
}
