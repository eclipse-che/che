package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DISCOVERABLE_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier.CHE_COMPONENT_NAME_LABEL;
import static org.testng.Assert.*;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ComponentToKubernetesConverterTest {

  private ComponentToKubernetesConverter componentToKubernetesConverter;

  @BeforeMethod
  public void setUp() {
    this.componentToKubernetesConverter = new ComponentToKubernetesConverter();
  }

  @Test
  public void testConvertComponentWithSingleEndpointToService() {
    // given
    ComponentImpl component = new ComponentImpl("kubernetes", "123");
    component.setEndpoints(singletonList(new EndpointImpl("test-endpoint", 1234,
        singletonMap(DISCOVERABLE_ENDPOINT_ATTRIBUTE, Boolean.TRUE.toString()))));

    // when
    List<Service> services = componentToKubernetesConverter
        .publicEndpointsToServices(component, "hello");

    // then
    assertEquals(services.size(), 1);
    Service service = services.get(0);
    assertEquals(service.getMetadata().getName(), "test-endpoint");
    assertEquals(service.getSpec().getSelector().get(CHE_COMPONENT_NAME_LABEL), "hello");
    List<ServicePort> ports = service.getSpec().getPorts();
    assertEquals(ports.size(), 1);
    assertEquals(ports.get(0).getPort().intValue(), 1234);
    assertEquals(ports.get(0).getTargetPort().getIntVal().intValue(), 1234);
    assertEquals(ports.get(0).getProtocol(), "TCP");
  }


  @Test
  public void testComponentWithNoEndpointsConvertsToEmptyList() {
    // given-when
    List<Service> services = componentToKubernetesConverter
        .publicEndpointsToServices(new ComponentImpl("kubernetes", "123"), "c1");

    // then
    assertTrue(services.isEmpty());
  }

  @Test
  public void testComponentWithMoreEndpointsConvertsToMultipleServices() {
    // given
    ComponentImpl component = new ComponentImpl("kubernetes", "123");
    component.setEndpoints(Arrays.asList(
        new EndpointImpl("test-endpoint", 1234, singletonMap(DISCOVERABLE_ENDPOINT_ATTRIBUTE, Boolean.TRUE.toString())),
        new EndpointImpl("test-endpoint2", 1234, singletonMap(DISCOVERABLE_ENDPOINT_ATTRIBUTE, Boolean.TRUE.toString()))));

    // when
    List<Service> services = componentToKubernetesConverter
        .publicEndpointsToServices(component, "hello");

    // then
    assertFalse(services.isEmpty());
    assertEquals(services.size(), 2);
    assertTrue(services.stream().anyMatch(s -> s.getMetadata().getName().equals("test-endpoint")));
    assertTrue(services.stream().anyMatch(s -> s.getMetadata().getName().equals("test-endpoint2")));
  }

  @Test
  public void testNoDiscoverableEndpointConvertsToEmpty() {
    // given
    ComponentImpl component = new ComponentImpl("kubernetes", "123");
    component.setEndpoints(singletonList(
        new EndpointImpl("test-endpoint", 1234, emptyMap())));

    // when
    List<Service> services = componentToKubernetesConverter
        .publicEndpointsToServices(component, "hello");

    // then
    assertTrue(services.isEmpty());
  }
}
