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
package org.eclipse.che.dto.definitions;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

/**
 * Makes use of the JsonFieldName annotation
 *
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface DtoWithFieldNames {

  public String THENAME_FIELD = "the name";
  public String THEDEFAULT_FIELD = "default";

  @JsonFieldName(THENAME_FIELD)
  String getTheName();

  void setTheName(String v);

  DtoWithFieldNames withTheName(String v);

  @JsonFieldName(THEDEFAULT_FIELD)
  String getTheDefault();

  void setTheDefault(String v);

  DtoWithFieldNames withTheDefault(String v);
}
