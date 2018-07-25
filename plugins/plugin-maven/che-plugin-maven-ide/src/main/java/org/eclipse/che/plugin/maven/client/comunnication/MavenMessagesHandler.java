/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.client.comunnication;

import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.DefaultOutputConsole;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.plugin.maven.client.MavenJsonRpcHandler;
import org.eclipse.che.plugin.maven.client.comunnication.progressor.background.BackgroundLoaderPresenter;
import org.eclipse.che.plugin.maven.shared.dto.ArchetypeOutput;
import org.eclipse.che.plugin.maven.shared.dto.PercentMessageDto;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;
import org.eclipse.che.plugin.maven.shared.dto.TextMessageDto;

/**
 * Handler which receives messages from the maven server.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
@Singleton
public class MavenMessagesHandler {
  private final EventBus eventBus;
  private final BackgroundLoaderPresenter dependencyResolver;
  private final PomEditorReconciler pomEditorReconciler;
  private final ProcessesPanelPresenter processesPanelPresenter;
  private final AppContext appContext;

  private DefaultOutputConsole outputConsole;

  @Inject
  public MavenMessagesHandler(
      EventBus eventBus,
      MavenJsonRpcHandler mavenJsonRpcHandler,
      BackgroundLoaderPresenter dependencyResolver,
      PomEditorReconciler pomEditorReconciler,
      ProcessesPanelPresenter processesPanelPresenter,
      CommandConsoleFactory commandConsoleFactory,
      AppContext appContext) {
    this.eventBus = eventBus;
    this.dependencyResolver = dependencyResolver;
    this.pomEditorReconciler = pomEditorReconciler;
    this.processesPanelPresenter = processesPanelPresenter;
    this.appContext = appContext;

    mavenJsonRpcHandler.addTextHandler(this::handleTextNotification);
    mavenJsonRpcHandler.addStartStopHandler(this::handleStartStop);
    mavenJsonRpcHandler.addPercentHandler(this::handlePercentNotification);
    mavenJsonRpcHandler.addProjectsUpdateHandler(this::handleUpdate);
    mavenJsonRpcHandler.addArchetypeOutputHandler(this::onMavenArchetypeReceive);

    handleOperations();
    outputConsole = (DefaultOutputConsole) commandConsoleFactory.create("Maven Archetype");
  }

  private void handleOperations() {
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, event -> dependencyResolver.hide());
  }

  /**
   * Updates progress bar when the percent of project resolving is changed.
   *
   * @param percentMessageDto object with value of percent
   */
  protected void handlePercentNotification(PercentMessageDto percentMessageDto) {
    dependencyResolver.updateProgressBar((int) (percentMessageDto.getPercent() * 100));
  }

  /**
   * Updates progress label when the resolved project is changed.
   *
   * @param textMessageDto object with name of new label
   */
  protected void handleTextNotification(TextMessageDto textMessageDto) {
    dependencyResolver.show();
    dependencyResolver.setProgressLabel(textMessageDto.getText());
  }

  /**
   * Hides or shows a progress bar.
   *
   * @param dto describes a state of the project resolving
   */
  protected void handleStartStop(StartStopNotification dto) {
    if (dto.isStart()) {
      dependencyResolver.show();
    } else {
      dependencyResolver.hide();
    }
  }

  /**
   * Updates the tree of projects which were modified.
   *
   * @param dto describes a projects which were modified
   */
  protected void handleUpdate(ProjectsUpdateMessage dto) {
    List<String> updatedProjects = dto.getUpdatedProjects();
    Set<String> projectToRefresh = computeUniqueHiLevelProjects(updatedProjects);

    for (final String path : projectToRefresh) {
      appContext
          .getWorkspaceRoot()
          .getContainer(path)
          .then(
              container -> {
                if (container.isPresent()) {
                  container.get().synchronize();
                }
              });
    }

    pomEditorReconciler.reconcilePoms(updatedProjects);
  }

  private void onMavenArchetypeReceive(ArchetypeOutput output) {
    String message = output.getOutput();
    switch (output.getState()) {
      case START:
        processesPanelPresenter.addCommandOutput(outputConsole);
        outputConsole.clearOutputsButtonClicked();
        outputConsole.printText(message, "green");
        break;
      case IN_PROGRESS:
        outputConsole.printText(message);
        break;
      case DONE:
        outputConsole.printText(message, "green");
        break;
      case ERROR:
        outputConsole.printText(message, "red");
        break;
      default:
        break;
    }
  }

  private Set<String> computeUniqueHiLevelProjects(List<String> updatedProjects) {
    return updatedProjects
        .stream()
        .filter(each -> shouldBeUpdated(updatedProjects, each))
        .collect(toSet());
  }

  private boolean shouldBeUpdated(List<String> updatedProjects, String project) {
    for (String each : updatedProjects) {
      if (!project.equals(each) && project.startsWith(each)) {
        return false;
      }
    }
    return true;
  }
}
