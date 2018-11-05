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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.UNSECURED_PATHS_ATTRIBUTE;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManagerException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.ServerServiceBuilder;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyConfigBuilderFactory;

/**
 * Modifies Kubernetes environment to expose the specified service port via JWTProxy.
 *
 * <p>Exposing includes the following operation:
 *
 * <ul>
 *   <li>Putting Machine configuration into Kubernetes environment if absent;
 *   <li>Putting JwtProxy pod with one container if absent;
 *   <li>Putting JwtProxy service that will expose added JWTProxy pod if absent;
 *   <li>Putting JwtProxy ConfigMap that contains public key and JwtProxy config in yaml format if
 *       absent;
 *   <li>Updating JwtProxy Service to expose port for secure server;
 *   <li>Updating JwtProxy configuration in config map by adding the corresponding verifier proxy
 *       there;
 * </ul>
 *
 * @see JwtProxyConfigBuilder
 * @see SignatureKeyManager
 * @author Sergii Leshchenko
 */
public class JwtProxyProvisioner {

  static final int FIRST_AVAILABLE_PORT = 4400;

  static final int MEGABYTES_TO_BYTES_DIVIDER = 1024 * 1024;

  static final String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----\n";
  static final String PUBLIC_KEY_FOOTER = "\n-----END PUBLIC KEY-----";

  static final String JWT_PROXY_CONFIG_FILE = "config.yaml";
  static final String JWT_PROXY_MACHINE_NAME = "che-jwtproxy";
  static final String JWT_PROXY_POD_NAME = JWT_PROXY_MACHINE_NAME;

  static final String JWT_PROXY_CONFIG_FOLDER = "/config";
  static final String JWT_PROXY_PUBLIC_KEY_FILE = "mykey.pub";

  private final SignatureKeyManager signatureKeyManager;

  private final RuntimeIdentity identity;

  private final JwtProxyConfigBuilder proxyConfigBuilder;

  private final String jwtProxyImage;
  private final Map<String, String> attributes;

  private final String serviceName;
  private int availablePort;

  @Inject
  public JwtProxyProvisioner(
      SignatureKeyManager signatureKeyManager,
      JwtProxyConfigBuilderFactory jwtProxyConfigBuilderFactory,
      @Named("che.server.secure_exposer.jwtproxy.image") String jwtProxyImage,
      @Named("che.server.secure_exposer.jwtproxy.memory_limit") String memoryLimitBytes,
      @Assisted RuntimeIdentity identity) {
    this.signatureKeyManager = signatureKeyManager;

    this.jwtProxyImage = jwtProxyImage;

    this.proxyConfigBuilder = jwtProxyConfigBuilderFactory.create(identity.getWorkspaceId());
    this.identity = identity;
    this.serviceName = generate(SERVER_PREFIX, SERVER_UNIQUE_PART_SIZE) + "-jwtproxy";
    this.availablePort = FIRST_AVAILABLE_PORT;
    long memoryLimitLong = Size.parseSizeToMegabytes(memoryLimitBytes) * MEGABYTES_TO_BYTES_DIVIDER;
    this.attributes =
        ImmutableMap.of(
            MEMORY_LIMIT_ATTRIBUTE,
            Long.toString(memoryLimitLong),
            MEMORY_REQUEST_ATTRIBUTE,
            Long.toString(memoryLimitLong));
  }

  /**
   * Modifies Kubernetes environment to expose the specified service port via JWTProxy.
   *
   * @param k8sEnv Kubernetes environment to modify
   * @param backendServiceName service name that will be exposed
   * @param backendServicePort service port that will be exposed
   * @param protocol protocol that will be used for exposed port
   * @param secureServers secure servers to expose
   * @return JWTProxy service port that expose the specified one
   * @throws InfrastructureException if any exception occurs during port exposing
   */
  public ServicePort expose(
      KubernetesEnvironment k8sEnv,
      String backendServiceName,
      int backendServicePort,
      String protocol,
      Map<String, ServerConfig> secureServers)
      throws InfrastructureException {
    Preconditions.checkArgument(
        secureServers != null && !secureServers.isEmpty(), "Secure servers are missing");
    ensureJwtProxyInjected(k8sEnv);

    int listenPort = availablePort++;

    Set<String> excludes = new HashSet<>();
    Boolean cookiesAuthEnabled = null;
    for (ServerConfig config : secureServers.values()) {
      // accumulate unsecured paths
      if (config.getAttributes().containsKey(UNSECURED_PATHS_ATTRIBUTE)) {
        Collections.addAll(
            excludes, config.getAttributes().get(UNSECURED_PATHS_ATTRIBUTE).split(","));
      }

      // calculate `cookiesAuthEnabled` attributes
      Boolean serverCookiesAuthEnabled =
          parseBoolean(config.getAttributes().get(SECURE_SERVER_COOKIES_AUTH_ENABLED_ATTRIBUTE));
      if (cookiesAuthEnabled == null) {
        cookiesAuthEnabled = serverCookiesAuthEnabled;
      } else {
        if (!cookiesAuthEnabled.equals(serverCookiesAuthEnabled)) {
          throw new InfrastructureException(
              "Secure servers which expose the same port should have the same `cookiesAuthEnabled` value.");
        }
      }
    }

    proxyConfigBuilder.addVerifierProxy(
        listenPort,
        "http://" + backendServiceName + ":" + backendServicePort,
        excludes,
        cookiesAuthEnabled);
    k8sEnv
        .getConfigMaps()
        .get(getConfigMapName())
        .getData()
        .put(JWT_PROXY_CONFIG_FILE, proxyConfigBuilder.build());

    ServicePort exposedPort =
        new ServicePortBuilder()
            .withName("server-" + listenPort)
            .withPort(listenPort)
            .withProtocol(protocol)
            .withNewTargetPort(listenPort)
            .build();

    k8sEnv.getServices().get(getServiceName()).getSpec().getPorts().add(exposedPort);

    return exposedPort;
  }

  /** Returns service name that exposed JWTProxy Pod. */
  public String getServiceName() {
    return serviceName;
  }

  /** Returns config map name that will be mounted into JWTProxy Pod. */
  @VisibleForTesting
  String getConfigMapName() {
    return "jwtproxy-config-" + identity.getWorkspaceId();
  }

  private void ensureJwtProxyInjected(KubernetesEnvironment k8sEnv) throws InfrastructureException {
    if (!k8sEnv.getMachines().containsKey(JWT_PROXY_MACHINE_NAME)) {
      k8sEnv.getMachines().put(JWT_PROXY_MACHINE_NAME, createJwtProxyMachine());
      k8sEnv.getPods().put(JWT_PROXY_POD_NAME, createJwtProxyPod());

      KeyPair keyPair;
      try {
        keyPair = signatureKeyManager.getOrCreateKeyPair(identity.getWorkspaceId());
      } catch (SignatureKeyManagerException e) {
        throw new InternalInfrastructureException(
            "Signature key pair for machine authentication cannot be retrieved. Reason: "
                + e.getMessage());
      }
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
              .withSelectorEntry(CHE_ORIGINAL_NAME_LABEL, JWT_PROXY_MACHINE_NAME)
              .withMachineName(JWT_PROXY_MACHINE_NAME)
              .withPorts(emptyList())
              .build();
      k8sEnv.getServices().put(jwtProxyService.getMetadata().getName(), jwtProxyService);
    }
  }

  private InternalMachineConfig createJwtProxyMachine() {
    return new InternalMachineConfig(null, emptyMap(), emptyMap(), attributes, null);
  }

  private Pod createJwtProxyPod() {
    return new PodBuilder()
        .withNewMetadata()
        .withName(JWT_PROXY_POD_NAME)
        .withAnnotations(
            ImmutableMap.of(
                "org.eclipse.che.container.verifier.machine_name", JWT_PROXY_MACHINE_NAME))
        .endMetadata()
        .withNewSpec()
        .withContainers(
            new ContainerBuilder()
                .withImagePullPolicy("Always")
                .withName("verifier")
                .withImage(jwtProxyImage)
                .withVolumeMounts(
                    new VolumeMount(
                        JWT_PROXY_CONFIG_FOLDER + "/", null, "jwtproxy-config-volume", false, null))
                .withArgs("-config", JWT_PROXY_CONFIG_FOLDER + "/" + JWT_PROXY_CONFIG_FILE)
                .build())
        .withVolumes(
            new VolumeBuilder()
                .withName("jwtproxy-config-volume")
                .withNewConfigMap()
                .withName(getConfigMapName())
                .endConfigMap()
                .build())
        .endSpec()
        .build();
  }
}
