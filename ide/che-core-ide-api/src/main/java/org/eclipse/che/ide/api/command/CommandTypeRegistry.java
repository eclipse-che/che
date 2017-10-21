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
