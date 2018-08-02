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
package org.eclipse.che.ide.dto;

/**
 * Provides implementation of DTO interface.
 *
 * @param <DTO> the type of DTO interface which implementation this provider provides
 * @author Artem Zatsarynnyi
 */
public interface DtoProvider<DTO> {
  /** Get type of interface which implementation this provider provides. */
  Class<? extends DTO> getImplClass();

  /** Provides implementation of DTO interface from the specified JSON string. */
  DTO fromJson(String json);

  /** Get new implementation of DTO interface. */
  DTO newInstance();
}
