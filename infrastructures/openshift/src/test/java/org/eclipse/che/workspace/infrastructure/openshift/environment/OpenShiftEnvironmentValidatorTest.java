/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentPodsValidator;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentValidator}.
 *
 * @author Angel Misevski
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftEnvironmentValidatorTest {
  @Mock private KubernetesEnvironmentPodsValidator podsValidator;

  @Mock private OpenShiftEnvironment environment;

  @InjectMocks private OpenShiftEnvironmentValidator envValidator;

  @Test
  public void shouldDoNothingWhenRoutesMatchServices() throws Exception {
    // Given
    Map<String, Service> services = ImmutableMap.of("service1", makeService("service1"));
    Map<String, Route> routes = ImmutableMap.of("route1", makeRoute("route1", "service1"));
    when(environment.getRoutes()).thenReturn(routes);
    when(environment.getServices()).thenReturn(services);

    // When
    envValidator.validate(environment);

    // Then (nothing)
  }

  @Test
  public void shouldDoNothingWhenRoutesDoNotHaveToField() throws Exception {
    // Given
    Map<String, Route> routes = ImmutableMap.of("route1", makeRoute("route1", null));
    when(environment.getRoutes()).thenReturn(routes);

    // When
    envValidator.validate(environment);

    // Then (nothing)
  }

  @Test(expectedExceptions = ValidationException.class)
  public void shouldThrowExceptionWhenRouteRefersToMissingService() throws Exception {
    // Given
    Map<String, Service> services = ImmutableMap.of("service1", makeService("service1"));
    Map<String, Route> routes = ImmutableMap.of("route1", makeRoute("route1", "notservice1"));
    when(environment.getRoutes()).thenReturn(routes);
    when(environment.getServices()).thenReturn(services);

    // When
    envValidator.validate(environment);

    // Then (ValidationException)
  }

  private Service makeService(String serviceName) {
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(serviceName)
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }

  /**
   * Make route for use in tests. If serviceName is null, return a route that does not refer to a
   * service.
   */
  private Route makeRoute(String routeName, String serviceName) {
    RouteBuilder routeBuilder =
        new RouteBuilder().withNewMetadata().withName(routeName).endMetadata();
    if (serviceName != null) {
      routeBuilder
          .withNewSpec()
          .withNewTo()
          .withKind("Service")
          .withName(serviceName)
          .endTo()
          .endSpec();
    } else {
      routeBuilder.withNewSpec().endSpec();
    }
    return routeBuilder.build();
  }
}
