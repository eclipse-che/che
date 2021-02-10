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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.CREATE_IN_CHE_INSTALLATION_NAMESPACE;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimFluent.SpecNested;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;

/**
 * Helps to work with Kubernetes objects.
 *
 * @author Anton Korneta
 */
public class KubernetesObjectUtil {

  private static final String STORAGE_PARAM = "storage";
  private static final String VALID_CONFIG_MAP_KEY_NAME = "[-._a-zA-Z0-9]+";
  private static final Pattern VALID_CONFIG_MAP_KEY_NAME_PATTERN =
      Pattern.compile(VALID_CONFIG_MAP_KEY_NAME);

  /** Checks if the specified object has the specified label. */
  public static boolean isLabeled(HasMetadata source, String key, String value) {
    ObjectMeta metadata = source.getMetadata();

    if (metadata == null) {
      return false;
    }

    Map<String, String> labels = metadata.getLabels();
    if (labels == null) {
      return false;
    }

    return value.equals(labels.get(key));
  }

  /** Adds label to target Kubernetes object. */
  public static void putLabel(HasMetadata target, String key, String value) {
    ObjectMeta metadata = target.getMetadata();

    if (metadata == null) {
      target.setMetadata(metadata = new ObjectMeta());
    }

    putLabel(target.getMetadata(), key, value);
  }

  /** Adds labels to target Kubernetes object. */
  public static void putLabels(ObjectMeta metadata, Map<String, String> labels) {
    if (labels == null || labels.isEmpty()) {
      return;
    }

    Map<String, String> metaLabels = metadata.getLabels();
    if (metaLabels == null) {
      metadata.setLabels(new HashMap<>(labels));
    } else {
      metaLabels.putAll(labels);
    }
  }

  /** Adds label to target Kubernetes object. */
  public static void putLabel(ObjectMeta metadata, String key, String value) {
    Map<String, String> labels = metadata.getLabels();
    if (labels == null) {
      metadata.setLabels(labels = new HashMap<>());
    }

    labels.put(key, value);
  }

  /** Adds annotation to target Kubernetes object. */
  public static void putAnnotation(HasMetadata target, String key, String value) {
    ObjectMeta metadata = target.getMetadata();

    if (metadata == null) {
      target.setMetadata(metadata = new ObjectMeta());
    }

    putAnnotation(metadata, key, value);
  }

  /** Adds annotation to target ObjectMeta object. */
  public static void putAnnotation(ObjectMeta metadata, String key, String value) {
    Map<String, String> annotations = metadata.getAnnotations();
    if (annotations == null) {
      metadata.setAnnotations(annotations = new HashMap<>());
    }

    annotations.put(key, value);
  }

  /** Adds annotations to target ObjectMeta object. */
  public static void putAnnotations(ObjectMeta metadata, Map<String, String> annotations) {
    if (annotations == null || annotations.isEmpty()) {
      return;
    }

    Map<String, String> metaAnnotations = metadata.getAnnotations();
    if (metaAnnotations == null) {
      metadata.setAnnotations(new HashMap<>(annotations));
    } else {
      metaAnnotations.putAll(annotations);
    }
  }

  /** Adds selector into target Kubernetes service. */
  public static void putSelector(Service target, String key, String value) {
    ServiceSpec spec = target.getSpec();

    if (spec == null) {
      target.setSpec(spec = new ServiceSpec());
    }

    Map<String, String> selector = spec.getSelector();
    if (selector == null) {
      spec.setSelector(selector = new HashMap<>());
    }

    selector.put(key, value);
  }

  /** Sets the specified key/value par of a selector into target Kubernetes service. */
  public static void setSelector(Service target, String key, String value) {
    ServiceSpec spec = target.getSpec();

    if (spec == null) {
      spec = new ServiceSpec();
      target.setSpec(spec);
    }

    HashMap<String, String> selector = new HashMap<>();
    selector.put(key, value);
    spec.setSelector(selector);
  }

  /** Sets the specified match labels for a selector into target Kubernetes Deployment. */
  public static void setSelector(Deployment target, Map<String, String> matchLabels) {
    DeploymentSpec spec = target.getSpec();

    if (spec == null) {
      spec = new DeploymentSpec();
      target.setSpec(spec);
    }

    LabelSelector selector = spec.getSelector();
    if (selector == null) {
      selector = new LabelSelector();
      spec.setSelector(selector);
    }

    selector.setMatchLabels(new HashMap<>(matchLabels));
  }

  /**
   * Returns new instance of {@link PersistentVolumeClaim} with specified name, accessMode and
   * quantity.
   */
  public static PersistentVolumeClaim newPVC(String name, String accessMode, String quantity) {
    return newPVC(name, accessMode, quantity, null);
  }

  /**
   * Returns new instance of {@link PersistentVolumeClaim} with specified name, accessMode, quantity
   * and storageClassName.
   */
  public static PersistentVolumeClaim newPVC(
      String name, String accessMode, String quantity, String storageClassName) {
    SpecNested<PersistentVolumeClaimBuilder> specs =
        new PersistentVolumeClaimBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withNewSpec()
            .withAccessModes(accessMode);
    if (!isNullOrEmpty(storageClassName)) {
      specs.withStorageClassName(storageClassName);
    }
    return specs
        .withNewResources()
        .withRequests(ImmutableMap.of(STORAGE_PARAM, new Quantity(quantity)))
        .endResources()
        .endSpec()
        .build();
  }

  /** Returns new instance of {@link VolumeMount} with specified name, mountPath and subPath. */
  public static VolumeMount newVolumeMount(String name, String mountPath, String subPath) {
    return new VolumeMountBuilder()
        .withMountPath(mountPath)
        .withName(name)
        .withSubPath(subPath)
        .build();
  }

  /** Returns new instance of {@link Volume} with specified name and name of claim related to. */
  public static Volume newVolume(String name, String pvcName) {
    final PersistentVolumeClaimVolumeSource pvcs =
        new PersistentVolumeClaimVolumeSourceBuilder().withClaimName(pvcName).build();
    return new VolumeBuilder().withPersistentVolumeClaim(pvcs).withName(name).build();
  }

  /**
   * Checks the object if it is propetly annotated to be created in Che installation namespace.
   *
   * <p>Create in Che installation namespace only if there is {@link
   * Annotations#CREATE_IN_CHE_INSTALLATION_NAMESPACE} annotation set exactly to `true`. In all
   * other cases we create the object in Workspace's namespace.
   *
   * @param k8sObject object to check
   * @return `true` if {@link Annotations#CREATE_IN_CHE_INSTALLATION_NAMESPACE} is set to `true`.
   *     `false` otherwise.
   */
  public static boolean shouldCreateInCheNamespace(HasMetadata k8sObject) {
    if (k8sObject.getMetadata() == null) {
      return false;
    }
    Map<String, String> annotations = k8sObject.getMetadata().getAnnotations();
    if (annotations == null || annotations.isEmpty()) {
      return false;
    }

    return annotations
        .getOrDefault(CREATE_IN_CHE_INSTALLATION_NAMESPACE, FALSE.toString())
        .equals(TRUE.toString());
  }

  /** Checks the provided name is a valid kubernetes config map key name */
  public static boolean isValidConfigMapKeyName(String configMapKeyName) {
    return VALID_CONFIG_MAP_KEY_NAME_PATTERN.matcher(configMapKeyName).matches();
  }
}
