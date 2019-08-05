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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.external;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

/**
 * Provides a path-based strategy for exposing service ports outside the cluster using Ingress.
 * Ingresses will be created with a common host name for all workspaces.
 *
 * <p>This strategy uses different Ingress path entries <br>
 * Each external server is exposed with a unique path prefix.
 *
 * <p>This strategy imposes limitation on user-developed applications. <br>
 *
 * <pre>
 *   Path-Based Ingress exposing service's port:
 * Ingress
 * ...
 * spec:
 *   rules:
 *     - host: CHE_HOST
 *       http:
 *         paths:
 *           - path: service123/webapp        ---->> Service.metadata.name + / + Service.spec.ports[0].name
 *             backend:
 *               serviceName: service123      ---->> Service.metadata.name
 *               servicePort: [8080|web-app]  ---->> Service.spec.ports[0].[port|name]
 * </pre>
 *
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
public class SingleHostIngressExternalServerExposer
    implements ExternalServerExposerStrategy<KubernetesEnvironment> {

  public static final String SINGLE_HOST_STRATEGY = "single-host";
  private final Map<String, String> ingressAnnotations;
  private final String cheHost;
  private final String pathTransformFmt;
  private final Pattern pathTransformInverse;

  @Inject
  public SingleHostIngressExternalServerExposer(
      @Named("infra.kubernetes.ingress.annotations") Map<String, String> ingressAnnotations,
      @Named("che.host") String cheHost,
      @Nullable @Named("che.infra.kubernetes.ingress.path_transform") String pathTransformFmt) {
    this.ingressAnnotations = ingressAnnotations;
    this.cheHost = cheHost;
    this.pathTransformFmt = pathTransformFmt == null ? "%s" : pathTransformFmt;
    this.pathTransformInverse = extractPathFromFmt(this.pathTransformFmt);
  }

  private static Pattern extractPathFromFmt(String pathTransformFmt) {
    int refIdx = pathTransformFmt.indexOf("%s");
    String matchPath = "(.*)";

    String transformed;
    if (refIdx < 0) {
      transformed = Pattern.quote(pathTransformFmt);
    } else {
      if (refIdx == 0) {
        if (pathTransformFmt.length() > 2) {
          transformed = matchPath + Pattern.quote(pathTransformFmt.substring(2));
        } else {
          transformed = matchPath;
        }
      } else {
        String prefix = Pattern.quote(pathTransformFmt.substring(0, refIdx));
        String suffix =
            refIdx < pathTransformFmt.length() - 2
                ? Pattern.quote(pathTransformFmt.substring(refIdx + 2))
                : "";

        transformed = prefix + matchPath + suffix;
      }
    }

    return Pattern.compile("^" + transformed + "$");
  }

  @Override
  public void expose(
      KubernetesEnvironment k8sEnv,
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> externalServers) {
    Ingress ingress = generateIngress(machineName, serviceName, servicePort, externalServers);
    k8sEnv.getIngresses().put(ingress.getMetadata().getName(), ingress);
  }

  @Override
  public String demanglePath(HasMetadata exposingObject, String path) {
    Matcher matcher = pathTransformInverse.matcher(path);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return path;
    }
  }

  private Ingress generateIngress(
      String machineName,
      String serviceName,
      ServicePort servicePort,
      Map<String, ServerConfig> ingressesServers) {
    return new ExternalServerIngressBuilder()
        .withHost(cheHost)
        .withPath(generateExternalServerIngressPath(serviceName, servicePort))
        .withName(generateExternalServerIngressName(serviceName, servicePort))
        .withMachineName(machineName)
        .withServiceName(serviceName)
        .withAnnotations(ingressAnnotations)
        .withServicePort(servicePort.getName())
        .withServers(ingressesServers)
        .build();
  }

  private String generateExternalServerIngressName(String serviceName, ServicePort servicePort) {
    return serviceName + '-' + servicePort.getName();
  }

  private String generateExternalServerIngressPath(String serviceName, ServicePort servicePort) {
    return String.format(pathTransformFmt, "/" + serviceName + "/" + servicePort.getName());
  }
}
