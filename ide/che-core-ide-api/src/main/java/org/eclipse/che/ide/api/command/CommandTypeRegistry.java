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
package org.eclipse.che.ide.api.command;

import java.util.Optional;
import java.util.Set;

/**
 * Registry for command types.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandTypeRegistry {

  /**
   * Returns {@code Optional} {@link CommandType} with the specified ID or {@code Optional.empty()}
   * if none.
   *
   * @param id the ID of the command type
   * @return {@link CommandType} or {@code Optional.absent()}
   */
  Optional<CommandType> getCommandTypeById(String id);

  /** Returns set of all registered {@link CommandType}s. */
  Set<CommandType> getCommandTypes();
}
