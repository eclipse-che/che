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
package org.eclipse.che.api.project.shared.dto.event;

import com.google.common.annotations.Beta;
import org.eclipse.che.dto.shared.DTO;

/**
 * To transfer branch name after git checkout operation
 *
 * @author Dmitry Kuleshov
 * @since 4.5
 */
@Beta
@DTO
public interface GitCheckoutEventDto {
  Type getType();

  String getName();

  String getProjectName();

  GitCheckoutEventDto withType(Type type);

  GitCheckoutEventDto withName(String name);

  GitCheckoutEventDto withProjectName(String projectName);

  enum Type {
    BRANCH,
    REVISION,
  }
}
