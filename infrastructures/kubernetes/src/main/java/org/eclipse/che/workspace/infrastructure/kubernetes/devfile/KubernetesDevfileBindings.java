/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * A utility class to ease the binding of Kubernetes-related devfile bindings in a Guice module.
 *
 * <p>Consult the individual methods to see if you need to use them.
 */
public class KubernetesDevfileBindings {

  public static final String ALLOWED_ENVIRONMENT_TYPE_UPGRADES_KEY_NAME =
      "allowedEnvironmentTypeUpgrades";
  public static final String KUBERNETES_BASED_ENVIRONMENTS_KEY_NAME = "kubernetesBasedEnvironments";
  public static final String KUBERNETES_BASED_COMPONENTS_KEY_NAME = "kubernetesBasedComponents";

  /**
   * Any workspace environments based on Kubernetes recipes need to register the binding using this
   * method so that the {@link KubernetesComponentProvisioner} and {@link
   * KubernetesEnvironmentProvisioner} can work properly with these environments.
   *
   * @param baseBinder the binder available in the Guice module calling this method.
   * @param environmentTypes the environment types to be registered as handled by Kubernetes recipes
   */
  public static void addKubernetesBasedEnvironmentTypeBindings(
      Binder baseBinder, String... environmentTypes) {
    Multibinder<String> binder =
        Multibinder.newSetBinder(
            baseBinder, String.class, Names.named(KUBERNETES_BASED_ENVIRONMENTS_KEY_NAME));
    for (String envType : environmentTypes) {
      binder.addBinding().toInstance(envType);
    }
  }

  /**
   * Any devfile components based on Kubernetes recipes need to register the binding using this
   * method so that the {@link KubernetesComponentProvisioner} and {@link
   * KubernetesComponentToWorkspaceApplier} can work properly with these components.
   *
   * @param baseBinder the binder available in the Guice module calling this method.
   * @param componentTypes the component types to be registered as handled by Kubernetes recipes
   */
  public static void addKubernetesBasedComponentTypeBindings(
      Binder baseBinder, String... componentTypes) {
    Multibinder<String> binder =
        Multibinder.newSetBinder(
            baseBinder, String.class, Names.named(KUBERNETES_BASED_COMPONENTS_KEY_NAME));
    for (String envType : componentTypes) {
      binder.addBinding().toInstance(envType);
    }
  }

  /**
   * It is possible "upgrade" a kubernetes-based environment to a more specific type (e.g. a
   * Kubernetes can be upgraded to Openshift environment, because Openshift is compatible with
   * Kubernetes, but an Openshift environment cannot be "upgraded" Kubernetes environment, because
   * Kubernetes is not itself compatible with Openshift).
   *
   * @param baseBinder the binder available in the Guice module calling this method
   * @param targetEnvironmentType the environment type to upgrade to, if possible
   * @param baseEnvironmentTypes the environments from which it is possible to upgrade to the target
   *     environment type.
   */
  public static void addAllowedEnvironmentTypeUpgradeBindings(
      Binder baseBinder, String targetEnvironmentType, String... baseEnvironmentTypes) {
    MapBinder<String, String> binder =
        MapBinder.newMapBinder(
                baseBinder,
                String.class,
                String.class,
                Names.named(ALLOWED_ENVIRONMENT_TYPE_UPGRADES_KEY_NAME))
            .permitDuplicates();

    for (String baseType : baseEnvironmentTypes) {
      binder.addBinding(targetEnvironmentType).toInstance(baseType);
    }
  }

  private KubernetesDevfileBindings() {}
}
