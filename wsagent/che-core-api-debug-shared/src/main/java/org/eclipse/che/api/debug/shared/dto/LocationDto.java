/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.debug.shared.dto;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.dto.shared.DTO;

/** @author Anatolii Bazko */
@DTO
public interface LocationDto extends Location {
  void setTarget(String target);

  LocationDto withTarget(String target);

  void setLineNumber(int lineNumber);

  LocationDto withLineNumber(int lineNumber);

  void setExternalResource(boolean externalResource);

  LocationDto withExternalResource(boolean externalResource);

  void setExternalResourceId(int externalResourceId);

  LocationDto withExternalResourceId(int externalResourceId);

  void setResourceProjectPath(String resourceProjectPath);

  LocationDto withResourceProjectPath(String resourceProjectPath);

  @Override
  MethodDto getMethod();

  void setMethod(MethodDto method);

  LocationDto withMethod(MethodDto method);

  @Override
  long getThreadId();

  void setThreadId(long threadId);

  LocationDto withThreadId(long threadId);
}
