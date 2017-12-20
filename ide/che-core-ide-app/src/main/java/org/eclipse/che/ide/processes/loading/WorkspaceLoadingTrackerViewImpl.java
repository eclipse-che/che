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
import com.google.gwt.dom.client.PreElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** View for tracking workspace loading progress. */
public class WorkspaceLoadingTrackerViewImpl extends Composite
    implements WorkspaceLoadingTrackerView {

  interface WorkspaceLoadingTrackerViewImplUiBinder
      extends UiBinder<Widget, WorkspaceLoadingTrackerViewImpl> {}

  private static final int MACHINE_NAME_WIDTH = 25;
  private static final int IMAGE_NAME_WIDTH = 35;
  private static final int PROGRESS_WIDTH = 33;

  private static final String LOADING_CHAR1 = "&blk14;";
  private static final String LOADING_CHAR2 = "&blk34;";

  private static final String ANIMATION_START = "    ";

  @UiField PreElement waitingWorkspaceTitle;

  @UiField PreElement preparingWorkspaceRuntime;

  @UiField PreElement preparingWorkspaceRuntimeItems;

  private Node preparingWorkspaceRuntimeOriginalItem;
  private Node preparingWorkspaceRuntimeOriginalNewLine;

  @UiField PreElement startingWorkspaceRuntimes;

  @UiField PreElement startingWorkspaceRuntimesItems;

  private Node startingWorkspaceRuntimesOriginalItem;
  private Node startingWorkspaceRuntimesOriginalNewLine;

  @UiField PreElement initializingWorkspaceAgents;

  @UiField PreElement workspaceStarted;

  private Map<String, Node> step1Nodes = new LinkedHashMap<>();
  private Map<String, Node> step2Nodes = new LinkedHashMap<>();

  private List<Element> animatedElements = new ArrayList<>();

  @Inject
  public WorkspaceLoadingTrackerViewImpl(WorkspaceLoadingTrackerViewImplUiBinder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));

    // initialize element templates
    preparingWorkspaceRuntimeOriginalItem =
        preparingWorkspaceRuntimeItems.getChildNodes().getItem(0);
    preparingWorkspaceRuntimeOriginalNewLine =
        preparingWorkspaceRuntimeItems.getChildNodes().getItem(1);
    preparingWorkspaceRuntimeItems.removeChild(preparingWorkspaceRuntimeOriginalItem);
    preparingWorkspaceRuntimeItems.removeChild(preparingWorkspaceRuntimeOriginalNewLine);

    startingWorkspaceRuntimesOriginalItem =
        startingWorkspaceRuntimesItems.getChildNodes().getItem(0);
    startingWorkspaceRuntimesOriginalNewLine =
        startingWorkspaceRuntimesItems.getChildNodes().getItem(1);
    startingWorkspaceRuntimesItems.removeChild(startingWorkspaceRuntimesOriginalItem);
    startingWorkspaceRuntimesItems.removeChild(startingWorkspaceRuntimesOriginalNewLine);

    animationTimer.scheduleRepeating(200);
  }

  @Override
  public void startLoading() {
    waitingWorkspaceTitle.getStyle().clearDisplay();
    preparingWorkspaceRuntime.getStyle().clearDisplay();
  }

  private Timer animationTimer =
      new Timer() {
        @Override
        public void run() {
          for (Element e : animatedElements) {
            String text = e.getInnerText();
            switch (text) {
              case "    ":
                e.setInnerText("/   ");
                break;
              case "/   ":
                e.setInnerText("//  ");
                break;
              case "//  ":
                e.setInnerText("/// ");
                break;
              case "/// ":
                e.setInnerText("////");
                break;
              case "////":
                e.setInnerText(" ///");
                break;
              case " ///":
                e.setInnerText("  //");
                break;
              case "  //":
                e.setInnerText("   /");
                break;
              case "   /":
                e.setInnerText("    ");
                break;

              case "/":
                e.setInnerText("-");
                break;
              case "-":
                e.setInnerText("\\");
                break;
              case "\\":
                e.setInnerText("|");
                break;
              case "|":
                e.setInnerText("/");
                break;
            }
          }
        }
      };

  @Override
  public void pullMachine(String machine) {
    // add machine node to pull
    Node cloned = preparingWorkspaceRuntimeOriginalItem.cloneNode(true);
    step1Nodes.put(machine, cloned);

    Node clonedRet = preparingWorkspaceRuntimeOriginalNewLine.cloneNode(true);

    preparingWorkspaceRuntimeItems.appendChild(cloned);
    preparingWorkspaceRuntimeItems.appendChild(clonedRet);

    for (int i = 0; i < cloned.getChildNodes().getLength(); i++) {
      Node n = cloned.getChildNodes().getItem(i);
      if (Node.ELEMENT_NODE != n.getNodeType()) {
        continue;
      }

      Element e = n.cast();
      if ("machine-name".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString(machine, MACHINE_NAME_WIDTH));
        continue;
      }

      if ("image-name".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString("...", IMAGE_NAME_WIDTH));
        continue;
      }

      if ("progress".equals(e.getAttribute("rel"))) {
        e.setInnerHTML(expandString("Preparing...", PROGRESS_WIDTH));
        continue;
      }

      if ("progress-value".equals(e.getAttribute("rel"))) {
        // must be animated
        e.setInnerText(ANIMATION_START);
        animatedElements.add(e);
        continue;
      }
    }
  }

  @Override
  public void setMachineImage(String machine, String image) {
    Node cloned = step1Nodes.get(machine);
    if (cloned == null) {
      return;
    }

    for (int i = 0; i < cloned.getChildNodes().getLength(); i++) {
      Node n = cloned.getChildNodes().getItem(i);
      if (Node.ELEMENT_NODE != n.getNodeType()) {
        continue;
      }

      Element e = n.cast();

      if ("image-name".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString("[" + image + "]", IMAGE_NAME_WIDTH));
        continue;
      }
    }
  }

  private String getLoadingProgressBar(int percents) {
    int maxChars = 25;

    int chars = maxChars * percents / 100;

    String result = "";

    for (int i = 0; i < maxChars; i++) {
      result += i < chars ? LOADING_CHAR2 : LOADING_CHAR1;
    }

    return result;
  }

  @Override
  public void onPullingProgress(String machine, int percents) {
    Node cloned = step1Nodes.get(machine);
    if (cloned == null) {
      return;
    }

    for (int i = 0; i < cloned.getChildNodes().getLength(); i++) {
      Node n = cloned.getChildNodes().getItem(i);
      if (Node.ELEMENT_NODE != n.getNodeType()) {
        continue;
      }

      Element e = n.cast();

      if ("progress".equals(e.getAttribute("rel"))) {
        e.setInnerHTML("Pulling " + getLoadingProgressBar(percents));
        continue;
      }

      if ("progress-value".equals(e.getAttribute("rel"))) {
        e.setInnerText(percents + "%");
        continue;
      }
    }
  }

  @Override
  public void onPullingComplete(String machine) {
    Node cloned = step1Nodes.get(machine);
    if (cloned == null) {
      return;
    }

    for (int i = 0; i < cloned.getChildNodes().getLength(); i++) {
      Node n = cloned.getChildNodes().getItem(i);
      if (Node.ELEMENT_NODE != n.getNodeType()) {
        continue;
      }

      Element e = n.cast();

      if ("progress".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString("Retrieved", PROGRESS_WIDTH));
        continue;
      }

      if ("progress-value".equals(e.getAttribute("rel"))) {
        e.setInnerText("OK");
        e.setAttribute("rel", "done");
        continue;
      }
    }
  }

  private String expandString(String source, int length) {
    String result = source;

    if (result.length() > length) {
      return result.substring(0, length - 3) + "...";
    }

    while (result.length() < length) {
      result += " ";
    }

    return result;
  }

  @Override
  public void startWorkspaceMachines() {
    startingWorkspaceRuntimes.getStyle().clearDisplay();
  }

  @Override
  public void startWorkspaceMachine(String machine, String image) {
    // add machine node to pull
    Node cloned = startingWorkspaceRuntimesOriginalItem.cloneNode(true);
    step2Nodes.put(machine, cloned);

    Node clonedRet = startingWorkspaceRuntimesOriginalNewLine.cloneNode(true);

    startingWorkspaceRuntimesItems.appendChild(cloned);
    startingWorkspaceRuntimesItems.appendChild(clonedRet);

    for (int i = 0; i < cloned.getChildNodes().getLength(); i++) {
      Node n = cloned.getChildNodes().getItem(i);
      if (Node.ELEMENT_NODE != n.getNodeType()) {
        continue;
      }

      Element e = n.cast();
      if ("machine-name".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString(machine, MACHINE_NAME_WIDTH));
        continue;
      }

      if ("image-name".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString("[" + image + "]", IMAGE_NAME_WIDTH));
        continue;
      }

      if ("progress".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString("Starting...", PROGRESS_WIDTH));
        continue;
      }

      if ("progress-value".equals(e.getAttribute("rel"))) {
        // must be animated
        e.setInnerText(ANIMATION_START);
        animatedElements.add(e);
        continue;
      }
    }
  }

  @Override
  public void onMachineRunning(String machine) {
    Node cloned = step2Nodes.get(machine);
    if (cloned == null) {
      return;
    }

    for (int i = 0; i < cloned.getChildNodes().getLength(); i++) {
      Node n = cloned.getChildNodes().getItem(i);
      if (Node.ELEMENT_NODE != n.getNodeType()) {
        continue;
      }

      Element e = n.cast();

      if ("progress".equals(e.getAttribute("rel"))) {
        e.setInnerText(expandString("Running", PROGRESS_WIDTH));
        continue;
      }

      if ("progress-value".equals(e.getAttribute("rel"))) {
        e.setInnerText("OK");
        e.setAttribute("rel", "done");
        continue;
      }
    }
  }

  @Override
  public void showInitializingWorkspaceAgents() {
    initializingWorkspaceAgents.getStyle().clearDisplay();
  }

  @Override
  public void onWorkspaceStarted() {
    workspaceStarted.getStyle().clearDisplay();

    animatedElements.clear();
    animationTimer.cancel();
  }
}
