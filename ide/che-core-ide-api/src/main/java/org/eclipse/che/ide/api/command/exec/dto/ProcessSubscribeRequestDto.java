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
package org.eclipse.che.ide.api.command.exec.dto;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.api.command.exec.dto.event.DtoWithPid;

@DTO
public interface ProcessSubscribeRequestDto extends DtoWithPid {
  ProcessSubscribeRequestDto withPid(int pid);

  String getEventTypes();

  ProcessSubscribeRequestDto withEventTypes(String eventTypes);

  String getAfter();

  ProcessSubscribeRequestDto withAfter(String after);
}
