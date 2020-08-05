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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class K8sVersionTest {

  private K8sVersion k8sVersion;

  @Mock KubernetesClientFactory kubernetesClientFactory;

  @Mock KubernetesClient kubernetesClient;

  @BeforeMethod
  public void setUp() throws InfrastructureException {
    k8sVersion = new K8sVersion(kubernetesClientFactory);
    when(kubernetesClientFactory.create()).thenReturn(kubernetesClient);
  }

  @Test(dataProvider = "greaterThanData")
  public void testGreaterOrEqual(VersionInfo versionInfo, int major, int minor, boolean expected) {
    when(kubernetesClient.getVersion()).thenReturn(versionInfo);
    assertEquals(k8sVersion.newerOrEqualThan(major, minor), expected);
  }

  @Test(dataProvider = "olderThanData")
  public void testOlderThan(VersionInfo versionInfo, int major, int minor, boolean expected) {
    when(kubernetesClient.getVersion()).thenReturn(versionInfo);
    assertEquals(k8sVersion.olderThan(major, minor), expected);
  }

  @Test
  public void testGreaterOrEqualTrueWhenInfrastructureFails() throws InfrastructureException {
    when(kubernetesClientFactory.create()).thenThrow(new InfrastructureException("eh"));
    assertTrue(k8sVersion.newerOrEqualThan(1, 1));
    assertTrue(k8sVersion.newerOrEqualThan(-1, -1));
    assertTrue(k8sVersion.newerOrEqualThan(0, 1));
    assertTrue(k8sVersion.newerOrEqualThan(0, 0));
    assertTrue(k8sVersion.newerOrEqualThan(1337, 1337));
    assertTrue(k8sVersion.newerOrEqualThan(6655321, 6655321));
  }

  @Test
  public void testOlderThanWhenInfrastructureFails() throws InfrastructureException {
    when(kubernetesClientFactory.create()).thenThrow(new InfrastructureException("eh"));
    assertFalse(k8sVersion.olderThan(1, 1));
    assertFalse(k8sVersion.olderThan(-1, -1));
    assertFalse(k8sVersion.olderThan(0, 1));
    assertFalse(k8sVersion.olderThan(0, 0));
    assertFalse(k8sVersion.olderThan(1337, 1337));
    assertFalse(k8sVersion.olderThan(6655321, 6655321));
  }

  @Test
  public void testGreaterOrEqualWhenParseFailure() throws ParseException {
    when(kubernetesClient.getVersion()).thenReturn(createDummyVersionInfo("abc", "cde"));
    assertTrue(k8sVersion.newerOrEqualThan(1, 1));
    assertTrue(k8sVersion.newerOrEqualThan(-1, -1));
    assertTrue(k8sVersion.newerOrEqualThan(0, 1));
    assertTrue(k8sVersion.newerOrEqualThan(0, 0));
    assertTrue(k8sVersion.newerOrEqualThan(1337, 1337));
    assertTrue(k8sVersion.newerOrEqualThan(6655321, 6655321));
  }

  @Test
  public void testOlderThanWhenParseFailure() throws ParseException {
    when(kubernetesClient.getVersion()).thenReturn(createDummyVersionInfo("abc", "cde"));
    assertFalse(k8sVersion.olderThan(1, 1));
    assertFalse(k8sVersion.olderThan(-1, -1));
    assertFalse(k8sVersion.olderThan(0, 1));
    assertFalse(k8sVersion.olderThan(0, 0));
    assertFalse(k8sVersion.olderThan(1337, 1337));
    assertFalse(k8sVersion.olderThan(6655321, 6655321));
  }

  @DataProvider
  public Object[][] greaterThanData() throws ParseException {
    VersionInfo versionInfo = createDummyVersionInfo("2", "10");
    return new Object[][] {
      {versionInfo, 1, 9, true},
      {versionInfo, 1, 10, true},
      {versionInfo, 1, 11, true},
      {versionInfo, 2, 9, true},
      {versionInfo, 2, 10, true},
      {versionInfo, 2, 11, false},
      {versionInfo, 3, 9, false},
      {versionInfo, 3, 10, false},
      {versionInfo, 3, 11, false},
      {createDummyVersionInfo("1", "17+"), 1, 17, true},
      {createDummyVersionInfo("1", "17+"), 1, 18, false},
      {createDummyVersionInfo("1", "17+"), 1, 16, true},
      {createDummyVersionInfo("1", "11+"), 1, 17, false},
    };
  }

  @DataProvider
  public Object[][] olderThanData() throws ParseException {
    VersionInfo versionInfo = createDummyVersionInfo("2", "10");
    return new Object[][] {
      {versionInfo, 1, 9, false},
      {versionInfo, 1, 10, false},
      {versionInfo, 1, 11, false},
      {versionInfo, 2, 9, false},
      {versionInfo, 2, 10, false},
      {versionInfo, 2, 11, true},
      {versionInfo, 3, 9, true},
      {versionInfo, 3, 10, true},
      {versionInfo, 3, 11, true},
      {createDummyVersionInfo("1", "17+"), 1, 17, false},
      {createDummyVersionInfo("1", "17+"), 1, 11, false},
      {createDummyVersionInfo("1", "11+"), 1, 17, true},
    };
  }

  private VersionInfo createDummyVersionInfo(String major, String minor) throws ParseException {
    Map<String, String> versionData = new HashMap<>();
    versionData.put(
        VERSION_KEYS.BUILD_DATE,
        new SimpleDateFormat(VERSION_KEYS.BUILD_DATE_FORMAT).format(new Date()));
    versionData.put(VERSION_KEYS.GIT_COMMIT, "3f6f40d");
    versionData.put(VERSION_KEYS.GIT_VERSION, "v1.17.1+3f6f40d");
    versionData.put(VERSION_KEYS.GIT_TREE_STATE, "clean");
    versionData.put(VERSION_KEYS.GO_VERSION, "go1.13.4");
    versionData.put(VERSION_KEYS.PLATFORM, "linux/amd64");
    versionData.put(VERSION_KEYS.COMPILER, "gc");

    versionData.put(VERSION_KEYS.MAJOR, major);
    versionData.put(VERSION_KEYS.MINOR, minor);
    return new VersionInfo(versionData);
  }

  private final class VERSION_KEYS {

    public static final String BUILD_DATE = "buildDate";
    public static final String GIT_COMMIT = "gitCommit";
    public static final String GIT_VERSION = "gitVersion";
    public static final String MAJOR = "major";
    public static final String MINOR = "minor";
    public static final String GIT_TREE_STATE = "gitTreeState";
    public static final String PLATFORM = "platform";
    public static final String GO_VERSION = "goVersion";
    public static final String COMPILER = "compiler";
    public static final String BUILD_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  }
}
