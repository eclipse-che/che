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
package org.eclipse.che.ide.command.execute;

import org.eclipse.che.ide.api.command.CommandImpl;

/**
 * Factory for creating {@link ExecuteCommandAction} instances.
 *
 * @author Artem Zatsarynnyi
 */
public interface ExecuteCommandActionFactory {

  /**
   * Creates new instance of {@link ExecuteCommandAction} for executing the specified {@code
   * command}.
   */
  ExecuteCommandAction create(CommandImpl command);
}
