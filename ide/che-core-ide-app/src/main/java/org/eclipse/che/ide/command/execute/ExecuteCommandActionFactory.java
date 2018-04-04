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
