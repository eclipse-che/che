package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static org.testng.Assert.*;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.testng.annotations.Test;

public class CheInstallationLocationTest {
  @Test
  public void returnKubernetesNamespaceWhenBothSet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation("kube", "pod");
    assertEquals(cheInstallationLocation.getInstallationLocationNamespace(), "kube");
  }

  @Test
  public void returnKubernetesNamespaceWhenItsOnlySet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation("kube", null);
    assertEquals(cheInstallationLocation.getInstallationLocationNamespace(), "kube");
  }

  @Test
  public void returnPodNamespaceWhenKubernetesNamespaceNotSet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation(null, "pod");
    assertEquals(cheInstallationLocation.getInstallationLocationNamespace(), "pod");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwExceptionWhenNoneSet() throws InfrastructureException {
    CheInstallationLocation cheInstallationLocation = new CheInstallationLocation(null, null);
    cheInstallationLocation.getInstallationLocationNamespace();
  }
}
