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
package org.eclipse.che.ide.ext.java.client.search;

import com.google.common.base.Optional;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.rest.HTTPStatus;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.providers.DynaObject;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * Presenter for Find Usages tree
 *
 * @author Evgen Vidolob
 */
@Singleton
@DynaObject
public class FindUsagesPresenter extends BasePresenter implements FindUsagesView.ActionDelegate {


    private       WorkspaceAgent           workspaceAgent;
    private       JavaLocalizationConstant localizationConstant;
    private       FindUsagesView           view;
    private       JavaSearchService        searchService;
    private       DtoFactory               dtoFactory;
    private       NotificationManager      manager;
    private final Resources                resources;

    @Inject
    public FindUsagesPresenter(WorkspaceAgent workspaceAgent,
                               JavaLocalizationConstant localizationConstant,
                               FindUsagesView view,
                               JavaSearchService searchService,
                               DtoFactory dtoFactory,
                               NotificationManager manager,
                               Resources resources) {
        this.workspaceAgent = workspaceAgent;
        this.localizationConstant = localizationConstant;
        this.view = view;
        this.searchService = searchService;
        this.dtoFactory = dtoFactory;
        this.manager = manager;
        this.resources = resources;
        view.setDelegate(this);
    }

    @Override
    public String getTitle() {
        return localizationConstant.findUsagesPartTitle();
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.findUsagesPartTitleTooltip();
    }

    @Override
    public SVGResource getTitleImage() {
        return resources.find();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    public void findUsages(TextEditor activeEditor) {

        final VirtualFile virtualFile = activeEditor.getEditorInput().getFile();

        if (virtualFile instanceof Resource) {
            final Project project = ((Resource)virtualFile).getRelatedProject().get();

            if (project == null) {
                return;
            }

            final Optional<Resource> srcFolder = ((Resource)virtualFile).getParentWithMarker(SourceFolderMarker.ID);

            if (!srcFolder.isPresent()) {
                return;
            }

            final String fqn = JavaUtil.resolveFQN((Container)srcFolder.get(), (Resource)virtualFile);

            String projectPath = project.getLocation().toString();
            FindUsagesRequest request = dtoFactory.createDto(FindUsagesRequest.class);
            request.setFQN(fqn);
            request.setProjectPath(projectPath);
            request.setOffset(activeEditor.getCursorOffset());

            Promise<FindUsagesResponse> promise = searchService.findUsages(request);
            promise.then(new Operation<FindUsagesResponse>() {
                @Override
                public void apply(FindUsagesResponse arg) throws OperationException {
                    handleResponse(arg);
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    Throwable cause = arg.getCause();
                    if (cause instanceof ServerException) {
                        handleError(((ServerException)cause).getHTTPStatus(), cause.getMessage());
                        return;
                    }
                    //in case websocket request
                    if (cause instanceof org.eclipse.che.ide.websocket.rest.exceptions.ServerException) {
                        handleError(((org.eclipse.che.ide.websocket.rest.exceptions.ServerException)cause).getHTTPStatus(),
                                    cause.getMessage());
                        return;
                    }
                    Log.error(getClass(), arg);
                    manager.notify(localizationConstant.failedToProcessFindUsage(), arg.getMessage(), FAIL, FLOAT_MODE);
                }
            });
        }


    }

    private void handleError(int statusCode, String message) {
        if (statusCode == HTTPStatus.BAD_REQUEST) {
            manager.notify(localizationConstant.failedToProcessFindUsage(),
                           JSONParser.parseLenient(message).isObject().get("message").isString().stringValue(), FAIL, FLOAT_MODE);
        } else {
            manager.notify(localizationConstant.failedToProcessFindUsage(), message, FAIL, FLOAT_MODE);
        }
    }

    private void handleResponse(FindUsagesResponse response) {
        workspaceAgent.openPart(this, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(this);
        view.showUsages(response);
    }
}
