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
package org.eclipse.che.plugin.typescript.dto;

import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/** @author Florent Benoit */
@DTO
public interface MyCustomDTO {

  String getName();

  void setName(String name);

  MyCustomDTO withName(String name);

  MyOtherDTO getConfig();

  MyCustomDTO withConfig(MyOtherDTO otherDTO);

  void setConfig(MyOtherDTO otherDTO);

  void setStatus(Status status);

  MyCustomDTO withStatus(Status status);

  Status getStatus();

  Map<String, MyOtherDTO> getCustomMap();

  void setCustomMap(Map<String, MyOtherDTO> map);

  MyCustomDTO withCustomMap(Map<String, MyOtherDTO> map);

  // arguments is a reserved keyword for TypeScript
  List<MyOtherDTO> getArguments();

  void setArguments(List<MyOtherDTO> arguments);

  MyCustomDTO withArguments(List<MyOtherDTO> arguments);
}
