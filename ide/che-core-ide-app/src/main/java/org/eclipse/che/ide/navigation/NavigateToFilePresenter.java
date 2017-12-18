/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.navigation;

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.common.base.Optional;
import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.ProjectSearchRequestDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Presenter for file navigation (find file by name and open it).
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NavigateToFilePresenter implements NavigateToFileView.ActionDelegate {

  private final EditorAgent editorAgent;
  private final RequestTransmitter requestTransmitter;
  private final DtoFactory dtoFactory;
  private final NavigateToFileView view;
  private final AppContext appContext;

  @Inject
  public NavigateToFilePresenter(
      NavigateToFileView view,
      AppContext appContext,
      EditorAgent editorAgent,
      RequestTransmitter requestTransmitter,
      DtoFactory dtoFactory) {
    this.view = view;
    this.appContext = appContext;
    this.editorAgent = editorAgent;
    this.requestTransmitter = requestTransmitter;
    this.dtoFactory = dtoFactory;

    this.view.setDelegate(this);
  }

  /** Show dialog with view for navigation. */
  public void showDialog() {
    view.showPopup();
  }

  @Override
  public void onFileSelected(Path path) {
    view.hidePopup();

    appContext
        .getWorkspaceRoot()
        .getFile(path)
        .then(
            new Operation<Optional<File>>() {
              @Override
              public void apply(Optional<File> optFile) throws OperationException {
                if (optFile.isPresent()) {
                  editorAgent.openEditor(optFile.get());
                }
              }
            });
  }

  @Override
  public void onFileNameChanged(String fileName) {
    if (fileName.isEmpty()) {
      view.showItems(emptyList());
      return;
    }

    ProjectSearchRequestDto requestParams =
        dtoFactory
            .createDto(ProjectSearchRequestDto.class)
            .withPath("")
            .withName(URL.encodePathSegment(fileName + "*"));

    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName("project/search")
        .paramsAsDto(requestParams)
        .sendAndReceiveResultAsDto(ProjectSearchResponseDto.class, 20_000)
        .onSuccess(response -> view.showItems(response.getItemReferences()))
        .onFailure(error -> Log.error(getClass(), error.getMessage()))
        .onTimeout(() -> Log.error(getClass(), "Project search request failed due timeout"));
  }
}
