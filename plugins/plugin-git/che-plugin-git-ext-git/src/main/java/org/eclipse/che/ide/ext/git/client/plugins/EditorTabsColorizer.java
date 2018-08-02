/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.plugins;

import static org.eclipse.che.ide.api.vcs.VcsStatus.ADDED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.UNTRACKED;
import static org.eclipse.che.ide.ext.git.client.GitUtil.getRootPath;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.RepositoryDeletedEventDto;
import org.eclipse.che.api.git.shared.RepositoryInitializedEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.vcs.HasVcsChangeMarkerRender;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.ext.git.client.GitEventSubscribable;
import org.eclipse.che.ide.ext.git.client.GitEventsSubscriber;
import org.eclipse.che.ide.resource.Path;

/**
 * Responsible for colorize editor tabs depending on their git status.
 *
 * @author Igor Vinokur
 */
public class EditorTabsColorizer implements GitEventsSubscriber {

  private final Provider<EditorAgent> editorAgentProvider;
  private final Provider<EditorMultiPartStack> multiPartStackProvider;

  @Inject
  public EditorTabsColorizer(
      GitEventSubscribable subscribeToGitEvents,
      Provider<EditorAgent> editorAgentProvider,
      Provider<EditorMultiPartStack> multiPartStackProvider) {
    this.editorAgentProvider = editorAgentProvider;
    this.multiPartStackProvider = multiPartStackProvider;

    subscribeToGitEvents.addSubscriber(this);
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
              VcsStatus vcsStatus = VcsStatus.from(dto.getStatus().toString());
              // set vcs status to editor file
              ((File) editor.getEditorInput().getFile()).setVcsStatus(vcsStatus);
              EditorTab tab = multiPartStackProvider.get().getTabByPart(editor);
              if (vcsStatus != null) {
                tab.setTitleColor(vcsStatus.getColor());
              }
            });
  }

  @Override
  public void onGitStatusChanged(String endpointId, StatusChangedEventDto statusChangedEventDto) {
    Status status = statusChangedEventDto.getStatus();
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(
            editor ->
                editor instanceof HasVcsChangeMarkerRender
                    && statusChangedEventDto
                        .getProjectName()
                        .equals(getRootPath(editor.getEditorInput().getFile().getLocation())))
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
            });
  }

  @Override
  public void onGitRepositoryDeleted(
      String endpointId, RepositoryDeletedEventDto repositoryDeletedEventDto) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(editor -> editor instanceof HasVcsChangeMarkerRender)
        .forEach(
            editor ->
                multiPartStackProvider
                    .get()
                    .getTabByPart(editor)
                    .setTitleColor(NOT_MODIFIED.getColor()));
  }

  @Override
  public void onGitRepositoryInitialized(
      String endpointId, RepositoryInitializedEventDto gitRepositoryInitializedEventDto) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(editor -> editor instanceof HasVcsChangeMarkerRender)
        .forEach(
            editor ->
                multiPartStackProvider
                    .get()
                    .getTabByPart(editor)
                    .setTitleColor(UNTRACKED.getColor()));
  }
}
