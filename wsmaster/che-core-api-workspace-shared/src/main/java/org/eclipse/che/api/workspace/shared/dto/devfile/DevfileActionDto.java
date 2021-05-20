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

import org.eclipse.che.api.core.model.workspace.devfile.Action;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leshchenko */
@DTO
public interface DevfileActionDto extends Action {
  @Override
  String getType();

  void setType(String type);

  DevfileActionDto withType(String type);

  @Override
  String getComponent();

  void setComponent(String component);

  DevfileActionDto withComponent(String component);

  @Override
  String getCommand();

  void setCommand(String command);

  DevfileActionDto withCommand(String command);

  @Override
  String getWorkdir();

  void setWorkdir(String workdir);

  DevfileActionDto withWorkdir(String workdir);

  @Override
  String getReference();

  void setReference(String reference);

  DevfileActionDto withReference(String reference);

  @Override
  String getReferenceContent();

  void setReferenceContent(String referenceContent);

  DevfileActionDto withReferenceContent(String referenceContent);
}
