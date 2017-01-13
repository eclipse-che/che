/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.outputspanel.console;

import com.google.inject.name.Named;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.command.CommandImpl;

/** @author Artem Zatsarynnyi */
public interface CommandConsoleFactory {

    /** Create the instance of {@link CommandOutputConsole} for the given {@code command}. */
    @Named("command")
    CommandOutputConsole create(CommandImpl command, Machine machine);

    /** Create the instance of {@link DefaultOutputConsole} for the given title. */
    @Named("default")
    OutputConsole create(String title);
}
