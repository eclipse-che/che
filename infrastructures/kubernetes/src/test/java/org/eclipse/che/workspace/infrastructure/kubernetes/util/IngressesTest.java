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

package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPath;
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressRuleValue;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBackend;
import io.fabric8.kubernetes.api.model.extensions.IngressRule;
import io.fabric8.kubernetes.api.model.extensions.IngressSpec;
import java.util.Optional;
import org.testng.annotations.Test;

public class IngressesTest {

  @Test
  public void findHostWhenPortDefinedByString() {
    final String SERVER_PORT_NAME = "server-8080";
    final int PORT = 8080;

    Service service = createService(SERVER_PORT_NAME, PORT);
    Ingress ingress = createIngress(new IngressBackend(null, "servicename", new IntOrString(PORT)));

    Optional<IngressRule> foundRule =
        Ingresses.findIngressRuleForServicePort(singletonList(ingress), service, PORT);
    assertTrue(foundRule.isPresent());
    assertEquals(foundRule.get().getHost(), "ingresshost");
  }

  @Test
  public void findHostWhenPortDefinedByInt() {
    final String SERVER_PORT_NAME = "server-8080";
    final int PORT = 8080;

    Service service = createService(SERVER_PORT_NAME, PORT);
    Ingress ingress =
        createIngress(new IngressBackend(null, "servicename", new IntOrString(SERVER_PORT_NAME)));

    Optional<IngressRule> foundRule =
        Ingresses.findIngressRuleForServicePort(singletonList(ingress), service, PORT);
    assertTrue(foundRule.isPresent());
    assertEquals(foundRule.get().getHost(), "ingresshost");
  }

  @Test
  public void emptyWhenPortByStringAndNotFound() {
    final String SERVER_PORT_NAME = "server-8080";
    final int PORT = 8080;

    Service service = createService(SERVER_PORT_NAME, PORT);
    Ingress ingress =
        createIngress(new IngressBackend(null, "servicename", new IntOrString("does not exist")));

    Optional<IngressRule> foundRule =
        Ingresses.findIngressRuleForServicePort(singletonList(ingress), service, PORT);
    assertFalse(foundRule.isPresent());
  }

  @Test
  public void emptyWhenPortByIntAndNotFound() {
    final String SERVER_PORT_NAME = "server-8080";
    final int PORT = 8080;

    Service service = createService(SERVER_PORT_NAME, PORT);
    Ingress ingress = createIngress(new IngressBackend(null, "servicename", new IntOrString(666)));

    Optional<IngressRule> foundRule =
        Ingresses.findIngressRuleForServicePort(singletonList(ingress), service, PORT);
    assertFalse(foundRule.isPresent());
  }

  private Service createService(String serverPortName, int port) {
    Service service = new Service();
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName("servicename");
    service.setMetadata(serviceMeta);
    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setPorts(
        singletonList(
            new ServicePort(null, serverPortName, null, port, "TCP", new IntOrString(port))));
    service.setSpec(serviceSpec);
    return service;
  }

  private Ingress createIngress(IngressBackend backend) {
    Ingress ingress = new Ingress();
    ObjectMeta ingressMeta = new ObjectMeta();
    ingressMeta.setName("ingressname");
    ingress.setMetadata(ingressMeta);
    IngressSpec ingressSpec = new IngressSpec();
    IngressRule ingressRule = new IngressRule();
    ingressRule.setHost("ingresshost");
    ingressRule.setHttp(
        new HTTPIngressRuleValue(singletonList(new HTTPIngressPath(backend, null, null))));
    ingressSpec.setRules(singletonList(ingressRule));
    ingress.setSpec(ingressSpec);
    return ingress;
  }
}
