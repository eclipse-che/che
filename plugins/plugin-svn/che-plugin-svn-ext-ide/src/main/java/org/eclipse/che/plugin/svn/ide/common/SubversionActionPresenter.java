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

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.plugin.svn.ide.action.SubversionAction;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.plugin.svn.ide.common.PathTypeFilter.ALL;

/**
 * Presenter to be extended by all {@link SubversionAction} presenters.
 */
public class SubversionActionPresenter {

    static final String[][] STATUS_COLORS = {
            {"M", Style.getVcsConsoleStagedFilesColor()},
            {"!", Style.getMainFontColor()},
            {"?", Style.getMainMenuFontSelectedColor()},
            {"A", Style.getVcsConsoleStagedFilesColor()},
            {"X", Style.getMainFontColor()},
            {"C", Style.getMainFontColor()},
            {"D", Style.getVcsConsoleUnstagedFilesColor()},
            {"+", Style.getVcsConsoleStagedFilesColor()},
            {"-", Style.getVcsConsoleUnstagedFilesColor()},
            {"@", Style.getVcsConsoleChangesLineNumbersColor()},
            {"U", Style.getVcsConsoleModifiedFilesColor()},
            {"G", Style.getVcsConsoleModifiedFilesColor()}
    };

    private final AppContext                     appContext;
    private final SubversionOutputConsoleFactory consoleFactory;
    private final ConsolesPanelPresenter         consolesPanelPresenter;
    private final ProjectExplorerPresenter       projectExplorerPart;

    protected SubversionActionPresenter(final AppContext appContext,
                                        final SubversionOutputConsoleFactory consoleFactory,
                                        final ConsolesPanelPresenter consolesPanelPresenter,
                                        final ProjectExplorerPresenter projectExplorerPart) {
        this.appContext = appContext;
        this.consoleFactory = consoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;

        this.projectExplorerPart = projectExplorerPart;
    }

    /**
     * @return the current project path
     */
    protected String getCurrentProjectPath() {
        final CurrentProject currentProject = getActiveProject();
        ProjectConfigDto project;
        String projectPath = null;

        if (currentProject != null) {
            project = currentProject.getRootProject();

            if (project != null) {
                projectPath = project.getPath();
            }
        }

        return projectPath;
    }

    /**
     * Returns currently selected project item.
     * @return
     */
    protected HasStorablePath getSelectedNode() {
        Object selectedNode = projectExplorerPart.getSelection().getHeadElement();
        return selectedNode != null && selectedNode instanceof HasStorablePath ? (HasStorablePath)selectedNode : null;
    }

    /**
     * @return the selected paths or an empty list of there is no selection
     */
    @NotNull
    protected List<String> getSelectedPaths(final Collection<PathTypeFilter> filters) {
        final List<?> selection = projectExplorerPart.getSelection().getAllElements();
        final List<String> paths = new ArrayList<>();

        if (selection.isEmpty()) {
            return Collections.emptyList();
        }

            for (final Object item : selection) {
                if (matchesFilter(item, filters)) {
                    final String path = relativePath((HasStorablePath)item);
                    if (!path.isEmpty()) {
                        paths.add(path);
                    } else {
                        paths.add("."); //it may be root path for our project
                    }
                }
            }

        return paths;
    }

    /**
     * Returns relative node path in the project.
     *
     * @return relative node path
     */
    protected String relativePath(final HasStorablePath node) {
        String path = node.getStorablePath().replaceFirst(appContext.getCurrentProject().getRootProject().getPath(), ""); // TODO: Move to method

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    protected List<String> getSelectedPaths() {
        return getSelectedPaths(Collections.singleton(ALL));
    }

    protected boolean matchesFilter(final Object node, final Collection<PathTypeFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        for (final PathTypeFilter filter : filters) {
            if (filter == ALL && node instanceof HasStorablePath
                || filter == PathTypeFilter.FILE && node instanceof FileReferenceNode
                || filter == PathTypeFilter.FOLDER && node instanceof FolderReferenceNode
                || filter == PathTypeFilter.PROJECT && (node instanceof ProjectNode)) {
                return true;
            }
        }
        return false;
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
        consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
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

        consolesPanelPresenter.addCommandOutput(appContext.getDevMachineId(), console);
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
            boolean found = false;

            if (!line.trim().isEmpty()) {
                String prefix = line.trim().substring(0, 1);

                for (String[] stcol : STATUS_COLORS) {
                    if (stcol[0].equals(prefix)) {
                        // TODO: Turn the file paths into links (where appropriate)
                        console.print(line, stcol[1]);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                console.print(line);
            }
        }

        console.print("");
    }

    protected CurrentProject getActiveProject() {
        return appContext.getCurrentProject();
    }
}
