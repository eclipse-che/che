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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ServicesTest {

  @Test(dataProvider = "nullService")
  public void testFindPortShouldReturnEmptyWhenSomethingIsNull(Service service) {
    assertEquals(Services.findPort(service, 1), Optional.empty());
  }

  @Test
  public void testFindPortWhenExists() {
    final int PORT = 1234;

    Service service = new Service();
    ServiceSpec spec = new ServiceSpec();
    ServicePort port = new ServicePort();
    port.setPort(PORT);
    spec.setPorts(Arrays.asList(port, new ServicePort()));
    service.setSpec(spec);

    assertEquals(Services.findPort(service, PORT).get(), port);
  }

  @Test(dataProvider = "nullServices")
  public void testFindServiceWithPortShouldReturnEmptyWhenSomethingIsNull(
      Collection<Service> services) {
    assertEquals(Services.findServiceWithPort(services, 1), Optional.empty());
  }

  @Test
  public void testFindServiceWhenExists() {
    final int PORT = 1234;

    Service service = new Service();
    ServiceSpec spec = new ServiceSpec();
    ServicePort port = new ServicePort();
    port.setPort(PORT);
    spec.setPorts(Collections.singletonList(port));
    service.setSpec(spec);

    assertEquals(
        Services.findServiceWithPort(Arrays.asList(service, new Service()), PORT).get(), service);
  }

  @DataProvider
  public Object[][] nullService() {
    List<Service> nullServices = createNullServices();
    Object[][] returnObjects = new Object[nullServices.size()][1];
    for (int i = 0; i < nullServices.size(); i++) {
      returnObjects[i][0] = nullServices.get(i);
    }

    return returnObjects;
  }

  @DataProvider
  public Object[][] nullServices() {
    List<Service> nullServices = createNullServices();

    Object[][] returnObjects = new Object[nullServices.size() + 1][1];
    for (int i = 0; i < nullServices.size(); i++) {
      returnObjects[i][0] = Collections.singletonList(nullServices.get(i));
    }
    returnObjects[returnObjects.length - 1][0] = null;

    return returnObjects;
  }

  private List<Service> createNullServices() {
    List<Service> nullServices = new ArrayList<>();

    nullServices.add(null);

    Service service = new Service();
    service.setSpec(null);
    nullServices.add(service);

    service = new Service();
    ServiceSpec spec = new ServiceSpec();
    spec.setPorts(null);
    service.setSpec(spec);
    nullServices.add(service);

    service = new Service();
    spec = new ServiceSpec();
    ServicePort port = new ServicePort();
    port.setPort(null);
    spec.setPorts(Collections.singletonList(port));
    service.setSpec(spec);
    nullServices.add(service);

    return nullServices;
  }
}
