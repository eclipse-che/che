package org.eclipse.che.ide.ext.git.client.plugins;

import static org.eclipse.che.ide.api.vcs.VcsStatus.ADDED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.UNTRACKED;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.shared.dto.event.GitChangeEventDto;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.parts.EditorMultiPartStack;
import org.eclipse.che.ide.api.parts.EditorTab;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitFileChangedHandler;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitFileChangedHandler.GitFileChangesSubscriber;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitStatusChangedHandler;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitStatusChangedHandler.GitStatusChangesSubscriber;
import org.eclipse.che.ide.resource.Path;

/**
 * Responsible for colorize editor tabs depending on their git status.
 *
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
public class EditorTabsColorizer implements GitFileChangesSubscriber,
    GitStatusChangesSubscriber {

  private final GitFileChangedHandler gitFileChangedHandler;
  private final GitStatusChangedHandler gitStatusChangedHandler;
  private final Provider<EditorAgent> editorAgentProvider;
  private final Provider<EditorMultiPartStack> multiPartStackProvider;

  @Inject
  public EditorTabsColorizer(
      GitFileChangedHandler gitFileChangedHandler,
      GitStatusChangedHandler gitStatusChangedHandler,
      Provider<EditorAgent> editorAgentProvider,
      Provider<EditorMultiPartStack> multiPartStackProvider) {
    this.gitFileChangedHandler = gitFileChangedHandler;
    this.gitStatusChangedHandler = gitStatusChangedHandler;
    this.editorAgentProvider = editorAgentProvider;
    this.multiPartStackProvider = multiPartStackProvider;

    subscribeToGitEvents();
  }

  @Override
  public void onFileUnderGitChanged(String endpointId, GitChangeEventDto dto) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
        .stream()
        .filter(
            editor ->
                editor.getEditorInput().getFile().getLocation().equals(Path.valueOf(dto.getPath())))
        .forEach(
            editor -> {
              VcsStatus vcsStatus = VcsStatus.from(dto.getType().toString());
              EditorTab tab = multiPartStackProvider.get().getTabByPart(editor);
              if (vcsStatus != null) {
                tab.setTitleColor(vcsStatus.getColor());
              }
            });
  }

  @Override
  public void onGitStatusChanged(String endpointId, Status status) {
    editorAgentProvider
        .get()
        .getOpenedEditors()
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

  private void subscribeToGitEvents() {
    gitFileChangedHandler.subscribe(this);
    gitStatusChangedHandler.subscribe(this);
  }

  @PreDestroy
  private void unsubscribeFromGitEvents() {
    gitFileChangedHandler.unsubscribe(this);
    gitStatusChangedHandler.unsubscribe(this);
  }
}
