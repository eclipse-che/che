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
package org.eclipse.che.api.system.shared.dto;

import java.util.List;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.system.shared.SystemStatus;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes current system state.
 *
 * @author Yevhenii Voevodin
 */
@DTO
public interface SystemStateDto extends Hyperlinks {

  /** Returns current system status. */
  SystemStatus getStatus();

  void setStatus(SystemStatus status);

  SystemStateDto withStatus(SystemStatus status);

  SystemStateDto withLinks(List<Link> links);
}
