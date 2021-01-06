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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

/**
 * Set of annotations used in auto-mount secrets to specify theirs type and/or expected behaviour.
 */
public class KubernetesSecretAnnotationNames {

  public static final String ANNOTATION_PREFIX = "che.eclipse.org";
  public static final String ANNOTATION_MOUNT_AS = ANNOTATION_PREFIX + "/" + "mount-as";

  public static final String ANNOTATION_AUTOMOUNT =
      ANNOTATION_PREFIX + "/" + "automount-workspace-secret";

  public static final String ANNOTATION_GIT_CREDENTIALS =
      ANNOTATION_PREFIX + "/" + "git-credential";

  public static final String ANNOTATION_MOUNT_PATH = ANNOTATION_PREFIX + "/" + "mount-path";

  public static final String ANNOTATION_ENV_NAME = ANNOTATION_PREFIX + "/" + "env-name";
  public static final String ANNOTATION_ENV_NAME_TEMPLATE = ANNOTATION_PREFIX + "/%s_" + "env-name";

  private KubernetesSecretAnnotationNames() {}
}
