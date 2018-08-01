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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * Makes use of the 'any' (JsonElement) property type feature.
 *
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@DTO
public interface DtoWithAny {
  int getId();

  Object getStuff();

  void setStuff(Object stuff);

  DtoWithAny withStuff(Object stuff);

  List<Object> getObjects();

  void setObjects(List<Object> objects);

  DtoWithAny withObjects(List<Object> objects);
}
