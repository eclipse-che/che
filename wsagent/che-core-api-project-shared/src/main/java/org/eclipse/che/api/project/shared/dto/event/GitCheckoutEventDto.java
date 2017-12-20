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
