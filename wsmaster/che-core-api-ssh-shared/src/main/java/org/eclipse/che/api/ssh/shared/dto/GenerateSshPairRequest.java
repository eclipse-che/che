/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.ssh.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Interface describe a request for generating a SSH key pair.
 *
 * @author Sergii Leschenko
 */
@DTO
public interface GenerateSshPairRequest {
  /** Returns name service that will use generated ssh pair. */
  String getService();

  void setService(String service);

  GenerateSshPairRequest withService(String service);

  /** Returns name for generated ssh pair */
  String getName();

  void setName(String name);

  GenerateSshPairRequest withName(String name);
}
