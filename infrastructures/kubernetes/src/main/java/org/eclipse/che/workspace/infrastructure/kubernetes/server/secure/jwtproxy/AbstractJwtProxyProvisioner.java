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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.CPU_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.CONTAINER_SOURCE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.TOOL_CONTAINER_SOURCE;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.ServerServiceBuilder;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.ProxyProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyConfigBuilderFactory;

/**
 * A base class for both {@link JwtProxyProvisioner} and {@link PassThroughProxyProvisioner} that
 * contains the bulk of the provisioning logic.
 */
abstract class AbstractJwtProxyProvisioner implements ProxyProvisioner {

  static final String JWT_PROXY_MACHINE_NAME = "che-jwtproxy";
  public static final String JWT_PROXY_POD_NAME = JWT_PROXY_MACHINE_NAME;
  static final int MEGABYTES_TO_BYTES_DIVIDER = 1024 * 1024;
  static final String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----\n";
  static final String PUBLIC_KEY_FOOTER = "\n-----END PUBLIC KEY-----";
  static final String JWT_PROXY_CONFIG_FILE = "config.yaml";
  static final String JWT_PROXY_CONFIG_FOLDER = "/che-jwtproxy-config";
  static final String JWT_PROXY_PUBLIC_KEY_FILE = "mykey.pub";
  private final JwtProxyConfigBuilder proxyConfigBuilder;
  private final String jwtProxyImage;
  private final Map<String, String> attributes;
  private final String serviceName;
  private final ExternalServiceExposureStrategy externalServiceExposureStrategy;
  private final ExternalServiceExposureStrategy multiHostExternalServiceExposureStrategy;
  private final CookiePathStrategy cookiePathStrategy;
  private final MultiHostCookiePathStrategy multihostCookiePathStrategy;
  private final String imagePullPolicy;
  private int availablePort;
  private final KeyPair keyPair;
  private final boolean detectCookieAuth;

  /**
   * Constructor!
   *
   * @param signatureKeyPair the key pair for JWT proxy SSH comms
   * @param jwtProxyConfigBuilderFactory factory to create a JWT proxy config builder
   * @param externalServiceExposureStrategy the strategy to expose external servers
   * @param cookiePathStrategy the strategy for the cookie path of the JWT auth cookies, if used
   * @param jwtProxyImage the image of JWT proxy to use
   * @param memoryLimitBytes the memory limit of the JWT proxy container
   * @param imagePullPolicy the image pull policy for the JWT proxy container
   * @param workspaceId the workspace ID being started
   * @param detectCookieAuth whether to look for cookie auth requirements in the proxied servers or
   *     whether to ignore such requirements
   */
  AbstractJwtProxyProvisioner(
      KeyPair signatureKeyPair,
      JwtProxyConfigBuilderFactory jwtProxyConfigBuilderFactory,
      ExternalServiceExposureStrategy externalServiceExposureStrategy,
      ExternalServiceExposureStrategy multiHostStrategy,
      CookiePathStrategy cookiePathStrategy,
      MultiHostCookiePathStrategy multihostCookiePathStrategy,
      String jwtProxyImage,
      String memoryRequestBytes,
      String memoryLimitBytes,
      String cpuRequestCores,
      String cpuLimitCores,
      String imagePullPolicy,
      String workspaceId,
      boolean detectCookieAuth) {
    this.keyPair = signatureKeyPair;
    this.proxyConfigBuilder = jwtProxyConfigBuilderFactory.create(workspaceId);
    this.jwtProxyImage = jwtProxyImage;
    this.externalServiceExposureStrategy = externalServiceExposureStrategy;
    this.multiHostExternalServiceExposureStrategy = multiHostStrategy;
    this.cookiePathStrategy = cookiePathStrategy;
    this.multihostCookiePathStrategy = multihostCookiePathStrategy;
    this.imagePullPolicy = imagePullPolicy;
    this.serviceName = generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + "-jwtproxy";

    this.availablePort = FIRST_AVAILABLE_PROXY_PORT;
    long memoryLimitLong = Size.parseSizeToMegabytes(memoryLimitBytes) * MEGABYTES_TO_BYTES_DIVIDER;
    long memoryRequestLong =
        Size.parseSizeToMegabytes(memoryRequestBytes) * MEGABYTES_TO_BYTES_DIVIDER;
    this.attributes =
        ImmutableMap.of(
            MEMORY_LIMIT_ATTRIBUTE,
            Long.toString(memoryLimitLong),
            MEMORY_REQUEST_ATTRIBUTE,
            Long.toString(memoryRequestLong),
            CPU_LIMIT_ATTRIBUTE,
            cpuLimitCores,
            CPU_REQUEST_ATTRIBUTE,
            cpuRequestCores,
            CONTAINER_SOURCE_ATTRIBUTE,
            TOOL_CONTAINER_SOURCE);

    this.detectCookieAuth = detectCookieAuth;
  }

  /**
   * Returns an exposure configuration of the provided server.
   *
   * @param serverConfig the server configuration
   * @return the exposure configuration for the server
   */
  protected abstract ExposureConfiguration getExposureConfiguration(ServerConfig serverConfig);

  /**
   * Modifies Kubernetes environment to expose the specified service port via JWTProxy.
   *
   * @param k8sEnv Kubernetes environment to modify
   * @param pod the pod that runs the server being exposed
   * @param backendServiceName service name that will be exposed
   * @param backendServicePort service port that will be exposed
   * @param protocol protocol that will be used for exposed port
   * @param secureServers secure servers to expose
   * @return JWTProxy service port that expose the specified one
   * @throws InfrastructureException if any exception occurs during port exposing
   */
  @Override
  public ServicePort expose(
      KubernetesEnvironment k8sEnv,
      PodData pod,
      String machineName,
      String backendServiceName,
      ServicePort backendServicePort,
      String protocol,
      boolean requireSubdomain,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException {
    Preconditions.checkArgument(
        secureServers != null && !secureServers.isEmpty(), "Secure servers are missing");
    ensureJwtProxyInjected(k8sEnv, machineName, pod);

    Set<String> excludes = new HashSet<>();
    Boolean cookiesAuthEnabled = null;
    for (ServerConfig serverConfig : secureServers.values()) {
      ExposureConfiguration config = getExposureConfiguration(serverConfig);

      // accumulate unsecured paths
      if (config.excludedPaths != null) {
        excludes.addAll(config.excludedPaths);
      }

      // calculate `cookiesAuthEnabled` attributes
      if (detectCookieAuth) {
        if (cookiesAuthEnabled == null) {
          cookiesAuthEnabled = config.cookiesAuthEnabled;
        } else {
          if (!cookiesAuthEnabled.equals(config.cookiesAuthEnabled)) {
            throw new InfrastructureException(
                "Secure servers which expose the same port should have the same `cookiesAuthEnabled` value.");
          }
        }
      }
    }

    int listenPort = availablePort++;

    ServicePort exposedPort =
        new ServicePortBuilder()
            .withName("server-" + listenPort)
            .withPort(listenPort)
            .withProtocol(protocol)
            .withNewTargetPort(listenPort)
            .build();

    k8sEnv.getServices().get(serviceName).getSpec().getPorts().add(exposedPort);

    CookiePathStrategy actualCookiePathStrategy =
        requireSubdomain ? multihostCookiePathStrategy : cookiePathStrategy;
    ExternalServiceExposureStrategy actualExposureStrategy =
        requireSubdomain
            ? multiHostExternalServiceExposureStrategy
            : externalServiceExposureStrategy;

    // JwtProxySecureServerExposer creates no service for the exposed secure servers and
    // assumes everything will be proxied from localhost, because JWT proxy is collocated
    // with the workspace pod (because it is added to the environment as an injectable pod).
    // This method historically supported proxying secure servers exposed through a service
    // (which is not secure in absence of a appropriate network policy). The support for
    // accessing the backend server through a service was kept here because it doesn't add
    // any additional complexity to this method and keeps the door open for the
    // JwtProxySecureServerExposer to be enhanced in the future with support for service-handled
    // secure servers.
    backendServiceName = backendServiceName == null ? "127.0.0.1" : backendServiceName;
    proxyConfigBuilder.addVerifierProxy(
        listenPort,
        "http://" + backendServiceName + ":" + backendServicePort.getTargetPort().getIntVal(),
        excludes,
        cookiesAuthEnabled == null ? false : cookiesAuthEnabled,
        actualCookiePathStrategy.get(serviceName, exposedPort),
        actualExposureStrategy.getExternalPath(serviceName, exposedPort.getName()));
    k8sEnv
        .getConfigMaps()
        .get(getConfigMapName())
        .getData()
        .put(JWT_PROXY_CONFIG_FILE, proxyConfigBuilder.build());

    return exposedPort;
  }

  /** Returns service name that exposed JWTProxy Pod. */
  public String getServiceName() {
    return serviceName;
  }

  /** Returns config map name that will be mounted into JWTProxy Pod. */
  @VisibleForTesting
  String getConfigMapName() {
    return "jwtproxy-config";
  }

  private void ensureJwtProxyInjected(KubernetesEnvironment k8sEnv, String machineName, PodData pod)
      throws InfrastructureException {
    if (!k8sEnv.getMachines().containsKey(JWT_PROXY_MACHINE_NAME)) {
      k8sEnv.getMachines().put(JWT_PROXY_MACHINE_NAME, createJwtProxyMachine());
      Pod jwtProxyPod = createJwtProxyPod();
      k8sEnv.addInjectablePod(machineName, JWT_PROXY_MACHINE_NAME, jwtProxyPod);

      Map<String, String> initConfigMapData = new HashMap<>();
      initConfigMapData.put(
          JWT_PROXY_PUBLIC_KEY_FILE,
          PUBLIC_KEY_HEADER
              + java.util.Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
              + PUBLIC_KEY_FOOTER);

      initConfigMapData.put(JWT_PROXY_CONFIG_FILE, proxyConfigBuilder.build());

      ConfigMap jwtProxyConfigMap =
          new ConfigMapBuilder()
              .withNewMetadata()
              .withName(getConfigMapName())
              .endMetadata()
              .withData(initConfigMapData)
              .build();
      k8sEnv.getConfigMaps().put(jwtProxyConfigMap.getMetadata().getName(), jwtProxyConfigMap);

      Service jwtProxyService =
          new ServerServiceBuilder()
              .withName(serviceName)
              // we're merely injecting the pod, so we need a selector that is going to hit the
              // pod that runs the server that we're exposing
              .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, pod.getMetadata().getName())
              .withMachineName(JWT_PROXY_MACHINE_NAME)
              .withPorts(emptyList())
              .build();
      k8sEnv.getServices().put(jwtProxyService.getMetadata().getName(), jwtProxyService);
    }
  }

  private InternalMachineConfig createJwtProxyMachine() {
    return new InternalMachineConfig(emptyMap(), emptyMap(), attributes, null);
  }

  private Pod createJwtProxyPod() {
    String containerName = Names.generateName("che-jwtproxy");
    return new PodBuilder()
        .withNewMetadata()
        .withName(JWT_PROXY_POD_NAME)
        .withAnnotations(Names.createMachineNameAnnotations(containerName, JWT_PROXY_MACHINE_NAME))
        .endMetadata()
        .withNewSpec()
        .withContainers(
            new ContainerBuilder()
                .withImagePullPolicy(imagePullPolicy)
                .withName(containerName)
                .withImage(jwtProxyImage)
                .withVolumeMounts(
                    new VolumeMount(
                        JWT_PROXY_CONFIG_FOLDER + "/",
                        null,
                        "che-jwtproxy-config-volume",
                        false,
                        null,
                        null))
                .withArgs("-config", JWT_PROXY_CONFIG_FOLDER + "/" + JWT_PROXY_CONFIG_FILE)
                .addNewEnv()
                .withName("XDG_CONFIG_HOME")
                .withValue(JWT_PROXY_CONFIG_FOLDER)
                .endEnv()
                .build())
        .withVolumes(
            new VolumeBuilder()
                .withName("che-jwtproxy-config-volume")
                .withNewConfigMap()
                .withName(getConfigMapName())
                .endConfigMap()
                .build())
        .endSpec()
        .build();
  }

  protected static final class ExposureConfiguration {
    private final List<String> excludedPaths;
    private final Boolean cookiesAuthEnabled;

    public ExposureConfiguration(ServerConfig serverConfig) {
      this(serverConfig.getUnsecuredPaths(), serverConfig.isCookiesAuthEnabled());
    }

    public ExposureConfiguration(List<String> excludedPaths, Boolean cookiesAuthEnabled) {
      this.excludedPaths = excludedPaths;
      this.cookiesAuthEnabled = cookiesAuthEnabled;
    }
  }
}
