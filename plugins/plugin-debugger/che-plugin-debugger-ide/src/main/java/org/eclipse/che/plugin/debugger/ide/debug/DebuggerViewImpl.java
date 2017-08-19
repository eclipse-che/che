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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.TableCellElement;
import elemental.html.TableElement;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.MutableVariableImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The class business logic which allow us to change visual representation of debugger panel.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
@Singleton
public class DebuggerViewImpl extends BaseView<DebuggerView.ActionDelegate>
    implements DebuggerView {

  interface DebuggerViewImplUiBinder extends UiBinder<Widget, DebuggerViewImpl> {}

  @UiField Label vmName;
  @UiField Label executionPoint;
  @UiField SimplePanel toolbarPanel;
  @UiField ScrollPanel variablesPanel;
  @UiField ScrollPanel breakpointsPanel;

  @UiField(provided = true)
  DebuggerLocalizationConstant locale;

  @UiField(provided = true)
  Resources coreRes;

  @UiField(provided = true)
  SplitLayoutPanel splitPanel = new SplitLayoutPanel(3);

  @UiField ListBox threads;
  @UiField ScrollPanel framesPanel;

  private final Tree<MutableVariable> variables;
  private final SimpleList<Breakpoint> breakpoints;
  private final SimpleList<StackFrameDump> frames;
  private final DebuggerResources res;

  private TreeNodeElement<MutableVariable> selectedVariable;

  @Inject
  protected DebuggerViewImpl(
      PartStackUIResources partStackUIResources,
      DebuggerResources resources,
      DebuggerLocalizationConstant locale,
      Resources coreRes,
      VariableTreeNodeRenderer.Resources rendererResources,
      DebuggerViewImplUiBinder uiBinder) {
    super(partStackUIResources);

    this.locale = locale;
    this.res = resources;
    this.coreRes = coreRes;

    setContentWidget(uiBinder.createAndBindUi(this));

    this.breakpoints = createBreakpointList();
    this.breakpointsPanel.add(breakpoints);

    this.frames = createFramesList();
    this.framesPanel.add(frames);

    this.variables =
        Tree.create(
            rendererResources,
            new VariableNodeDataAdapter(),
            new VariableTreeNodeRenderer(rendererResources));
    this.variables.setTreeEventHandler(
        new Tree.Listener<MutableVariable>() {
          @Override
          public void onNodeAction(@NotNull TreeNodeElement<MutableVariable> node) {}

          @Override
          public void onNodeClosed(@NotNull TreeNodeElement<MutableVariable> node) {
            selectedVariable = null;
          }

          @Override
          public void onNodeContextMenu(
              int mouseX, int mouseY, @NotNull TreeNodeElement<MutableVariable> node) {}

          @Override
          public void onNodeDragStart(
              @NotNull TreeNodeElement<MutableVariable> node, @NotNull MouseEvent event) {}

          @Override
          public void onNodeDragDrop(
              @NotNull TreeNodeElement<MutableVariable> node, @NotNull MouseEvent event) {}

          @Override
          public void onNodeExpanded(@NotNull final TreeNodeElement<MutableVariable> node) {
            selectedVariable = node;
            delegate.onSelectedVariableElement(selectedVariable.getData());
            delegate.onExpandVariablesTree();
          }

          @Override
          public void onNodeSelected(
              @NotNull TreeNodeElement<MutableVariable> node, @NotNull SignalEvent event) {
            selectedVariable = node;
            delegate.onSelectedVariableElement(selectedVariable.getData());
          }

          @Override
          public void onRootContextMenu(int mouseX, int mouseY) {}

          @Override
          public void onRootDragDrop(@NotNull MouseEvent event) {}

          @Override
          public void onKeyboard(@NotNull KeyboardEvent event) {}
        });

    this.variablesPanel.add(variables);
    minimizeButton.ensureDebugId("debugger-minimizeBut");
  }

  private SimpleList<Breakpoint> createBreakpointList() {
    TableElement breakPointsElement = Elements.createTableElement();
    breakPointsElement.setAttribute("style", "width: 100%");
    SimpleList.ListEventDelegate<Breakpoint> breakpointListEventDelegate =
        new SimpleList.ListEventDelegate<Breakpoint>() {
          public void onListItemClicked(Element itemElement, Breakpoint itemData) {
            breakpoints.getSelectionModel().setSelectedItem(itemData);
          }

          public void onListItemDoubleClicked(Element listItemBase, Breakpoint itemData) {}
        };

    SimpleList.ListItemRenderer<Breakpoint> breakpointListItemRenderer =
        new SimpleList.ListItemRenderer<Breakpoint>() {
          @Override
          public void render(Element itemElement, Breakpoint itemData) {
            TableCellElement label = Elements.createTDElement();

            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            // Add icon
            sb.appendHtmlConstant("<table><tr><td>");
            SVGResource icon = res.breakpoint();
            if (icon != null) {
              sb.appendHtmlConstant("<img src=\"" + icon.getSafeUri().asString() + "\">");
            }
            sb.appendHtmlConstant("</td>");

            // Add title
            sb.appendHtmlConstant("<td>");

            String path = itemData.getPath();
            sb.appendEscaped(
                path.substring(path.lastIndexOf("/") + 1)
                    + ":"
                    + String.valueOf(itemData.getLineNumber() + 1));
            sb.appendHtmlConstant("</td></tr></table>");

            label.setInnerHTML(sb.toSafeHtml().asString());

            itemElement.appendChild(label);
          }

          @Override
          public Element createElement() {
            return Elements.createTRElement();
          }
        };

    return SimpleList.create(
        (SimpleList.View) breakPointsElement,
        coreRes.defaultSimpleListCss(),
        breakpointListItemRenderer,
        breakpointListEventDelegate);
  }

  private SimpleList<StackFrameDump> createFramesList() {
    TableElement frameElement = Elements.createTableElement();
    frameElement.setAttribute("style", "width: 100%");

    SimpleList.ListEventDelegate<StackFrameDump> frameListEventDelegate =
        new SimpleList.ListEventDelegate<StackFrameDump>() {
          public void onListItemClicked(Element itemElement, StackFrameDump itemData) {
            frames.getSelectionModel().setSelectedItem(itemData);
            delegate.onSelectedFrame(frames.getSelectionModel().getSelectedIndex());
          }

          public void onListItemDoubleClicked(Element listItemBase, StackFrameDump itemData) {
            delegate.onSelectedFrame(frames.getSelectionModel().getSelectedIndex());
          }
        };

    SimpleList.ListItemRenderer<StackFrameDump> frameListItemRenderer =
        new SimpleList.ListItemRenderer<StackFrameDump>() {
          @Override
          public void render(Element itemElement, StackFrameDump itemData) {
            TableCellElement label = Elements.createTDElement();

            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendEscaped(itemData.getLocation().getMethod().getName());
            sb.appendEscaped("(");

            List<? extends Variable> arguments = itemData.getLocation().getMethod().getArguments();
            for (int i = 0; i < arguments.size(); i++) {
              String[] classTypeEntries = arguments.get(i).getType().split("\\.");
              sb.appendEscaped(classTypeEntries[classTypeEntries.length - 1]);

              if (i != arguments.size() - 1) {
                sb.appendEscaped(", ");
              }
            }

            sb.appendEscaped("):");
            sb.append(itemData.getLocation().getLineNumber());
            sb.appendEscaped(", ");

            String classFqn = itemData.getLocation().getTarget();
            int classNameIndex = classFqn.lastIndexOf(".");
            String className = classFqn.substring(classNameIndex + 1);
            String packageName = classFqn.substring(0, classNameIndex);

            sb.appendEscaped(className);
            sb.appendEscaped(" (");
            sb.appendEscaped(packageName);
            sb.appendEscaped(") ");

            label.setInnerHTML(sb.toSafeHtml().asString());
            itemElement.appendChild(label);
          }

          @Override
          public Element createElement() {
            return Elements.createTRElement();
          }
        };

    return SimpleList.create(
        (SimpleList.View) frameElement,
        coreRes.defaultSimpleListCss(),
        frameListItemRenderer,
        frameListEventDelegate);
  }

  @Override
  public void setExecutionPoint(@Nullable Location location) {
    StringBuilder labelText = new StringBuilder();
    if (location != null) {
      labelText
          .append("{")
          .append(location.getTarget())
          .append(":")
          .append(location.getLineNumber())
          .append("} ");
    }
    executionPoint.getElement().setClassName(coreRes.coreCss().defaultFont());
    executionPoint.setText(labelText.toString());
  }

  @Override
  public void setVariables(@NotNull List<? extends Variable> variables) {
    MutableVariable root = this.variables.getModel().getRoot();
    if (root == null) {
      root = new MutableVariableImpl();
      this.variables.getModel().setRoot(root);
    }
    root.setVariables(variables);
    this.variables.renderTree(0);
  }

  @Override
  public void setBreakpoints(@NotNull List<Breakpoint> breakpoints) {
    this.breakpoints.render(breakpoints);
  }

  @Override
  public void setThreads(List<? extends ThreadDump> threadDumps, long activeThreadId) {
    threads.clear();

    for (int i = 0; i < threadDumps.size(); i++) {
      ThreadDump td = threadDumps.get(i);
      String item =
          "\""
              + td.getName()
              + "\"@"
              + td.getId()
              + " in group \""
              + td.getGroupName()
              + "\": "
              + td.getStatus();
      threads.addItem(item, String.valueOf(td.getId()));
      if (td.getId() == activeThreadId) {
        threads.setSelectedIndex(i);
      }
    }
  }

  @Override
  public void setFrames(List<? extends StackFrameDump> stackFrameDumps) {
    frames.render(new ArrayList<>(stackFrameDumps));
    if (!stackFrameDumps.isEmpty()) {
      frames.getSelectionModel().setSelectedItem(0);
    }
  }

  @Override
  public void setVMName(@Nullable String name) {
    vmName.setText(name == null ? "" : name);
  }

  @Override
  public void updateSelectedVariable() {
    variables.closeNode(selectedVariable);
    variables.expandNode(selectedVariable);
  }

  @Override
  public void setVariablesIntoSelectedVariable(@NotNull List<? extends Variable> variables) {
    MutableVariable rootVariable = selectedVariable.getData();
    rootVariable.setVariables(variables);
  }

  @Override
  public MutableVariable getSelectedDebuggerVariable() {
    if (selectedVariable != null) {
      return selectedVariable.getData();
    }
    return null;
  }

  @Override
  public AcceptsOneWidget getDebuggerToolbarPanel() {
    return toolbarPanel;
  }

  @UiHandler({"threads"})
  void onThreadChanged(ChangeEvent event) {
    delegate.onSelectedThread(Integer.parseInt(threads.getSelectedValue()));
  }
}
