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
public interface ProcessUnSubscribeRequestDto extends DtoWithPid {
  ProcessUnSubscribeRequestDto withPid(int pid);

  String getEventTypes();

  ProcessUnSubscribeRequestDto withEventTypes(String eventTypes);

  String getAfter();

  ProcessUnSubscribeRequestDto withAfter(String after);
}
