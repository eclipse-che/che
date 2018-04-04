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
package org.eclipse.che.ide.ext.git.client.plugins;

import static org.eclipse.che.ide.api.vcs.VcsStatus.MODIFIED;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.git.shared.EditedRegion;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.vcs.HasVcsChangeMarkerRender;
import org.eclipse.che.ide.api.vcs.HasVcsStatus;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRender;
import org.eclipse.che.ide.ext.git.client.GitEventSubscribable;
import org.eclipse.che.ide.ext.git.client.GitEventsSubscriber;
import org.eclipse.che.ide.ext.git.client.GitServiceClient;
import org.eclipse.che.ide.resource.Path;

/** @author Igor Vinokur */
@Singleton
public class GitChangeMarkerManager implements GitEventsSubscriber {

  private final Provider<EditorAgent> editorAgentProvider;

  @Inject
  public GitChangeMarkerManager(
      GitEventSubscribable subscribeToGitEvents,
      EventBus eventBus,
      GitServiceClient gitServiceClient,
      Provider<EditorAgent> editorAgentProvider) {
    this.editorAgentProvider = editorAgentProvider;

    eventBus.addHandler(
        EditorOpenedEvent.TYPE,
        event -> {
          if (!(event.getFile() instanceof HasVcsStatus)) {
            return;
          }
          HasVcsStatus file = (HasVcsStatus) event.getFile();
          if (file.getVcsStatus() != MODIFIED) {
            return;
          }
          VcsChangeMarkerRender render =
              ((HasVcsChangeMarkerRender) event.getEditor()).getVcsChangeMarkersRender();

          Path location = event.getFile().getLocation();
          gitServiceClient
              .getEditedRegions(location.uptoSegment(1), location.removeFirstSegments(1))
              .then(
                  edition -> {
                    handleEditedRegions(edition, render);
                  });
        });

    subscribeToGitEvents.addSubscriber(this);
  }

  private void handleEditedRegions(List<EditedRegion> editedRegions, VcsChangeMarkerRender render) {
    render.clearAllChangeMarkers();
    editedRegions.forEach(
        edition ->
            render.addChangeMarker(
                edition.getBeginLine(), edition.getEndLine(), edition.getType()));
  }

  @Override
  public void onFileChanged(String endpointId, FileChangedEventDto dto) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(
            editor ->
                editor.getEditorInput().getFile().getLocation().equals(Path.valueOf(dto.getPath()))
                    && editor instanceof HasVcsChangeMarkerRender)
        .forEach(
            editor -> {
              VcsChangeMarkerRender render =
                  ((HasVcsChangeMarkerRender) editor).getVcsChangeMarkersRender();
              if (((File) editor.getEditorInput().getFile()).getVcsStatus() != MODIFIED) {
                render.clearAllChangeMarkers();
              } else {
                handleEditedRegions(dto.getEditedRegions(), render);
              }
            });
  }

  @Override
  public void onGitStatusChanged(String endpointId, StatusChangedEventDto statusChangedEventDto) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(editor -> editor instanceof HasVcsChangeMarkerRender)
        .forEach(
            editor -> {
              String filePath =
                  editor.getEditorInput().getFile().getLocation().removeFirstSegments(1).toString();
              VcsChangeMarkerRender render =
                  ((HasVcsChangeMarkerRender) editor).getVcsChangeMarkersRender();
              if (statusChangedEventDto.getModifiedFiles().keySet().contains(filePath)) {
                handleEditedRegions(statusChangedEventDto.getModifiedFiles().get(filePath), render);
              } else {
                render.clearAllChangeMarkers();
              }
            });
  }
}
