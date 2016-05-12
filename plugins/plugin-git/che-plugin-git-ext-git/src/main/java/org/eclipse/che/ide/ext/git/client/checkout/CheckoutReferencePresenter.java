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
package org.eclipse.che.ide.ext.git.client.checkout;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.api.event.project.OpenProjectEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for checkout reference(branch, tag) name or commit hash.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class CheckoutReferencePresenter implements CheckoutReferenceView.ActionDelegate {
    public static final String CHECKOUT_COMMAND_NAME = "Git checkout";

    private final NotificationManager      notificationManager;
    private final GitServiceClient         service;
    private final AppContext               appContext;
    private final GitLocalizationConstant  constant;
    private final CheckoutReferenceView    view;
    private final ProjectExplorerPresenter projectExplorer;
    private final DtoFactory               dtoFactory;
    private final EditorAgent              editorAgent;
    private final EventBus                 eventBus;
    private final ProjectServiceClient     projectService;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final GitOutputConsoleFactory  gitOutputConsoleFactory;
    private final ConsolesPanelPresenter   consolesPanelPresenter;

    @Inject
    public CheckoutReferencePresenter(CheckoutReferenceView view,
                                      GitServiceClient service,
                                      AppContext appContext,
                                      GitLocalizationConstant constant,
                                      NotificationManager notificationManager,
                                      ProjectExplorerPresenter projectExplorer,
                                      DtoFactory dtoFactory,
                                      EditorAgent editorAgent,
                                      EventBus eventBus,
                                      ProjectServiceClient projectServiceClient,
                                      DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                      GitOutputConsoleFactory gitOutputConsoleFactory,
                                      ConsolesPanelPresenter consolesPanelPresenter) {
        this.view = view;
        this.projectExplorer = projectExplorer;
        this.dtoFactory = dtoFactory;
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
        this.view.setDelegate(this);
        this.service = service;
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.projectService = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.gitOutputConsoleFactory = gitOutputConsoleFactory;
        this.consolesPanelPresenter = consolesPanelPresenter;
    }

    /** Show dialog. */
    public void showDialog() {
        view.setCheckoutButEnableState(false);
        view.showDialog();
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    @Override
    public void onCheckoutClicked(final String reference) {
        view.close();
        final ProjectConfigDto project = appContext.getCurrentProject().getRootProject();
        service.checkout(appContext.getDevMachine(),
                         project,
                         dtoFactory.createDto(CheckoutRequest.class)
                                   .withName(reference)
                                   .withCreateNew(false),
                         new AsyncRequestCallback<String>() {
                             @Override
                             protected void onSuccess(String result) {
                                 //In this case we can have unconfigured state of the project,
                                 //so we must repeat the logic which is performed when we open a project
                                 Unmarshallable<ProjectConfigDto> unmarshaller =
                                         dtoUnmarshallerFactory.newUnmarshaller(ProjectConfigDto.class);
                                 projectService.getProject(appContext.getDevMachine(), project.getPath(),
                                                           new AsyncRequestCallback<ProjectConfigDto>(unmarshaller) {
                                                               @Override
                                                               protected void onSuccess(final ProjectConfigDto result) {
                                                                   if (!result.getProblems().isEmpty()) {
                                                                       eventBus.fireEvent(new OpenProjectEvent(result));
                                                                   } else {
                                                                       projectExplorer.reloadChildren();

                                                                       updateOpenedFiles();
                                                                   }
                                                               }

                                                               @Override
                                                               protected void onFailure(Throwable exception) {
                                                                   Log.error(getClass(), "Can't get project by path");
                                                               }
                                                           });
                             }

                             @Override
                             protected void onFailure(Throwable exception) {
                                 final String errorMessage = (exception.getMessage() != null)
                                                             ? exception.getMessage()
                                                             : constant.checkoutFailed();
                                 GitOutputConsole console = gitOutputConsoleFactory.create(CHECKOUT_COMMAND_NAME);
                                 console.printError(errorMessage);
                                 consolesPanelPresenter.addCommandOutput(appContext.getDevMachine().getId(), console);
                                 notificationManager.notify(constant.checkoutFailed(), FAIL, FLOAT_MODE, project);
                             }
                         }
                        );
    }

    private void updateOpenedFiles() {
        for (EditorPartPresenter editorPartPresenter : editorAgent.getOpenedEditors()) {
            VirtualFile file = editorPartPresenter.getEditorInput().getFile();

            eventBus.fireEvent(new FileContentUpdateEvent(file.getPath()));
        }
    }

    @Override
    public void referenceValueChanged(String reference) {
        view.setCheckoutButEnableState(isInputCorrect(reference));
    }

    @Override
    public void onEnterClicked() {
        String reference = view.getReference();
        if (isInputCorrect(reference)) {
            onCheckoutClicked(reference);
        }
    }

    private boolean isInputCorrect(String reference) {
        return reference != null && !reference.isEmpty();
    }
}
