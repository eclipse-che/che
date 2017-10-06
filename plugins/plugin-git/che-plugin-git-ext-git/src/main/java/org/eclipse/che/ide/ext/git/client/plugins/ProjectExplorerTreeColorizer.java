package org.eclipse.che.ide.ext.git.client.plugins;

import static org.eclipse.che.ide.api.vcs.VcsStatus.ADDED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.ide.api.vcs.VcsStatus.UNTRACKED;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.project.shared.dto.event.GitChangeEventDto;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.vcs.VcsStatus;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitFileChangedHandler;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitFileChangedHandler.GitFileChangesSubscriber;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitStatusChangedHandler;
import org.eclipse.che.ide.ext.git.client.eventhandlers.GitStatusChangedHandler.GitStatusChangesSubscriber;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.tree.FileNode;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.Tree;

/**
 * Responsible for colorize project explorer items depending on their git status.
 *
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
@Singleton
public class ProjectExplorerTreeColorizer implements GitFileChangesSubscriber,
    GitStatusChangesSubscriber {

  private final GitFileChangedHandler gitFileChangedHandler;
  private final GitStatusChangedHandler gitStatusChangedHandler;
  private final Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider;

  @Inject
  public ProjectExplorerTreeColorizer(
      GitFileChangedHandler gitFileChangedHandler,
      GitStatusChangedHandler gitStatusChangedHandler,
      Provider<ProjectExplorerPresenter> projectExplorerPresenterProvider) {
    this.gitFileChangedHandler = gitFileChangedHandler;
    this.gitStatusChangedHandler = gitStatusChangedHandler;
    this.projectExplorerPresenterProvider = projectExplorerPresenterProvider;

    subscribeToGitEvents();
  }

  @Override
  public void onFileUnderGitChanged(String endpointId, GitChangeEventDto dto) {
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
              ((ResourceNode) node)
                  .getData()
                  .asFile()
                  .setVcsStatus(VcsStatus.from(dto.getType().toString()));
              tree.refresh(node);
            });
  }

  @Override
  public void onGitStatusChanged(String endpointId, Status status) {
    Tree tree = projectExplorerPresenterProvider.get().getTree();
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
