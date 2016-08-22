/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.svn.ide.common;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.action.SubversionAction;

import java.util.List;


/**
 * Presenter to be extended by all {@link SubversionAction} presenters.
 */
public class SubversionActionPresenter {

    protected final AppContext                     appContext;
    private final   SubversionOutputConsoleFactory consoleFactory;
    private final   ProcessesPanelPresenter        consolesPanelPresenter;
    private final   StatusColors                   statusColors;

    protected SubversionActionPresenter(AppContext appContext,
                                        SubversionOutputConsoleFactory consoleFactory,
                                        ProcessesPanelPresenter processesPanelPresenter,
                                        StatusColors statusColors) {
        this.appContext = appContext;
        this.consoleFactory = consoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.statusColors = statusColors;
    }

    protected Path[] toRelative(Container project, Resource[] paths) {
        if (paths == null || paths.length == 0) {
            return new Path[0];
        }

        Path[] rel = new Path[0];

        for (Resource resource : paths) {
            if (project.getLocation().isPrefixOf(resource.getLocation())) {
                Path temp = resource.getLocation().removeFirstSegments(project.getLocation().segmentCount());
                if (temp.segmentCount() == 0) {
                    temp = Path.valueOf(".");
                }
                rel = Arrays.add(rel, temp);
            }
        }

        return rel;
    }

    protected Path toRelative(Container project, Resource path) {
        return toRelative(project, new Resource[]{path})[0];
    }

    /**
     * Prints errors output in console.
     *
     * @param errors the error output
     * @param consoleTitle the title of the console to use
     */
    protected void printErrors(final List<String> errors, final String consoleTitle) {
        final SubversionOutputConsole console = consoleFactory.create(consoleTitle);
        for (final String line : errors) {
            console.printError(line);
        }
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
    }

    /**
     * Colorizes and prints response outputs in console.
     *
     * @param command the SVN command that was executed
     * @param output the command output
     * @param errors the error output
     * @param consoleTitle the title of the console to use
     */
    protected void printResponse(final String command, final List<String> output, final List<String> errors, final String consoleTitle) {
        final SubversionOutputConsole console = consoleFactory.create(consoleTitle);

        if (command != null) {
            printCommand(command, console);
        }

        if (output != null) {
            printOutput(output, console);
        }

        if (errors != null) {
            for (final String line : errors) {
                console.printError(line);
            }
        }

        consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
    }

    /**
     * Prints an executed command line in given console.
     *
     * @param command the SVN command that was executed
     * @param console the console to use to print
     */
    private void printCommand(String command, final SubversionOutputConsole console) {
        if (command.startsWith("'") || command.startsWith("\"")) {
            command += command.substring(1, command.length() - 1);
        }

        console.printCommand(command);
        console.print("");
    }

    /**
     * Prints output in given console.
     *
     * @param output the command output
     * @param console the console to use to print
     */
    private void printOutput(List<String> output, SubversionOutputConsole console) {
        for (final String line : output) {
            String trimLine = line.trim();
            if (!trimLine.isEmpty()) {
                String prefix = trimLine.substring(0, 1);

                final String color = statusColors.getStatusColor(prefix);
                    if (color != null) {
                        // TODO: Turn the file paths into links (where appropriate)
                        console.print(line, color);
                    } else {
                        console.print(line);
                    }
                }
            }

        console.print("");
    }
}
