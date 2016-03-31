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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedEvent;
import org.eclipse.che.ide.api.event.project.CurrentProjectChangedHandler;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.plugin.svn.ide.action.SubversionAction;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.project.node.FolderReferenceNode;
import org.eclipse.che.ide.project.node.ProjectNode;

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
            {"M", "rgb(247, 47, 47)"},
            {"!", "grey"},
            {"?", "lightskyblue"},
            {"A", "chartreuse"},
            {"X", "yellow"},
            {"C", "yellow"},
            {"D", "rgb(247, 47, 47)"},
            {"+", "chartreuse"},
            {"-", "rgb(247, 47, 47)"},
            {"@", "cyan"},
            {"U", "chartreuse"},
            {"G", "chartreuse"}
    };

    protected final EventBus                         eventBus;
    protected final WorkspaceAgent                   workspaceAgent;
    private final   AppContext                       appContext;
    private final   SubversionOutputConsolePresenter console;
    private         boolean                          isViewClosed;

    private final ProjectExplorerPresenter projectExplorerPart;

    protected SubversionActionPresenter(final AppContext appContext,
                                        final EventBus eventBus,
                                        final SubversionOutputConsolePresenter console,
                                        final WorkspaceAgent workspaceAgent,
                                        final ProjectExplorerPresenter projectExplorerPart) {
        this.appContext = appContext;
        this.workspaceAgent = workspaceAgent;
        this.console = console;

        isViewClosed = true;

        this.eventBus = eventBus;
        this.projectExplorerPart = projectExplorerPart;

        eventBus.addHandler(CurrentProjectChangedEvent.TYPE, new CurrentProjectChangedHandler() {
            @Override
            public void onCurrentProjectChanged(CurrentProjectChangedEvent event) {
                isViewClosed = true;
                console.clear();
                workspaceAgent.hidePart(console);
            }
        });
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
     * Ensures view is opened.
     */
    protected void ensureViewOpened() {
        if (isViewClosed) {
            workspaceAgent.openPart(console, PartStackType.INFORMATION);
            isViewClosed = false;
        }
    }

    /**
     * Print the update output.
     *
     * @param lines text to be printed
     */
    protected void print(final List<String> lines) {
        ensureViewOpened();

        for (final String line : lines) {
            console.print(line);
        }
    }

    /**
     * Prints command line.
     *
     * @param command command line
     */
    protected void printCommand(String command) {
        ensureViewOpened();

        if (command.startsWith("'") || command.startsWith("\"")) {
            command += command.substring(1, command.length() - 1);
        }

        String line = "<span style=\"font-weight: bold; font-style: italic;\">$ " + command + "</span>";

        console.print(line);
    }

    protected void printErrors(final List<String> errors) {
        ensureViewOpened();

        for (final String line : errors) {
            console.print("<span style=\"color:red;\">" + SafeHtmlUtils.htmlEscape(line) + "</span>");
        }
    }

    /**
     * Colorizes and prints response to the output.
     *
     * @param command
     * @param output
     * @param errors
     */
    protected void printResponse(final String command, final List<String> output, final List<String> errors) {
        ensureViewOpened();

        if (command != null) {
            printCommand(command);
        }

        if (output != null) {
            for (final String line : output) {
                boolean found = false;

                if (!line.trim().isEmpty()) {
                    String prefix = line.trim().substring(0, 1);

                    for (String[] stcol : STATUS_COLORS) {
                        if (stcol[0].equals(prefix)) {
                            // TODO: Turn the file paths into links (where appropriate)
                            console.print("<span style=\"color:" + stcol[1] + ";\">" + SafeHtmlUtils.htmlEscape(line) + "</span>");
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    console.print(SafeHtmlUtils.htmlEscape(line));
                }
            }
        }

        if (errors != null) {
            for (final String line : errors) {
                console.print("<span style=\"color:red;\">" + SafeHtmlUtils.htmlEscape(line) + "</span>");
            }
        }

        console.print("");
    }

    /**
     * Print the update output & a blank line after.
     *
     * @param output text to be printed
     */
    protected void printAndSpace(final List<String> output) {
        print(output);
        console.print("");
    }

    protected CurrentProject getActiveProject() {
        return appContext.getCurrentProject();
    }
}
