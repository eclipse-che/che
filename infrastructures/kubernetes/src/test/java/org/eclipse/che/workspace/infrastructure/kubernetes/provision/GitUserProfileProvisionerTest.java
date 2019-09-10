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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GitUserProfileProvisionerTest {

  private KubernetesEnvironment k8sEnv;

  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private Pod pod;

  @Mock private PodSpec podSpec;

  @Mock private Container container;

  @Mock private PreferenceManager preferenceManager;

  private GitUserProfileProvisioner gitUserProfileProvisioner;

  @BeforeMethod
  public void setup() {
    k8sEnv = KubernetesEnvironment.builder().build();
    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    k8sEnv.addPod(pod);
    gitUserProfileProvisioner = new GitUserProfileProvisioner(preferenceManager);

    Subject subject = new SubjectImpl(null, "id", null, false);
    EnvironmentContext environmentContext = new EnvironmentContext();
    environmentContext.setSubject(subject);
    EnvironmentContext.setCurrent(environmentContext);
  }

  @Test
  public void testShouldDoNothingWhenGitUserNameAndEmailIsNotConfigured() throws Exception {
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(emptyMap());
    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);
  }

  @Test
  public void testShouldDoNothingWhenGitPreferencesDoesNotContainsGitPreferences()
      throws Exception {
    Map<String, String> preferences =
        singletonMap(
            "theia-user-preferences",
            "{\"chePlugins.repositories\":{\"Plugins\":\"http://url/\",\"my\":\"https://url/plugins.json\"}}");
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);
  }

  @Test
  public void testShouldDoNothingWhenGitPreferencesAreEmpty() throws Exception {
    Map<String, String> preferences = singletonMap("theia-user-preferences", "{}");
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);
  }

  @Test
  public void testShouldExpectWarningWhenPreferenceManagerThrowsServerException() throws Exception {
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences")))
        .thenThrow(new ServerException("message"));

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);

    List<Warning> warnings = k8sEnv.getWarnings();

    assertEquals(warnings.size(), 1);

    Warning actualWarning = warnings.get(0);
    String warnMsg =
        format(
            Warnings
                .INTERNAL_SERVER_ERROR_OCCURRED_DURING_OPERATING_WITH_USER_PREFERENCES_MESSAGE_FMT,
            "message");
    Warning expectedWarning =
        new WarningImpl(
            Warnings
                .INTERNAL_SERVER_ERROR_OCCURRED_DURING_OPERATING_WITH_USER_PREFERENCES_WARNING_CODE,
            warnMsg);

    assertEquals(expectedWarning, actualWarning);
  }

  @Test
  public void testShouldExpectWarningWhenJsonObjectInPreferencesIsNotValid() throws Exception {
    Map<String, String> preferences = singletonMap("theia-user-preferences", "{#$%}");
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);

    List<Warning> warnings = k8sEnv.getWarnings();

    assertEquals(warnings.size(), 1);

    Warning actualWarning = warnings.get(0);
    String warnMsg =
        format(
            Warnings.JSON_IS_NOT_A_VALID_REPRESENTATION_FOR_AN_OBJECT_OF_TYPE_MESSAGE_FMT,
            "java.io.EOFException: End of input at line 1 column 6 path $.");
    Warning expectedWarning =
        new WarningImpl(
            Warnings.JSON_IS_NOT_A_VALID_REPRESENTATION_FOR_AN_OBJECT_OF_TYPE_WARNING_CODE,
            warnMsg);

    assertEquals(expectedWarning, actualWarning);
  }

  @Test
  public void testShouldCheckIfPodHasMountAndK8HasConfigMapForGitConfig() throws Exception {
    String json = "{\"git.user.name\":\"user\",\"git.user.email\":\"email\"}";
    Map<String, String> preferences = singletonMap("theia-user-preferences", json);
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);
    when(runtimeIdentity.getWorkspaceId()).thenReturn("wksp");

    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(singletonList(container));

    List<VolumeMount> volumeMounts = new ArrayList<>();

    when(container.getVolumeMounts()).thenReturn(volumeMounts);
    k8sEnv.addPod(pod);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("wksp-gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("wksp-gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig = "[user]\n\tname = user\n\temail = email\n";

    assertEquals(gitconfig, expectedGitconfig);
  }

  @DataProvider(name = "invalidEmailValues")
  public Object[][] invalidEmailValues() {
    return new Object[][] {
      {"{\"git.user.name\":\"user\",\"git.user.email\":{}}"},
      {"{\"git.user.name\":\"user\",\"git.user.email\":true}"},
      {"{\"git.user.name\":\"user\",\"git.user.email\":1}"},
      {"{\"git.user.name\":\"user\",\"git.user.email\":[]}"},
      {"{\"git.user.name\":\"user\",\"git.user.email\":null}"}
    };
  }

  @Test(dataProvider = "invalidEmailValues")
  public void testShouldParseOnlyNameWhenEmailIsNotAString(String json) throws Exception {
    Map<String, String> preferences = singletonMap("theia-user-preferences", json);
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);
    when(runtimeIdentity.getWorkspaceId()).thenReturn("wksp");

    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(singletonList(container));

    List<VolumeMount> volumeMounts = new ArrayList<>();

    when(container.getVolumeMounts()).thenReturn(volumeMounts);
    k8sEnv.addPod(pod);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("wksp-gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("wksp-gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig = "[user]\n\tname = user\n";

    assertEquals(gitconfig, expectedGitconfig);
  }

  @DataProvider(name = "invalidNameValues")
  public Object[][] invalidNameValues() {
    return new Object[][] {
      {"{\"git.user.name\":{},\"git.user.email\":\"email\"}"},
      {"{\"git.user.name\":true,\"git.user.email\":\"email\"}"},
      {"{\"git.user.name\":1,\"git.user.email\":\"email\"}"},
      {"{\"git.user.name\":[],\"git.user.email\":\"email\"}"},
      {"{\"git.user.name\":null,\"git.user.email\":\"email\"}"}
    };
  }

  @Test(dataProvider = "invalidNameValues")
  public void testShouldParseOnlyEmailWhenNameIsNotAString(String json) throws Exception {
    Map<String, String> preferences = singletonMap("theia-user-preferences", json);
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);
    when(runtimeIdentity.getWorkspaceId()).thenReturn("wksp");

    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(singletonList(container));

    List<VolumeMount> volumeMounts = new ArrayList<>();

    when(container.getVolumeMounts()).thenReturn(volumeMounts);
    k8sEnv.addPod(pod);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("wksp-gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("wksp-gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig = "[user]\n\temail = email\n";

    assertEquals(gitconfig, expectedGitconfig);
  }

  @Test
  public void
      testShouldCheckIfPodHasMountAndK8HasConfigMapForGitConfigForDifferentTypeOfPreference()
          throws Exception {
    String json =
        "{\"chePlugins.repositories\":{\"Plugins\":\"http://url/\",\"my\":\"https://url/plugins.json\"}, \"git.user.name\":\"user\",\"git.user.email\":\"email\"}";
    Map<String, String> preferences = singletonMap("theia-user-preferences", json);
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);
    when(runtimeIdentity.getWorkspaceId()).thenReturn("wksp");

    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(singletonList(container));

    List<VolumeMount> volumeMounts = new ArrayList<>();

    when(container.getVolumeMounts()).thenReturn(volumeMounts);
    k8sEnv.addPod(pod);

    gitUserProfileProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("wksp-gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("wksp-gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig = "[user]\n\tname = user\n\temail = email\n";

    assertEquals(gitconfig, expectedGitconfig);
  }
}
