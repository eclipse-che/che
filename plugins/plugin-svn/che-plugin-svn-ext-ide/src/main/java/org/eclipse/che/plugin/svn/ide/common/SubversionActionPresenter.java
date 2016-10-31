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

import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.subversion.Credentials;
import org.eclipse.che.ide.api.subversion.SubversionCredentialsDialog;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.Arrays;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.action.SubversionAction;

import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;

/**
 * Presenter to be extended by all {@link SubversionAction} presenters.
 */
public class SubversionActionPresenter {

    protected final AppContext                               appContext;
    private final   SubversionOutputConsoleFactory           consoleFactory;
    private final   ProcessesPanelPresenter                  consolesPanelPresenter;
    private final   StatusColors                             statusColors;
    private final   SubversionExtensionLocalizationConstants locale;
    private final   NotificationManager                      notificationManager;
    private final   SubversionCredentialsDialog              credentialsDialog;

    protected SubversionActionPresenter(AppContext appContext,
                                        SubversionOutputConsoleFactory consoleFactory,
                                        ProcessesPanelPresenter processesPanelPresenter,
                                        StatusColors statusColors,
                                        SubversionExtensionLocalizationConstants locale,
                                        NotificationManager notificationManager,
                                        SubversionCredentialsDialog credentialsDialog) {
        this.appContext = appContext;
        this.consoleFactory = consoleFactory;
        this.consolesPanelPresenter = processesPanelPresenter;
        this.statusColors = statusColors;
        this.locale = locale;
        this.notificationManager = notificationManager;
        this.credentialsDialog = credentialsDialog;
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
     * @param errors
     *         the error output
     * @param consoleTitle
     *         the title of the console to use
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
     * @param command
     *         the SVN command that was executed
     * @param output
     *         the command output
     * @param errors
     *         the error output
     * @param consoleTitle
     *         the title of the console to use
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
     * Performs subversion operation. If this operations fails with authorization error
     * the operation will be recalled with requested credentials
     *
     * @param notification
     *         progress notification to set operation status
     */
    protected <Y> Promise<Y> performOperationWithCredentialsRequestIfNeeded(final RemoteSubversionOperation<Y> operation,
                                                                            @Nullable final StatusNotification notification) {
        return operation.perform(null)
                        .catchErrorPromise(new Function<PromiseError, Promise<Y>>() {
                            @Override
                            public Promise<Y> apply(PromiseError error) throws FunctionException {
                                if (getErrorCode(error.getCause()) == ErrorCodes.UNAUTHORIZED_SVN_OPERATION) {
                                    if (notification != null) {
                                        notification.setTitle(locale.waitingCredentials());
                                        notification.setStatus(PROGRESS);
                                    } else {
                                        notificationManager.notify(error.getMessage(), FAIL, FLOAT_MODE);
                                    }

                                    return credentialsDialog.askCredentials()
                                                            .thenPromise(new Function<Credentials, Promise<Y>>() {
                                                                @Override
                                                                public Promise<Y> apply(Credentials credentials) throws FunctionException {
                                                                    return operation.perform(credentials);
                                                                }
                                                            });
                                }
                                return Promises.reject(error);
                            }
                        });
    }

    /**
     * Remote Subversion operation that can require credentials.
     */
    protected interface RemoteSubversionOperation<Y> {
        Promise<Y> perform(Credentials credentials);
    }

    /**
     * Prints an executed command line in given console.
     *
     * @param command
     *         the SVN command that was executed
     * @param console
     *         the console to use to print
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
     * @param output
     *         the command output
     * @param console
     *         the console to use to print
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
