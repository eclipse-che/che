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
package org.eclipse.che.api.workspace.shared.dto.devfile;

import org.eclipse.che.api.core.model.workspace.devfile.PreviewUrl;
import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PreviewUrlDto extends PreviewUrl {

  @Override
  int getPort();

  @Override
  String getPath();

  void setPort(int port);

  PreviewUrlDto withPort(int port);

  void setPath(String path);

  PreviewUrlDto withPath(String path);
}
