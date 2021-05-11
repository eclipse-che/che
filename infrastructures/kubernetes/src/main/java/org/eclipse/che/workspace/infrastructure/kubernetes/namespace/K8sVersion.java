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

import io.fabric8.kubernetes.client.VersionInfo;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets version of k8s cluster from given {@link KubernetesClientFactory#create()} and provides
 * functions over it. It lazy-loads {@link VersionInfo} from k8s and use the stored data after.
 * Future methods implementations must ensure that {@link VersionInfo} is properly lazy-loaded.
 *
 * <p>In case of issue like infrastructure or parsing failures, this implementation assumes that
 * we're on newer version.
 *
 * <p>Parsing of version strings is very naive, we just strip all non-digit characters and parse as
 * integer. This should be enough for standard version strings and where it fails, we assume we're
 * on newer version. These are version formats from various platforms:
 *
 * <pre>
 * minikube v1.11.0 with k8s 1.17.6:
 * {
 *  major=1,
 *  minor=17,
 *  ...
 * }
 *
 * minishift v1.34.2+83ebaab:
 * {
 *   major=1,
 *   minor=11+,
 *   ...
 * }
 *
 * crc 1.12.0+6710aff with OpenShift version 4.4.8
 * {
 *  major=1,
 *  minor=17+,
 *  ...
 * }
 * </pre>
 */
@Singleton
public class K8sVersion {

  private static final Logger LOG = LoggerFactory.getLogger(K8sVersion.class);

  private final KubernetesClientFactory clientFactory;
  private VersionInfo versionInfo;
  private int major;
  private int minor;

  @Inject
  public K8sVersion(KubernetesClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  /**
   * Returns 'true' if k8s version is newer or equal than given {@code major.minor}. 'false' if k8s
   * version is older.
   *
   * <p>In case of any issue like infrastructure or parse failures, assume we're on newer version
   * and return 'true'.
   *
   * @param major major version to compare
   * @param minor minor version to compare
   * @return true if k8s version is newer or equal than given {@code major.minor}
   */
  public boolean newerOrEqualThan(int major, int minor) {
    try {
      initVersionInfo();
    } catch (InfrastructureException ie) {
      LOG.warn("Unable to obtain k8s VersionInfo.", ie);
      return true;
    }

    if (this.major > major) {
      return true;
    } else if (this.major == major) {
      return this.minor >= minor;
    } else {
      return false;
    }
  }

  /**
   * Returns 'true' if k8s version is older than given {@code major.minor}. 'false' if k8s version
   * is newer or equal.
   *
   * <p>In case of any issue like infrastructure or parse failures, assume we're on newer version
   * and return 'false'.
   *
   * @param major major version to compare
   * @param minor minor version to compare
   * @return true if given {@code major.minor} version is older than k8s version
   */
  public boolean olderThan(int major, int minor) {
    return !newerOrEqualThan(major, minor);
  }

  private void initVersionInfo() throws InfrastructureException {
    if (versionInfo == null) {
      synchronized (this) {
        if (versionInfo == null) {
          versionInfo = clientFactory.create().getVersion();
          LOG.debug("Obtained k8s version {} {}", versionInfo.getMajor(), versionInfo.getMinor());
          parseVersions();
        }
      }
    }
  }

  /**
   * Try parse versions into integers. This naive implementation removes all non-digits and try to
   * parse what's left into integer.
   */
  private void parseVersions() {
    try {
      this.major = parseVersionNumber(versionInfo.getMajor());
      this.minor = parseVersionNumber(versionInfo.getMinor());
    } catch (NumberFormatException nfe) {
      LOG.warn(
          "Unable to parse k8s version [major: {}, minor: {}].",
          versionInfo.getMajor(),
          versionInfo.getMinor(),
          nfe);
      this.major = Integer.MAX_VALUE;
      this.minor = Integer.MAX_VALUE;
    }
  }

  private int parseVersionNumber(String versionString) {
    versionString = versionString.replaceAll("\\D+", "");
    return Integer.parseInt(versionString);
  }
}
