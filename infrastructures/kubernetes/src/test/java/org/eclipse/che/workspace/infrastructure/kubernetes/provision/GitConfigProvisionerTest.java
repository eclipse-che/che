/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
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
public class GitConfigProvisionerTest {

  private KubernetesEnvironment k8sEnv;

  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private Pod pod;

  @Mock private PodSpec podSpec;

  @Mock private Container container;

  @Mock private PreferenceManager preferenceManager;

  @Mock private UserManager userManager;

  @Mock private VcsSslCertificateProvisioner vcsSslCertificateProvisioner;

  private GitConfigProvisioner gitConfigProvisioner;

  @BeforeMethod
  public void setup() {
    k8sEnv = KubernetesEnvironment.builder().build();
    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    k8sEnv.addPod(pod);
    gitConfigProvisioner =
        new GitConfigProvisioner(preferenceManager, userManager, vcsSslCertificateProvisioner);

    Subject subject = new SubjectImpl(null, "id", null, false);
    EnvironmentContext environmentContext = new EnvironmentContext();
    environmentContext.setSubject(subject);
    EnvironmentContext.setCurrent(environmentContext);
  }

  @Test
  public void testShouldDoNothingWhenGitPreferencesAndUserManagerAreEmpty() throws Exception {
    Map<String, String> preferences = singletonMap("theia-user-preferences", "{}");
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);

    User user = mock(User.class);
    when(userManager.getById(eq("id"))).thenReturn(user);
    when(user.getName()).thenReturn(null);
    when(user.getEmail()).thenReturn(null);

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);
  }

  @Test
  public void testShouldExpectWarningWhenPreferenceManagerThrowsServerException() throws Exception {
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences")))
        .thenThrow(new ServerException("message"));

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);

    List<Warning> warnings = k8sEnv.getWarnings();

    assertEquals(warnings.size(), 1);

    Warning actualWarning = warnings.get(0);
    String warnMsg =
        format(Warnings.EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_MESSAGE_FMT, "message");
    Warning expectedWarning =
        new WarningImpl(
            Warnings.EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_WARNING_CODE, warnMsg);

    assertEquals(expectedWarning, actualWarning);
  }

  @Test
  public void testShouldExpectWarningWhenJsonObjectInPreferencesIsNotValid() throws Exception {
    Map<String, String> preferences = singletonMap("theia-user-preferences", "{#$%}");
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

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
  public void testShouldExpectWarningWhenUserManagerThrowsServerException() throws Exception {
    when(userManager.getById(eq("id"))).thenThrow(new ServerException("message"));

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    verifyZeroInteractions(runtimeIdentity);

    List<Warning> warnings = k8sEnv.getWarnings();

    assertEquals(warnings.size(), 1);

    Warning actualWarning = warnings.get(0);
    String warnMsg =
        format(Warnings.EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_MESSAGE_FMT, "message");
    Warning expectedWarning =
        new WarningImpl(
            Warnings.EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_WARNING_CODE, warnMsg);

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

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("gitconfig");

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

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("gitconfig");

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

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("gitconfig");

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

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig = "[user]\n\tname = user\n\temail = email\n";

    assertEquals(gitconfig, expectedGitconfig);
  }

  @Test
  public void testShouldProvisionNameAndEmailFromUserManagerWhenUserPreferencesEmpty()
      throws Exception {
    String json = "{}";
    Map<String, String> preferences = singletonMap("theia-user-preferences", json);
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);
    when(runtimeIdentity.getWorkspaceId()).thenReturn("wksp");

    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(singletonList(container));

    User userMock = mock(User.class);
    when(userMock.getName()).thenReturn("userMockName");
    when(userMock.getEmail()).thenReturn("userMockEmail");
    when(userManager.getById(eq("id"))).thenReturn(userMock);

    List<VolumeMount> volumeMounts = new ArrayList<>();

    when(container.getVolumeMounts()).thenReturn(volumeMounts);
    k8sEnv.addPod(pod);

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig = "[user]\n\tname = userMockName\n\temail = userMockEmail\n";

    assertEquals(gitconfig, expectedGitconfig);
  }

  @Test
  public void testShouldProvisionConfigForHttpsServer() throws Exception {
    when(vcsSslCertificateProvisioner.isConfigured()).thenReturn(true);
    when(vcsSslCertificateProvisioner.getGitServerHost()).thenReturn("https://localhost");
    when(vcsSslCertificateProvisioner.getCertPath()).thenReturn("/some/path");

    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(singletonList(container));

    User userMock = mock(User.class);
    when(userMock.getName()).thenReturn("userMockName");
    when(userMock.getEmail()).thenReturn("userMockEmail");
    when(userManager.getById(eq("id"))).thenReturn(userMock);

    List<VolumeMount> volumeMounts = new ArrayList<>();

    when(container.getVolumeMounts()).thenReturn(volumeMounts);
    k8sEnv.addPod(pod);

    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    assertEquals(volumeMounts.size(), 1);

    VolumeMount mount = volumeMounts.get(0);

    assertEquals(mount.getMountPath(), "/etc/gitconfig");
    assertEquals(mount.getName(), "gitconfigvolume");
    assertFalse(mount.getReadOnly());
    assertEquals(mount.getSubPath(), "gitconfig");

    assertEquals(k8sEnv.getConfigMaps().size(), 1);
    assertTrue(k8sEnv.getConfigMaps().containsKey("gitconfig"));

    ConfigMap configMap = k8sEnv.getConfigMaps().get("gitconfig");

    assertEquals(configMap.getData().size(), 1);
    assertTrue(configMap.getData().containsKey("gitconfig"));

    String gitconfig = configMap.getData().get("gitconfig");
    String expectedGitconfig =
        "[user]\n\tname = userMockName\n\temail = userMockEmail\n[http \"https://localhost\"]\n\tsslCAInfo = /some/path";

    assertEquals(gitconfig, expectedGitconfig);
  }

  @Test
  public void shouldNotProvisionVolumeButShouldMountInInjectablePods() throws Exception {
    // given
    Map<String, String> preferences =
        singletonMap(
            "theia-user-preferences", "{\"git.user.name\":\"user\",\"git.user.email\":\"email\"}");
    when(preferenceManager.find(eq("id"), eq("theia-user-preferences"))).thenReturn(preferences);

    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("wkspc")
            .and()
            .withNewSpec()
            .withContainers(new ContainerBuilder().withImage("image").build())
            .and()
            .build();

    // we want to replace everything in the env that the setup() put there, so let's just re-init.
    k8sEnv = KubernetesEnvironment.builder().build();
    k8sEnv.addPod(pod);

    Pod injectedPod =
        new PodBuilder()
            .withNewMetadata()
            .withName("injected")
            .and()
            .withNewSpec()
            .withContainers(new ContainerBuilder().withImage("image").build())
            .and()
            .build();

    k8sEnv.addInjectablePod("r", "i", injectedPod);

    // when
    gitConfigProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    assertEquals(pod.getSpec().getVolumes().size(), 1);
    assertEquals(injectedPod.getSpec().getVolumes().size(), 0);

    Container podContainer = pod.getSpec().getContainers().get(0);
    Container injectedPodContainer = injectedPod.getSpec().getContainers().get(0);

    assertEquals(podContainer.getVolumeMounts().size(), 1);
    assertEquals(injectedPodContainer.getVolumeMounts().size(), 1);
  }
}
