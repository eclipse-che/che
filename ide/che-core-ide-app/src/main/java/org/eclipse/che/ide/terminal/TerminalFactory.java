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
package org.eclipse.che.ide.terminal;

import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;

/**
 * Special factory for creating {@link TerminalPresenter} instances.
 *
 * @author Dmitry Shnurenko
 * @author Alexander Andrienko
 */
public interface TerminalFactory {

  /**
   * Creates terminal for current machine.
   *
   * @param machine machine for which terminal will be created
   * @param options options for new terminal
   * @return an instance of {@link TerminalPresenter}
   */
  TerminalPresenter create(@Assisted MachineImpl machine, @Assisted TerminalOptionsJso options);
}
