/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Helps to work with OpenShift objects.
 *
 * @author Anton Korneta
 */
class OpenShiftObjectUtil {

  /** Adds label to target OpenShift object. */
  static void putLabel(HasMetadata target, String key, String value) {
    ObjectMeta metadata = target.getMetadata();

    if (metadata == null) {
      target.setMetadata(metadata = new ObjectMeta());
    }

    Map<String, String> labels = metadata.getLabels();
    if (labels == null) {
      metadata.setLabels(labels = new HashMap<>());
    }

    labels.put(key, value);
  }

  /** Adds selector into target OpenShift service. */
  static void putSelector(Service target, String key, String value) {
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
}
