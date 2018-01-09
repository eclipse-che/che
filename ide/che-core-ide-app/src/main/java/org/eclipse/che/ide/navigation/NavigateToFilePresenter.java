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
import static org.eclipse.che.ide.util.NameUtils.getFileExtension;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.ProjectSearchRequestDto;
import org.eclipse.che.api.project.shared.dto.ProjectSearchResponseDto;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
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

  private static final int TYPING_PERIOD_DELAY_MS = 400;
  private static final Comparator<SearchResultDto> SEARCH_COMPARATOR =
      (o1, o2) -> {
        String ext1 = getFileExtension(o1.getItemReference().getName());
        String ext2 = getFileExtension(o2.getItemReference().getName());
        return ext1.compareToIgnoreCase(ext2);
      };

  private final EditorAgent editorAgent;
  private final RequestTransmitter requestTransmitter;
  private final DtoFactory dtoFactory;
  private final NavigateToFileView view;
  private final AppContext appContext;

  private Timer timer;

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
            optFile -> {
              if (optFile.isPresent()) {
                editorAgent.openEditor(optFile.get());
              }
            });
  }

  @Override
  public void onFileNameChanged(final String fileName) {
    if (fileName.isEmpty()) {
      view.showItems(emptyList());
      return;
    }

    ProjectSearchRequestDto requestParams =
        dtoFactory
            .createDto(ProjectSearchRequestDto.class)
            .withPath("")
            .withName(URL.encodePathSegment(fileName + "*"));
    if (timer != null) {
      timer.cancel();
    }

    timer =
        new Timer() {
          @Override
          public void run() {
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName("project/search")
                .paramsAsDto(requestParams)
                .sendAndReceiveResultAsDto(ProjectSearchResponseDto.class, 20_000)
                .onSuccess(
                    response -> {
                      // Check that the file name from request corresponds to the actual file name
                      // from the view.
                      if (fileName.equals(view.getFileName())) {
                        prepareResults(response);
                      }
                    })
                .onFailure(error -> Log.error(getClass(), error.getMessage()))
                .onTimeout(
                    () -> Log.error(getClass(), "Project search request failed due timeout"));
          }
        };
    timer.schedule(TYPING_PERIOD_DELAY_MS);
  }

  private void prepareResults(ProjectSearchResponseDto response) {
    List<SearchResultDto> results = response.getItemReferences();
    results.sort(SEARCH_COMPARATOR);
    view.showItems(results);
  }
}
