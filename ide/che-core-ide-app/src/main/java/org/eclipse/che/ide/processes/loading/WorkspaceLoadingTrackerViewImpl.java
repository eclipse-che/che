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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** View for tracking workspace loading progress. */
public class WorkspaceLoadingTrackerViewImpl extends Composite
    implements WorkspaceLoadingTrackerView {

  interface WorkspaceLoadingTrackerViewImplUiBinder
      extends UiBinder<Widget, WorkspaceLoadingTrackerViewImpl> {}

  @UiField TableSectionElement tableBody;

  /* Anchor for inserting table elements */
  @UiField TableRowElement machinesSectionAnchor;

  /* Templates */
  @UiField TableRowElement machineTemplate;
  @UiField TableRowElement machineInlineDelimiterTemplate;
  @UiField TableRowElement installerTemplate;
  @UiField TableRowElement machinesDelimiterTemplate;

  @UiField TableRowElement waitingWorkspaceSection;

  @UiField TableRowElement workspaceStartedSection;
  @UiField TableRowElement workspaceStartedSectionFooter;

  @UiField TableRowElement workspaceStoppedSection;

  @UiField TableRowElement workspaceFailedSection;
  @UiField TableRowElement workspaceFailedSectionFooter;

  private Set<Element> animatedElements = new HashSet<>();

  private native void log(String msg) /*-{ console.log(msg); }-*/;

  private class Installer {
    // Installer section element
    Element section;

    // Installer name element
    Element name;

    // Installer description element
    Element description;

    // Installer state element
    Element state;

    // Installer status element
    Element status;

    public Installer(String installerName, String installerDescription) {
      // Clone installerTemplate node
      section = installerTemplate.cloneNode(true).cast();

      // Find machine cells
      for (int i = 0; i < section.getChildNodes().getLength(); i++) {
        Node n = section.getChildNodes().getItem(i);
        if (Node.ELEMENT_NODE != n.getNodeType()) {
          continue;
        }

        Element e = n.cast();
        switch (e.getId()) {
          case "installer-name":
            name = e;
            break;
          case "installer-description":
            description = e;
            break;
          case "installer-state":
            state = e;
            break;
          case "installer-status":
            status = e;
            break;
        }
      }

      name.setInnerText(installerName);
      description.setInnerText(installerDescription);
    }

    void setStarting() {
      state.setAttribute("rel", "starting");
      state.setInnerText("Starting");

      status.setAttribute("rel", "starting");
      status.setInnerText("|");

      animatedElements.add(status);
    }

    void setRunning() {
      state.setAttribute("rel", "running");
      state.setInnerText("Running");

      status.setAttribute("rel", "ok");
      status.setInnerText("OK");

      animatedElements.remove(status);
    }

    void setStopped() {
      state.setAttribute("rel", "stopped");
      state.setInnerText("");

      status.setAttribute("rel", "stopped");
      status.setInnerText("");
    }
  }

  private class Machine {
    // Machine section element
    Element section;

    // Machine icon element
    Element icon;

    // Machine title element
    Element title;

    // Machine image element
    // `image [eclipse/mysql]`
    Element image;

    // Machine state element
    // content: STARTING, RUNNING, STOPPED
    // rel: starting, running, stopped
    Element state;

    // Inline delimiter element
    Element inlineDelimiter;

    // Machine installers
    Map<String, Installer> installers;

    // Machines delimiter element;
    Element machinesDelimiter;

    /**
     * Creates machine section, inline delimiter and machines delimiter
     *
     * @param machineName machine name
     */
    public Machine(String machineName) {
      installers = new HashMap<>();

      // Clone machineTemplate node
      section = machineTemplate.cloneNode(true).cast();

      // Find machine cells
      for (int i = 0; i < section.getChildNodes().getLength(); i++) {
        Node n = section.getChildNodes().getItem(i);
        if (Node.ELEMENT_NODE != n.getNodeType()) {
          continue;
        }

        Element e = n.cast();
        switch (e.getId()) {
          case "machine-icon":
            icon = e;
            break;
          case "machine-title":
            title = e;
            break;
          case "machine-image":
            image = e;
            break;
          case "machine-state":
            state = e;
            break;
        }
      }

      // Set title
      title.setInnerText(machineName);

      // Clone inline delimiter
      inlineDelimiter = machineInlineDelimiterTemplate.cloneNode(true).cast();

      // Clone machines delimiter
      machinesDelimiter = machinesDelimiterTemplate.cloneNode(true).cast();
    }

    /**
     * Sets image name
     *
     * @param imageName image name
     */
    void setImageName(String imageName) {
      image.setInnerText("image [" + imageName + "]");
    }

    /**
     * Set machine state. Possible values: starting, running, stopped.
     *
     * @param newState new state
     */
    void setState(String newState) {
      state.setAttribute("rel", newState.toLowerCase());
      state.setInnerText(newState.toUpperCase());
    }

    /**
     * Creates and adds an installer.
     *
     * @param installerId installer id
     * @param installerName installer name
     * @param installerDescription installer description
     */
    void addInstaller(String installerId, String installerName, String installerDescription) {
      if (installers.containsKey(installerId)) {
        return;
      }

      Installer installer = new Installer(installerName, installerDescription);

      installers.put(installerId, installer);

      tableBody.insertBefore(installer.section, machinesDelimiter);

      // Update `rowspan` attribute of machine icon cell
      icon.setAttribute("rowspan", "" + (2 + installers.size()));
    }
  }

  private Map<String, Machine> machines = new HashMap<>();

  @Inject
  public WorkspaceLoadingTrackerViewImpl(WorkspaceLoadingTrackerViewImplUiBinder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    // Remove templates from the table
    machineTemplate.removeFromParent();
    machineInlineDelimiterTemplate.removeFromParent();
    installerTemplate.removeFromParent();
    machinesDelimiterTemplate.removeFromParent();

    animationTimer.scheduleRepeating(200);
  }

  @Override
  public void addMachine(String machineName) {
    // create machine
    Machine machine = new Machine(machineName);

    // remember it
    machines.put(machineName, machine);

    // insert machine section
    tableBody.insertBefore(machine.section, machinesSectionAnchor);
    tableBody.insertBefore(machine.inlineDelimiter, machinesSectionAnchor);
    tableBody.insertBefore(machine.machinesDelimiter, machinesSectionAnchor);
  }

  @Override
  public void setMachineImageName(String machineName, String imageName) {
    Machine machine = machines.get(machineName);
    if (machine != null) {
      machine.setImageName(imageName);
    }
  }

  @Override
  public void setMachineStarting(String machineName) {
    Machine machine = machines.get(machineName);
    if (machine != null) {
      machine.setState("starting");
    }
  }

  @Override
  public void setMachineRunning(String machineName) {
    Machine machine = machines.get(machineName);
    if (machine != null) {
      machine.setState("running");
    }
  }

  @Override
  public void addInstaller(
      String machineName, String installerId, String installerName, String installerDescription) {

    Machine machine = machines.get(machineName);
    if (machine != null) {
      machine.addInstaller(installerId, installerName, installerDescription);
    }
  }

  @Override
  public void setInstallerStarting(String machineName, String installerId) {
    Machine machine = machines.get(machineName);
    if (machine != null) {
      Installer installer = machine.installers.get(installerId);
      if (installer != null) {
        installer.setStarting();
      }
    }
  }

  @Override
  public void setInstallerRunning(String machineName, String installerId) {
    Machine machine = machines.get(machineName);
    if (machine != null) {
      Installer installer = machine.installers.get(installerId);
      if (installer != null) {
        installer.setRunning();
      }
    }
  }

  @Override
  public void showWorkspaceStarting() {
    waitingWorkspaceSection.getStyle().clearDisplay();

    workspaceStartedSection.getStyle().setDisplay(Style.Display.NONE);
    workspaceStartedSectionFooter.getStyle().setDisplay(Style.Display.NONE);

    workspaceStoppedSection.getStyle().setDisplay(Style.Display.NONE);

    workspaceFailedSection.getStyle().setDisplay(Style.Display.NONE);
    workspaceFailedSectionFooter.getStyle().setDisplay(Style.Display.NONE);
  }

  @Override
  public void showWorkspaceStarted() {
    waitingWorkspaceSection.getStyle().setDisplay(Style.Display.NONE);

    workspaceStartedSection.getStyle().clearDisplay();
    workspaceStartedSectionFooter.getStyle().clearDisplay();

    workspaceStoppedSection.getStyle().setDisplay(Style.Display.NONE);

    workspaceFailedSection.getStyle().setDisplay(Style.Display.NONE);
    workspaceFailedSectionFooter.getStyle().setDisplay(Style.Display.NONE);
  }

  @Override
  public void showWorkspaceStopped() {
    waitingWorkspaceSection.getStyle().setDisplay(Style.Display.NONE);

    workspaceStartedSection.getStyle().setDisplay(Style.Display.NONE);
    workspaceStartedSectionFooter.getStyle().setDisplay(Style.Display.NONE);

    workspaceStoppedSection.getStyle().clearDisplay();

    for (Machine machine : machines.values()) {
      machine.setState("stopped");

      for (Installer installer : machine.installers.values()) {
        installer.setStopped();
      }
    }

    workspaceFailedSection.getStyle().setDisplay(Style.Display.NONE);
    workspaceFailedSectionFooter.getStyle().setDisplay(Style.Display.NONE);
  }

  @Override
  public void showWorkspaceFailed(String error) {
    waitingWorkspaceSection.getStyle().setDisplay(Style.Display.NONE);

    workspaceStartedSection.getStyle().setDisplay(Style.Display.NONE);
    workspaceStartedSectionFooter.getStyle().setDisplay(Style.Display.NONE);

    workspaceStoppedSection.getStyle().setDisplay(Style.Display.NONE);

    workspaceFailedSection.getStyle().clearDisplay();
    workspaceFailedSectionFooter.getStyle().clearDisplay();
  }

  private Timer animationTimer =
      new Timer() {
        @Override
        public void run() {
          for (Element e : animatedElements) {
            String text = e.getInnerHTML();
            if ("".equals(text.trim())) {
              text = "    ";
            }

            switch (text) {
              case "    ":
                e.setInnerHTML("/   ");
                break;
              case "/   ":
                e.setInnerHTML("//  ");
                break;
              case "//  ":
                e.setInnerHTML("/// ");
                break;
              case "/// ":
                e.setInnerHTML("////");
                break;
              case "////":
                e.setInnerHTML(" ///");
                break;
              case " ///":
                e.setInnerHTML("  //");
                break;
              case "  //":
                e.setInnerHTML("   /");
                break;
              case "   /":
                e.setInnerHTML("    ");
                break;

              case "/":
                e.setInnerHTML("-");
                break;
              case "-":
                e.setInnerHTML("\\");
                break;
              case "\\":
                e.setInnerHTML("|");
                break;
              case "|":
                e.setInnerHTML("/");
                break;
            }
          }
        }
      };
}
