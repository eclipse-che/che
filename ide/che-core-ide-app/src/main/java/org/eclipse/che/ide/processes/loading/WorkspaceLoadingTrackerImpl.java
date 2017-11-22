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
package org.eclipse.che.ide.processes.loading;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.MachineRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.model.EnvironmentImpl;
import org.eclipse.che.ide.api.workspace.model.MachineConfigImpl;
import org.eclipse.che.ide.command.toolbar.processes.ProcessesListView;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.panel.EnvironmentOutputEvent;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.processes.panel.ProcessesPanelView;

/** Listens workspace events and outputs and visualizes the workspace loading process. */
@Singleton
public class WorkspaceLoadingTrackerImpl
    implements WorkspaceLoadingTracker, EnvironmentOutputEvent.Handler {

  /** Part of a docker image. */
  private class Chunk {
    long size = 0;
    long downloaded = 0;
  }

  /** Docker image. */
  private class Image {
    private String machineName;
    private String dockerImage;
    private Map<String, Chunk> chunks;
    private boolean downloaded;

    public Image(String machineName) {
      this.machineName = machineName;
      chunks = new HashMap<>();
    }

    public String getMachineName() {
      return machineName;
    }

    public void setDockerImage(String dockerImage) {
      this.dockerImage = dockerImage;
    }

    public String getDockerImage() {
      return dockerImage;
    }

    public Map<String, Chunk> getChunks() {
      return chunks;
    }

    public void setDownloaded(boolean downloaded) {
      this.downloaded = downloaded;
    }

    public boolean isDownloaded() {
      return downloaded;
    }
  }

  private final AppContext appContext;
  private final EventBus eventBus;
  private final ProcessesPanelPresenter processesPanelPresenter;
  private final MachineResources resources;

  private final WorkspaceLoadingTrackerView view;
  private final ProcessesListView processesListView;
  private final CoreLocalizationConstant localizationConstant;

  private Map<String, Image> images = new HashMap<>();

  private int percentage = 0;
  private int delta = 0;

  @Inject
  public WorkspaceLoadingTrackerImpl(
      AppContext appContext,
      EventBus eventBus,
      ProcessesPanelPresenter processesPanelPresenter,
      MachineResources resources,
      WorkspaceLoadingTrackerView view,
      ProcessesListView processesListView,
      CoreLocalizationConstant localizationConstant) {

    this.appContext = appContext;
    this.eventBus = eventBus;
    this.processesPanelPresenter = processesPanelPresenter;
    this.resources = resources;
    this.view = view;
    this.processesListView = processesListView;
    this.localizationConstant = localizationConstant;

    eventBus.addHandler(
        WorkspaceRunningEvent.TYPE,
        event -> {
          onWorkspaceRunnning();
        });

    eventBus.addHandler(
        MachineRunningEvent.TYPE,
        event -> {
          view.onMachineRunning(event.getMachine().getName());
          processesListView.setLoadingMessage(
              localizationConstant.menuLoaderMachineRunning(event.getMachine().getName()));
          percentage += delta;
          processesListView.setLoadingProgress(percentage);
        });
  }

  private void onWorkspaceRunnning() {
    for (String machineName : images.keySet()) {
      view.onPullingComplete(machineName);
    }

    view.onWorkspaceStarted();
    processesListView.setLoadingMessage(localizationConstant.menuLoaderWorkspaceStarted());
    processesListView.setLoadingProgress(100);

    Scheduler.get()
        .scheduleDeferred(
            () -> {
              ((ProcessesPanelView) processesPanelPresenter.getView()).showProcessOutput("*");
            });

    /* Delay in switching to command execution mode */
    new Timer() {
      @Override
      public void run() {
        processesListView.setExecMode();
      }
    }.schedule(3000);
  }

  @Override
  public void startTracking() {
    if (WorkspaceStatus.RUNNING == appContext.getWorkspace().getStatus()) {
      return;
    }

    view.startLoading();

    processesListView.setLoadMode();
    processesListView.setLoadingMessage(localizationConstant.menuLoaderWaitingWorkspace());
    processesListView.setLoadingProgress(0);

    String defaultEnvironmentName = appContext.getWorkspace().getConfig().getDefaultEnv();
    EnvironmentImpl defaultEnvironment =
        appContext.getWorkspace().getConfig().getEnvironments().get(defaultEnvironmentName);

    Map<String, MachineConfigImpl> machines = defaultEnvironment.getMachines();
    for (final String machineName : machines.keySet()) {
      view.pullMachine(machineName);
      images.put(machineName, new Image(machineName));
    }

    delta = 100 / machines.size() / 2;

    eventBus.addHandler(EnvironmentOutputEvent.TYPE, this);
    ((ProcessesPanelView) processesPanelPresenter.getView())
        .addWidget("*", "Workspace-start", resources.output(), view, true);
  }

  @Override
  public void onEnvironmentOutput(EnvironmentOutputEvent event) {
    Image machine = images.get(event.getMachineName());
    if (machine == null) {
      return;
    }

    String text = event.getContent();

    if (text.startsWith("[DOCKER] ")) {
      handleDockerOutput(machine, text);
      return;
    }
  }

  private void handleDockerOutput(Image machine, String text) {
    try {
      if (dockerPullingLatest(machine, text)) {
        // [DOCKER] latest: Pulling from eclipse/ubuntu_jdk8
        // Indicates the latest version is being downloading and contains image URL
        return;

      } else if (dockerPullingStarted(machine, text)) {
        // [DOCKER] sha256:40a6dd3c1f3af152d834e66fdf1dbca722dbc8ab4e98e157251c5179e8a6aa44: Pulling
        // from docker.io/eclipse/ubuntu_jdk8
        // Containing image SHA and image URL
        return;

      } else if (dockerPullingFinished(machine, text)) {
        // [DOCKER] Digest: sha256:40a6dd3c1f3af152d834e66fdf1dbca722dbc8ab4e98e157251c5179e8a6aa44
        // indicates image has been fully downloaded
        return;

      } else if (dockerPreparePullingChunk(machine, text)) {
        // [DOCKER] 6a447dcfe27d: Pulling fs layer
        // [DOCKER] d010c8cf75d7: Waiting
        return;

      } else if (dockerChunkPullingProgress(machine, text)) {
        // [DOCKER] 9fb6c798fa41: Downloading 16.22 MB/47.54 MB
        // gives how much of chunk has been already downloaded
        return;

      } else if (dockerChunkPullingCompleted(machine, text)) {
        // [DOCKER] 6fabefc10853: Download complete
        // mark chunk as fully downloaded
        return;
      }

    } catch (Exception e) {
      return;
    }
  }

  /**
   * [DOCKER] latest: Pulling from eclipse/ubuntu_jdk8
   *
   * @param machine
   * @param text
   * @return
   */
  private boolean dockerPullingLatest(Image machine, String text) {
    if (!text.startsWith("[DOCKER] latest: Pulling from ")) {
      return false;
    }

    String dockerImage = text.substring("[DOCKER] latest: Pulling from ".length()).trim();
    machine.setDockerImage(dockerImage);
    view.setMachineImage(machine.getMachineName(), dockerImage);
    processesListView.setLoadingMessage(localizationConstant.menuLoaderPullingImage(dockerImage));

    return true;
  }

  /**
   * [DOCKER] sha256:40a6dd3c1f3af152d834e66fdf1dbca722dbc8ab4e98e157251c5179e8a6aa44: Pulling from
   * docker.io/eclipse/ubuntu_jdk8
   *
   * @param machine
   * @param text
   * @return
   */
  private boolean dockerPullingStarted(Image machine, String text) {
    if (!text.startsWith("[DOCKER] sha256:")) {
      return false;
    }

    String[] parts = text.split(":");

    String dockerImage = parts[2];
    if (dockerImage.startsWith(" Pulling from ")) {
      dockerImage = dockerImage.substring(" Pulling from ".length()).trim();
      machine.setDockerImage(dockerImage);
      view.setMachineImage(machine.getMachineName(), dockerImage);
      processesListView.setLoadingMessage(localizationConstant.menuLoaderPullingImage(dockerImage));
    }

    return true;
  }

  /**
   * [DOCKER] Digest: sha256:40a6dd3c1f3af152d834e66fdf1dbca722dbc8ab4e98e157251c5179e8a6aa44
   *
   * @param machine
   * @param text
   * @return
   */
  private boolean dockerPullingFinished(Image machine, String text) {
    if (!text.startsWith("[DOCKER] Digest: ")) {
      return false;
    }

    machine.setDownloaded(true);

    view.onPullingComplete(machine.getMachineName());
    view.startWorkspaceMachines();
    view.startWorkspaceMachine(machine.getMachineName(), machine.getDockerImage());
    processesListView.setLoadingMessage(
        localizationConstant.menuLoaderMachineStarting(machine.getMachineName()));

    percentage += delta;
    processesListView.setLoadingProgress(percentage);

    return true;
  }

  /**
   * [DOCKER] 6a447dcfe27d: Pulling fs layer [DOCKER] d010c8cf75d7: Waiting
   *
   * @param machine
   * @param text
   * @return
   */
  private boolean dockerPreparePullingChunk(Image machine, String text) {
    if (!(text.startsWith("[DOCKER] ")
        && (text.indexOf(": Pulling fs layer") > 0 || text.indexOf(": Waiting") > 0))) {
      return false;
    }

    text = text.substring("[DOCKER] ".length());

    String[] parts = text.split(":");
    String hash = parts[0];

    Chunk chunk = machine.getChunks().get(hash);
    if (chunk == null) {
      chunk = new Chunk();
      machine.getChunks().put(hash, chunk);
    }

    return true;
  }

  /**
   * [DOCKER] e7cfbd075aa8: Downloading 67.58 MB/244.3 MB
   *
   * @param machine
   * @param text
   * @return
   */
  private boolean dockerChunkPullingProgress(Image machine, String text) {
    if (!(text.startsWith("[DOCKER] ") && text.indexOf(": Downloading ") > 0)) {
      return false;
    }

    text = text.substring("[DOCKER] ".length());
    // now text must be like `e7cfbd075aa8: Downloading 67.58 MB/244.3 MB`

    String[] parts = text.split(":");

    String hash = parts[0];
    String value = parts[1].substring(" Downloading ".length());
    // now value must be like `67.58 MB/244.3 MB`

    String[] values = value.split("/");

    long downloaded = getSizeInBytes(values[0]);
    long size = getSizeInBytes(values[1]);

    Chunk chunk = machine.getChunks().get(hash);
    if (chunk == null) {
      chunk = new Chunk();
      machine.getChunks().put(hash, chunk);
    }

    chunk.downloaded = downloaded;
    chunk.size = size;

    refreshMachineDownloadingProgress(machine);

    return true;
  }

  private void refreshMachineDownloadingProgress(Image machine) {
    long totalDownloaded = 0;
    long totalSize = 0;

    for (Chunk chunk : machine.chunks.values()) {
      totalSize += chunk.size;
      totalDownloaded += chunk.downloaded;
    }

    int percents = Math.round(totalDownloaded * 100 / totalSize);
    view.onPullingProgress(machine.getMachineName(), percents);
  }

  /**
   * `621 B` -> return 621 `490.8 kB` -> return 490.8 * 1024 `1.474 MB` -> return 1.474 * 1024 *
   * 1024 `244.3 MB` -> return 244.3 * 1024 * 1024
   *
   * @param value
   * @return
   */
  private long getSizeInBytes(String value) {
    value = value.toUpperCase();
    long size = 0;

    if (value.endsWith(" GB")) {
      value = value.substring(0, value.length() - 3);
      size = (long) (Double.parseDouble(value) * 1024 * 1024 * 1024);
    } else if (value.endsWith("GB")) {
      value = value.substring(0, value.length() - 2);
      size = (long) (Double.parseDouble(value) * 1024 * 1024 * 1024);
    } else if (value.endsWith(" MB")) {
      value = value.substring(0, value.length() - 3);
      size = (long) (Double.parseDouble(value) * 1024 * 1024);
    } else if (value.endsWith("MB")) {
      value = value.substring(0, value.length() - 2);
      size = (long) (Double.parseDouble(value) * 1024 * 1024);
    } else if (value.endsWith(" KB")) {
      value = value.substring(0, value.length() - 3);
      size = (long) (Double.parseDouble(value) * 1024);
    } else if (value.endsWith("KB")) {
      value = value.substring(0, value.length() - 2);
      size = (long) (Double.parseDouble(value) * 1024);
    } else if (value.endsWith(" B")) {
      value = value.substring(0, value.length() - 2);
      size = Long.parseLong(value);
    } else if (value.endsWith("B")) {
      value = value.substring(0, value.length() - 1);
      size = Long.parseLong(value);
    }

    return size;
  }

  /**
   * [DOCKER] 6fabefc10853: Download complete
   *
   * @param machine
   * @param text
   * @return
   */
  private boolean dockerChunkPullingCompleted(Image machine, String text) {
    if (!(text.startsWith("[DOCKER] ") && text.indexOf(": Download complete") > 0)) {
      return false;
    }

    text = text.substring("[DOCKER] ".length());
    // now text must be like `e7cfbd075aa8: Download complete`

    String[] parts = text.split(":");

    String hash = parts[0];

    Chunk chunk = machine.getChunks().get(hash);
    if (chunk != null) {
      chunk.downloaded = chunk.size;
      refreshMachineDownloadingProgress(machine);
    }

    return true;
  }
}
