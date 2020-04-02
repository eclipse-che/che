package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.INTERNAL_SERVER_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.ComponentToWorkspaceApplierUtils.createService;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.ComponentToWorkspaceApplierUtils.toServerConfig;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier.CHE_COMPONENT_NAME_LABEL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.testng.annotations.Test;

public class ComponentToWorkspaceApplierUtilsTest {

  @Test
  public void testToServerConfigMinimalEndpointShouldTranslateToHttpProtocol() {
    ServerConfig serverConfig = toServerConfig(new EndpointImpl("name", 123, emptyMap()));

    assertEquals(serverConfig.getProtocol(), "http");
  }

  @Test
  public void testToServerConfigMinimalEndpointShouldTranslateToNullPath() {
    ServerConfig serverConfig = toServerConfig(new EndpointImpl("name", 123, emptyMap()));

    assertNull(serverConfig.getPath());
  }

  @Test
  public void testToServerConfigMinimalEndpointShouldHaveEmptyAttributes() {
    ServerConfig serverConfig = toServerConfig(new EndpointImpl("name", 123, emptyMap()));

    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testToServerConfigCustomAttributesShouldPreserveInAttributes() {
    Map<String, String> customAttributes = ImmutableMap.of("k1", "v1", "k2", "v2");
    ServerConfig serverConfig = toServerConfig(new EndpointImpl("name", 123, customAttributes));

    assertEquals(serverConfig.getAttributes().get("k1"), "v1");
    assertEquals(serverConfig.getAttributes().get("k2"), "v2");
    assertEquals(serverConfig.getAttributes().size(), 2);
  }

  @Test
  public void testToServerConfigTranslatePath() {
    ServerConfig serverConfig =
        toServerConfig(new EndpointImpl("name", 123, singletonMap("path", "hello")));

    assertTrue(serverConfig.getAttributes().isEmpty());
    assertEquals(serverConfig.getPath(), "hello");
  }

  @Test
  public void testToServerConfigTranslateProtocol() {
    ServerConfig serverConfig =
        toServerConfig(new EndpointImpl("name", 123, singletonMap("protocol", "hello")));

    assertTrue(serverConfig.getAttributes().isEmpty());
    assertEquals(serverConfig.getProtocol(), "hello");
  }

  @Test
  public void testToServerConfigTranslatePublicTrue() {
    ServerConfig serverConfig =
        toServerConfig(new EndpointImpl("name", 123, singletonMap("public", "true")));

    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testToServerConfigTranslatePublicWhatever() {
    ServerConfig serverConfig =
        toServerConfig(new EndpointImpl("name", 123, singletonMap("public", "whatever")));

    assertTrue(serverConfig.getAttributes().isEmpty());
  }

  @Test
  public void testToServerConfigTranslatePublicFalse() {
    ServerConfig serverConfig =
        toServerConfig(new EndpointImpl("name", 123, singletonMap("public", "false")));

    assertFalse(serverConfig.getAttributes().isEmpty());
    assertEquals(
        serverConfig.getAttributes().get(INTERNAL_SERVER_ATTRIBUTE), Boolean.TRUE.toString());
  }

  @Test
  public void testCreateService() {
    Service service = createService("hello", new EndpointImpl("test-endpoint", 1234, emptyMap()));

    assertEquals(service.getMetadata().getName(), "test-endpoint");
    assertEquals(service.getSpec().getSelector().get(CHE_COMPONENT_NAME_LABEL), "hello");
    List<ServicePort> ports = service.getSpec().getPorts();
    assertEquals(ports.size(), 1);
    assertEquals(ports.get(0).getPort().intValue(), 1234);
    assertEquals(ports.get(0).getTargetPort().getIntVal().intValue(), 1234);
    assertEquals(ports.get(0).getProtocol(), "TCP");
  }
}
