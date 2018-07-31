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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownList;
import org.eclipse.che.ide.ui.dropdown.StringItemRenderer;

/** Implementation of {@link ProcessesListView} that displays processes in a dropdown list. */
@Singleton
public class ProcessesListViewImpl implements ProcessesListView {

  private final Map<Process, BaseListItem<Process>> listItems;
  private final Map<Process, ProcessItemRenderer> renderers;
  private final FlowPanel rootPanel;
  private final DropdownList dropdownList;
  private final CommandResources resources;
  private final EmptyListWidget emptyListWidget;
  private final ToolbarMessages messages;
  private final CreateCommandItem createCommandItem;
  private final CreateCommandItemRenderer createCommandItemRenderer;

  private ActionDelegate delegate;

  private final Label execLabel;
  private final FlowPanel loadAnimation;

  private FlowPanel loadInfo;
  private FlowPanel loadingLabel;
  private FlowPanel loadingProgress;

  @Inject
  public ProcessesListViewImpl(
      CommandResources resources, EmptyListWidget emptyListWidget, ToolbarMessages messages) {
    this.resources = resources;
    this.emptyListWidget = emptyListWidget;
    this.messages = messages;

    listItems = new HashMap<>();
    renderers = new HashMap<>();

    execLabel = new Label("EXEC");
    execLabel.addStyleName(resources.commandToolbarCss().processesListExecLabel());

    loadAnimation = getAnimationWidget();

    dropdownList = new DropdownList(emptyListWidget, true);
    dropdownList.setWidth("100%");
    dropdownList.ensureDebugId("dropdown-processes");
    dropdownList.setSelectionHandler(
        item -> {
          if (item instanceof CreateCommandItem) {
            delegate.onCreateCommand();
          } else {
            listItems
                .entrySet()
                .stream()
                .filter(entry -> item.equals(entry.getValue()))
                .forEach(entry -> delegate.onProcessChosen(entry.getKey()));
          }
        });

    rootPanel = new FlowPanel();
    rootPanel.add(execLabel);
    rootPanel.add(loadAnimation);
    rootPanel.add(dropdownList);

    createCommandItem = new CreateCommandItem();
    createCommandItemRenderer = new CreateCommandItemRenderer();
    checkCreateCommandItem();

    loadInfo = new FlowPanel();
    loadInfo.setStyleName(resources.commandToolbarCss().loaderPanel());
    rootPanel.add(loadInfo);

    loadingLabel = new FlowPanel();
    loadingLabel.setStyleName(resources.commandToolbarCss().loaderPanelLabel());
    loadInfo.add(loadingLabel);

    loadingProgress = new FlowPanel();
    loadingProgress.setStyleName(resources.commandToolbarCss().loaderPanelProgressBar());
    loadInfo.add(loadingProgress);
  }

  /**
   * Creates an animation widget containing three animated vertical bars.
   *
   * @return animation widget
   */
  private FlowPanel getAnimationWidget() {
    FlowPanel animation = new FlowPanel();
    animation.addStyleName(resources.commandToolbarCss().processesListLoader());

    FlowPanel bar1 = new FlowPanel();
    bar1.addStyleName(resources.commandToolbarCss().processesListLoaderBar1());
    animation.add(bar1);

    FlowPanel bar2 = new FlowPanel();
    bar2.addStyleName(resources.commandToolbarCss().processesListLoaderBar2());
    animation.add(bar2);

    FlowPanel bar3 = new FlowPanel();
    bar3.addStyleName(resources.commandToolbarCss().processesListLoaderBar3());
    animation.add(bar3);

    return animation;
  }

  @Override
  public void setLoadMode() {
    execLabel.getElement().getStyle().setDisplay(Style.Display.NONE);
    dropdownList.getElement().getStyle().setDisplay(Style.Display.NONE);

    loadAnimation.getElement().getStyle().setDisplay(Style.Display.BLOCK);
    loadInfo.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
  }

  @Override
  public void setExecMode() {
    execLabel.getElement().getStyle().clearDisplay();
    dropdownList.getElement().getStyle().clearDisplay();

    loadAnimation.getElement().getStyle().clearDisplay();
    loadInfo.getElement().getStyle().clearDisplay();
  }

  @Override
  public void setLoadingProgress(int percents) {
    loadingProgress.getElement().getStyle().setWidth(percents, Style.Unit.PCT);
  }

  @Override
  public void setLoadingMessage(String message) {
    loadingLabel.getElement().setInnerHTML(message);
  }

  /**
   * Ensures that item for creating command added to the empty list or removed from non empty list.
   */
  private void checkCreateCommandItem() {
    if (listItems.isEmpty()) {
      dropdownList.addItem(createCommandItem, createCommandItemRenderer);
    } else {
      dropdownList.removeItem(createCommandItem);
    }
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Widget asWidget() {
    return rootPanel;
  }

  @Override
  public void clearList() {
    dropdownList.clear();

    checkCreateCommandItem();
  }

  @Override
  public void processStopped(Process process) {
    final ProcessItemRenderer renderer = renderers.get(process);

    if (renderer != null) {
      renderer.notifyProcessStopped();
    }
  }

  @Override
  public void addProcess(Process process) {
    final BaseListItem<Process> listItem = new BaseListItem<>(process);
    final ProcessItemRenderer renderer =
        new ProcessItemRenderer(
            listItem, p -> delegate.onStopProcess(p), p -> delegate.onReRunProcess(p));

    listItems.put(process, listItem);
    renderers.put(process, renderer);

    dropdownList.addItem(listItem, renderer);

    checkCreateCommandItem();
  }

  @Override
  public void removeProcess(Process process) {
    final BaseListItem<Process> listItem = listItems.get(process);

    if (listItem != null) {
      listItems.remove(process);
      renderers.remove(process);

      dropdownList.removeItem(listItem);

      checkCreateCommandItem();
    }
  }

  private class CreateCommandItem extends BaseListItem<String> {
    CreateCommandItem() {
      super(messages.guideItemLabel("new"));
    }
  }

  private class CreateCommandItemRenderer extends StringItemRenderer {
    CreateCommandItemRenderer() {
      super(createCommandItem);
    }

    @Override
    public Widget renderHeaderWidget() {
      return emptyListWidget;
    }
  }
}
