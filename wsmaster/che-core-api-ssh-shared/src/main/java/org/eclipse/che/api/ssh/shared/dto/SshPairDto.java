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
package org.eclipse.che.api.ssh.shared.dto;

import java.util.List;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leschenko */
@DTO
public interface SshPairDto extends SshPair, Hyperlinks {
  @Override
  String getService();

  void setService(String service);

  SshPairDto withService(String service);

  @Override
  String getName();

  void setName(String name);

  SshPairDto withName(String name);

  @Override
  String getPublicKey();

  void setPublicKey(String publicKey);

  SshPairDto withPublicKey(String publicKey);

  @Override
  String getPrivateKey();

  void setPrivateKey(String privateKey);

  SshPairDto withPrivateKey(String privateKey);

  @Override
  SshPairDto withLinks(List<Link> links);
}
