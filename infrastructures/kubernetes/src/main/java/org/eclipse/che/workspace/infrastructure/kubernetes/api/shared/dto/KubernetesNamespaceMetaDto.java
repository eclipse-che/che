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
package org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto;

import java.util.Map;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;

/** @author Sergii Leshchenko */
@DTO
public interface KubernetesNamespaceMetaDto extends KubernetesNamespaceMeta {
  @Override
  String getName();

  void setName(String name);

  KubernetesNamespaceMetaDto withName(String name);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  KubernetesNamespaceMetaDto withAttributes(Map<String, String> attributes);
}
