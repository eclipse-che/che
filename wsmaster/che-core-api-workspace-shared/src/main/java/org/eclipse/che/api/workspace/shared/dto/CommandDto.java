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
package org.eclipse.che.api.workspace.shared.dto;

import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.MANDATORY;
import static org.eclipse.che.api.core.factory.FactoryParameter.Obligation.OPTIONAL;

import java.util.Map;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.workspace.shared.dto.devfile.PreviewUrlDto;
import org.eclipse.che.dto.shared.DTO;

/** @author Alexander Garagatyi */
@DTO
public interface CommandDto extends Command {

  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getName();

  void setName(String name);

  CommandDto withName(String name);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getCommandLine();

  void setCommandLine(String commandLine);

  CommandDto withCommandLine(String commandLine);

  @Override
  @FactoryParameter(obligation = MANDATORY)
  String getType();

  void setType(String type);

  CommandDto withType(String type);

  @Override
  @FactoryParameter(obligation = OPTIONAL)
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  CommandDto withAttributes(Map<String, String> attributes);

  @Override
  PreviewUrlDto getPreviewUrl();

  void setPreviewUrl(PreviewUrlDto previewUrl);

  CommandDto withPreviewUrl(PreviewUrlDto previewUrl);
}
