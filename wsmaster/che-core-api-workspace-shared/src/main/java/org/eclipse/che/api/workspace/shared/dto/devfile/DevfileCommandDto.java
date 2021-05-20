/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface DevfileCommandDto extends Command {
  @Override
  String getName();

  void setName(String name);

  DevfileCommandDto withName(String name);

  void setPreviewUrl(PreviewUrlDto previewUrl);

  DevfileCommandDto withPreviewUrl(PreviewUrlDto previewUrl);

  PreviewUrlDto getPreviewUrl();

  @Override
  List<DevfileActionDto> getActions();

  void setActions(List<DevfileActionDto> actions);

  DevfileCommandDto withActions(List<DevfileActionDto> actions);

  @Override
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  DevfileCommandDto withAttributes(Map<String, String> attributes);
}
