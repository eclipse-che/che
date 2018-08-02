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
package org.eclipse.che.dto.server;

import com.google.gson.JsonElement;

/**
 * Provides implementation of DTO interface.
 *
 * @author andrew00x
 */
public interface DtoProvider<DTO> {
  Class<? extends DTO> getImplClass();

  DTO fromJson(String json);

  DTO fromJson(JsonElement json);

  DTO newInstance();

  DTO clone(DTO origin);
}
