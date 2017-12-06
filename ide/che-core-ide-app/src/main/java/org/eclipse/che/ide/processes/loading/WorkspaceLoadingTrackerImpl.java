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

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.InstallerFailedEvent;
import org.eclipse.che.ide.api.workspace.event.InstallerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.InstallerStartingEvent;
import org.eclipse.che.ide.api.workspace.event.MachineRunningEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.EnvironmentImpl;
import org.eclipse.che.ide.api.workspace.model.MachineConfigImpl;
import org.eclipse.che.ide.command.toolbar.processes.ProcessesListView;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.processes.panel.ProcessesPanelView;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;

/** Listens workspace events and outputs and visualizes the workspace loading process. */
@Singleton
public class WorkspaceLoadingTrackerImpl
    implements WorkspaceLoadingTracker,
        InstallerStartingEvent.Handler,
        InstallerRunningEvent.Handler,
        InstallerFailedEvent.Handler,
        WorkspaceStartingEvent.Handler,
        WorkspaceRunningEvent.Handler,
        WorkspaceStoppedEvent.Handler,
        MachineStatusChangedEvent.Handler {

  private final AppContext appContext;
  private final ProcessesPanelPresenter processesPanelPresenter;
  private final MachineResources resources;

  private final WorkspaceLoadingTrackerView view;
  private final ProcessesListView processesListView;
  private final CoreLocalizationConstant localizationConstant;

  private AsyncRequestFactory asyncRequestFactory;

  private int percentage = 0;
  private int delta = 0;

  private Map<String, String> installernames = new HashMap<>();
  private Map<String, String> installerDescriptions = new HashMap<>();

  @Inject
  public WorkspaceLoadingTrackerImpl(
      AppContext appContext,
      EventBus eventBus,
      ProcessesPanelPresenter processesPanelPresenter,
      MachineResources resources,
      WorkspaceLoadingTrackerView view,
      ProcessesListView processesListView,
      CoreLocalizationConstant localizationConstant,
      AsyncRequestFactory asyncRequestFactory) {
    this.appContext = appContext;
    this.processesPanelPresenter = processesPanelPresenter;
    this.resources = resources;
    this.view = view;
    this.processesListView = processesListView;
    this.localizationConstant = localizationConstant;

    this.asyncRequestFactory = asyncRequestFactory;

    eventBus.addHandler(WorkspaceStartingEvent.TYPE, this);
    eventBus.addHandler(WorkspaceRunningEvent.TYPE, this);
    eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);

    eventBus.addHandler(
        MachineRunningEvent.TYPE,
        event -> {
          processesListView.setLoadingMessage(
              localizationConstant.menuLoaderMachineRunning(event.getMachine().getName()));
          percentage += delta;
          processesListView.setLoadingProgress(percentage);
        });

    eventBus.addHandler(InstallerStartingEvent.TYPE, this);
    eventBus.addHandler(InstallerRunningEvent.TYPE, this);
    eventBus.addHandler(InstallerFailedEvent.TYPE, this);

    eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);

    Scheduler.get().scheduleDeferred(() -> loadInstallers());
  }

  @Override
  public void startTracking() {
    if (WorkspaceStatus.RUNNING == appContext.getWorkspace().getStatus()) {
      return;
    }

    showWorkspaceStartPanel();
    addMachines();

    processesListView.setLoadMode();
    processesListView.setLoadingMessage(localizationConstant.menuLoaderWaitingWorkspace());
    processesListView.setLoadingProgress(0);
  }

  private void showWorkspaceStartPanel() {
    ((ProcessesPanelView) processesPanelPresenter.getView()).hideProcessOutput("*");

    ((ProcessesPanelView) processesPanelPresenter.getView())
        .addWidget("*", "Workspace Status", resources.output(), view, true);

    ((ProcessesPanelView) processesPanelPresenter.getView()).showProcessOutput("*");
  }

  private void addMachines() {
    String defaultEnvironmentName = appContext.getWorkspace().getConfig().getDefaultEnv();
    EnvironmentImpl defaultEnvironment =
        appContext.getWorkspace().getConfig().getEnvironments().get(defaultEnvironmentName);

    Map<String, MachineConfigImpl> machines = defaultEnvironment.getMachines();

    for (final String machineName : machines.keySet()) {
      MachineConfigImpl machineConfig = machines.get(machineName);

      view.addMachine(machineName);

      if ("db".equals(machineName)) {
        view.setMachineImageName(machineName, "eclipse/mysql");
      } else if ("dev-machine".equals(machineName)) {
        view.setMachineImageName(machineName, "eclipse/ubuntu_jdk8");
      }
    }

    delta = 100 / machines.size() / 2;
  }

  private void loadInstallers() {
    asyncRequestFactory
        .createGetRequest(appContext.getMasterApiEndpoint() + "/installer")
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .send(new StringUnmarshaller())
        .then(
            content -> {
              JSONValue parsed = JSONParser.parseStrict(content);
              if (parsed.isArray() == null) {
                return;
              }

              JSONArray installers = parsed.isArray();
              for (int i = 0; i < installers.size(); i++) {
                JSONObject installer = installers.get(i).isObject();

                String id = installer.get("id").isString().stringValue();
                String name = installer.get("name").isString().stringValue();
                String description = installer.get("description").isString().stringValue();
                installernames.put(id, name);
                installerDescriptions.put(id, description);
              }

              showInstallers();
            });
  }

  private void showInstallers() {
    String defaultEnvironmentName = appContext.getWorkspace().getConfig().getDefaultEnv();
    EnvironmentImpl defaultEnvironment =
        appContext.getWorkspace().getConfig().getEnvironments().get(defaultEnvironmentName);

    Map<String, MachineConfigImpl> machines = defaultEnvironment.getMachines();
    for (final String machineName : machines.keySet()) {
      MachineConfigImpl machineConfig = machines.get(machineName);

      for (String installerId : machineConfig.getInstallers()) {
        String installerName = installernames.get(installerId);
        if (installerName == null) {
          installerName = "";
        }

        String installerDescription = installerDescriptions.get(installerId);
        if (installerDescription == null) {
          installerDescription = "";
        }

        view.addInstaller(machineName, installerId, installerName, installerDescription);
      }
    }
  }

  @Override
  public void onInstallerStarting(InstallerStartingEvent event) {
    view.setInstallerStarting(event.getMachineName(), event.getInstaller());
  }

  @Override
  public void onInstallerRunning(InstallerRunningEvent event) {
    view.setInstallerRunning(event.getMachineName(), event.getInstaller());
  }

  @Override
  public void onInstallerFailed(InstallerFailedEvent event) {
  }

  @Override
  public void onWorkspaceStarting(WorkspaceStartingEvent event) {
    view.showWorkspaceStarting();
  }

  @Override
  public void onWorkspaceRunning(WorkspaceRunningEvent event) {
    view.showWorkspaceStarted();

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
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    if (event.isError()) {
      view.showWorkspaceFailed(event.getErrorMessage());
    } else {
      view.showWorkspaceStopped();
    }
  }

  @Override
  public void onMachineStatusChanged(MachineStatusChangedEvent event) {
    switch (event.getStatus()) {
      case STARTING:
        view.setMachineStarting(event.getMachineName());
        break;
      case RUNNING:
        view.setMachineRunning(event.getMachineName());
        break;
      case STOPPED:
        break;
      case FAILED:
        break;
    }
  }
}
