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
package org.eclipse.che.ide.ext.git.client;

import static org.eclipse.che.ide.api.vcs.VcsStatus.ADDED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.UNTRACKED;

import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.git.shared.EditedRegion;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.vcs.HasVcsChangeMarkerRender;
import org.eclipse.che.ide.api.vcs.VcsChangeMarkerRender;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;

/**
 * Receives and handel's git changes notifications caught by server side VFS file watching system.
 *
 * @author Igor Vinokur
 */
@Singleton
public class GitChangesHandler {

  private final Provider<EditorAgent> editorAgentProvider;
  private final Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider;
  private final Provider<EditorMultiPartStack> multiPartStackProvider;

  @Inject
  public GitChangesHandler(
      RequestHandlerConfigurator configurator,
      EventBus eventBus,
      GitServiceClient gitServiceClient,
      Provider<EditorAgent> editorAgentProvider,
      Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider,
      Provider<EditorMultiPartStack> multiPartStackProvider) {
    this.editorAgentProvider = editorAgentProvider;
    this.projectExplorerPresenterProvider = projectExplorerPresenterProvider;
    this.multiPartStackProvider = multiPartStackProvider;

    eventBus.addHandler(
        EditorOpenedEvent.TYPE,
        event -> {
          if (((File) event.getFile()).getVcsStatus() != MODIFIED) {
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
    configureHandler(configurator);
  }

  private void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event/git-change")
        .paramsAsDto(FileChangedEventDto.class)
        .noResult()
        .withBiConsumer(this::apply);

    configurator
        .newConfiguration()
        .methodName("event/git/statusChanged")
        .paramsAsDto(StatusChangedEventDto.class)
        .noResult()
        .withBiConsumer(this::apply);
  }

  public void apply(String endpointId, FileChangedEventDto dto) {
    Tree tree = projectExplorerPresenterProvider.get().getTree();
    tree.getNodeStorage()
        .getAll()
        .stream()
        .filter(
            node ->
                node instanceof FileNode
                    && ((ResourceNode) node)
                        .getData()
                        .getLocation()
                        .equals(Path.valueOf(dto.getPath())))
        .forEach(
            node -> {
              setVcsStatusToEditorsFile(
                  Path.valueOf(dto.getPath()), VcsStatus.from(dto.getStatus().toString()));

              ((ResourceNode) node)
                  .getData()
                  .asFile()
                  .setVcsStatus(VcsStatus.from(dto.getStatus().toString()));
              tree.refresh(node);
            });

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
              VcsStatus vcsStatus = VcsStatus.from(dto.getStatus().toString());
              EditorTab tab = multiPartStackProvider.get().getTabByPart(editor);
              if (vcsStatus != null) {
                tab.setTitleColor(vcsStatus.getColor());
              }
              VcsChangeMarkerRender render =
                  ((HasVcsChangeMarkerRender) editor).getVcsChangeMarkersRender();
              if (((File) editor.getEditorInput().getFile()).getVcsStatus() != MODIFIED) {
                render.clearAllChangeMarkers();
              } else {
                handleEditedRegions(dto.getEditedRegions(), render);
              }
            });
  }

  private void setVcsStatusToEditorsFile(Path filePath, VcsStatus status) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(
            editor ->
                editor.getEditorInput().getFile().getLocation().equals(filePath)
                    && editor instanceof HasVcsChangeMarkerRender)
        .forEach(editor -> ((File) editor.getEditorInput().getFile()).setVcsStatus(status));
  }

  private void handleEditedRegions(List<EditedRegion> editedRegions, VcsChangeMarkerRender render) {
    render.clearAllChangeMarkers();
    editedRegions.forEach(
        edition ->
            render.addChangeMarker(
                edition.getBeginLine(), edition.getEndLine(), edition.getType()));
  }

  public void apply(String endpointId, StatusChangedEventDto dto) {
    Tree tree = projectExplorerPresenterProvider.get().getTree();
    Status status = dto.getStatus();
    tree.getNodeStorage()
        .getAll()
        .stream()
        .filter(node -> node instanceof FileNode)
        .forEach(
            node -> {
              Resource resource = ((ResourceNode) node).getData();
              File file = resource.asFile();
              String nodeLocation = resource.getLocation().removeFirstSegments(1).toString();

              VcsStatus newVcsStatus;
              if (status.getUntracked().contains(nodeLocation)) {
                newVcsStatus = UNTRACKED;
              } else if (status.getModified().contains(nodeLocation)
                  || status.getChanged().contains(nodeLocation)) {
                newVcsStatus = MODIFIED;
              } else if (status.getAdded().contains(nodeLocation)) {
                newVcsStatus = ADDED;
              } else {
                newVcsStatus = NOT_MODIFIED;
              }

              if (file.getVcsStatus() != newVcsStatus) {
                file.setVcsStatus(newVcsStatus);
                tree.refresh(node);
              }
            });

    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(editor -> editor instanceof HasVcsChangeMarkerRender)
        .forEach(
            editor -> {
              EditorTab tab = multiPartStackProvider.get().getTabByPart(editor);
              String nodeLocation = tab.getFile().getLocation().removeFirstSegments(1).toString();
              if (status.getUntracked().contains(nodeLocation)) {
                tab.setTitleColor(UNTRACKED.getColor());
              } else if (status.getModified().contains(nodeLocation)
                  || status.getChanged().contains(nodeLocation)) {
                tab.setTitleColor(MODIFIED.getColor());
              } else if (status.getAdded().contains(nodeLocation)) {
                tab.setTitleColor(ADDED.getColor());
              } else {
                tab.setTitleColor(NOT_MODIFIED.getColor());
              }

              String filePath =
                  editor.getEditorInput().getFile().getLocation().removeFirstSegments(1).toString();

              if (editor instanceof HasVcsChangeMarkerRender) {
                VcsChangeMarkerRender render =
                    ((HasVcsChangeMarkerRender) editor).getVcsChangeMarkersRender();

                if (dto.getModifiedFiles().keySet().contains(filePath)) {
                  handleEditedRegions(dto.getModifiedFiles().get(filePath), render);
                } else {
                  render.clearAllChangeMarkers();
                }
              }
            });
  }
}
