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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEqualsNoOrder;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class K8sContainerResolverBuilderTest {
  @Test(dataProvider = "allFieldsSetProvider", expectedExceptions = IllegalStateException.class)
  public void shouldCheckThatAllFieldsAreSet(K8sContainerResolverBuilder builder) {
    builder.build();
  }

  @DataProvider
  public static Object[][] allFieldsSetProvider() {
    return new Object[][] {
      {new K8sContainerResolverBuilder()},
      {new K8sContainerResolverBuilder().setContainer(new CheContainer())},
      {new K8sContainerResolverBuilder().setPluginEndpoints(new ArrayList<>())},
    };
  }

  @Test
  public void shouldPassOnlyParticularContainerEndpoints() {
    // given
    K8sContainerResolverBuilder builder = new K8sContainerResolverBuilder();
    builder.setContainer(
        new CheContainer()
            .ports(
                asList(
                    new CheContainerPort().exposedPort(9014),
                    new CheContainerPort().exposedPort(4040))));
    builder.setPluginEndpoints(
        asList(
            new ChePluginEndpoint().targetPort(9014),
            new ChePluginEndpoint().targetPort(9013),
            new ChePluginEndpoint().targetPort(8080),
            new ChePluginEndpoint().targetPort(4040)));
    K8sContainerResolver resolver = builder.build();
    ArrayList<ChePluginEndpoint> expected = new ArrayList<>();
    expected.add(new ChePluginEndpoint().targetPort(9014));
    expected.add(new ChePluginEndpoint().targetPort(4040));

    // when
    List<ChePluginEndpoint> actualEndpoints = resolver.getEndpoints();

    // then
    assertEqualsNoOrder(actualEndpoints.toArray(), expected.toArray());
  }
}
