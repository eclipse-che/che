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
