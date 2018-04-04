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
