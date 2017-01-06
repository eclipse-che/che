/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.debugger.ide.debug;

import elemental.dom.Element;
import elemental.events.KeyboardEvent;
import elemental.events.MouseEvent;
import elemental.html.TableCellElement;
import elemental.html.TableElement;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.MutableVariableImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.debug.Breakpoint;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.SignalEvent;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.DebuggerResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The class business logic which allow us to change visual representation of debugger panel.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
@Singleton
public class DebuggerViewImpl extends BaseView<DebuggerView.ActionDelegate> implements DebuggerView {

    interface DebuggerViewImplUiBinder extends UiBinder<Widget, DebuggerViewImpl> {
    }

    @UiField
    Label                        vmName;
    @UiField
    Label                        executionPoint;
    @UiField
    SimplePanel                  toolbarPanel;
    @UiField
    ScrollPanel                  variablesPanel;
    @UiField
    ScrollPanel                  breakpointsPanel;
    @UiField(provided = true)
    DebuggerLocalizationConstant locale;
    @UiField(provided = true)
    Resources                    coreRes;
    @UiField(provided = true)
    SplitLayoutPanel splitPanel = new SplitLayoutPanel(3);

    private final Tree<MutableVariable> variables;
    private final SimpleList<Breakpoint> breakpoints;
    private final DebuggerResources      res;

    private TreeNodeElement<MutableVariable> selectedVariable;

    @Inject
    protected DebuggerViewImpl(PartStackUIResources partStackUIResources,
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

        TableElement breakPointsElement = Elements.createTableElement();
        breakPointsElement.setAttribute("style", "width: 100%");
        SimpleList.ListEventDelegate<Breakpoint> breakpointListEventDelegate = new SimpleList.ListEventDelegate<Breakpoint>() {
            public void onListItemClicked(Element itemElement, Breakpoint itemData) {
                breakpoints.getSelectionModel().setSelectedItem(itemData);
            }

            public void onListItemDoubleClicked(Element listItemBase, Breakpoint itemData) {
                // TODO: implement 'go to breakpoint source' feature
            }
        };

        SimpleList.ListItemRenderer<Breakpoint> breakpointListItemRenderer = new
                SimpleList.ListItemRenderer<Breakpoint>() {
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
                        sb.appendEscaped(path.substring(path.lastIndexOf("/") + 1)
                                         + " - [line: "
                                         + String.valueOf(itemData.getLineNumber() + 1)
                                         + "]");
                        sb.appendHtmlConstant("</td></tr></table>");

                        label.setInnerHTML(sb.toSafeHtml().asString());

                        itemElement.appendChild(label);
                    }

                    @Override
                    public Element createElement() {
                        return Elements.createTRElement();
                    }
                };

        breakpoints = SimpleList.create((SimpleList.View)breakPointsElement,
                                        coreRes.defaultSimpleListCss(),
                                        breakpointListItemRenderer,
                                        breakpointListEventDelegate);
        this.breakpointsPanel.add(breakpoints);
        this.variables = Tree.create(rendererResources, new VariableNodeDataAdapter(), new VariableTreeNodeRenderer(rendererResources));
        this.variables.setTreeEventHandler(new Tree.Listener<MutableVariable>() {
            @Override
            public void onNodeAction(@NotNull TreeNodeElement<MutableVariable> node) {
            }

            @Override
            public void onNodeClosed(@NotNull TreeNodeElement<MutableVariable> node) {
                selectedVariable = null;
            }

            @Override
            public void onNodeContextMenu(int mouseX, int mouseY, @NotNull TreeNodeElement<MutableVariable> node) {
            }

            @Override
            public void onNodeDragStart(@NotNull TreeNodeElement<MutableVariable> node, @NotNull MouseEvent event) {
            }

            @Override
            public void onNodeDragDrop(@NotNull TreeNodeElement<MutableVariable> node, @NotNull MouseEvent event) {
            }

            @Override
            public void onNodeExpanded(@NotNull final TreeNodeElement<MutableVariable> node) {
                selectedVariable = node;
                delegate.onSelectedVariableElement(selectedVariable.getData());
                delegate.onExpandVariablesTree();
            }

            @Override
            public void onNodeSelected(@NotNull TreeNodeElement<MutableVariable> node, @NotNull SignalEvent event) {
                selectedVariable = node;
                delegate.onSelectedVariableElement(selectedVariable.getData());
            }

            @Override
            public void onRootContextMenu(int mouseX, int mouseY) {
            }

            @Override
            public void onRootDragDrop(@NotNull MouseEvent event) {
            }

            @Override
            public void onKeyboard(@NotNull KeyboardEvent event) {
            }
        });

        this.variablesPanel.add(variables);
        minimizeButton.ensureDebugId("debugger-minimizeBut");
    }

    /** {@inheritDoc} */
    @Override
    public void setExecutionPoint(@Nullable Location location) {
        StringBuilder labelText = new StringBuilder();
        if (location != null) {
            labelText.append("{").append(location.getTarget()).append(":").append(location.getLineNumber()).append("} ");
        }
        executionPoint.getElement().setClassName(coreRes.coreCss().defaultFont());
        executionPoint.setText(labelText.toString());
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void setBreakpoints(@NotNull List<Breakpoint> breakpoints) {
        this.breakpoints.render(breakpoints);
    }

    /** {@inheritDoc} */
    @Override
    public void setVMName(@NotNull String name) {
        vmName.setText(name);
    }

    /** {@inheritDoc} */
    @Override
    public void updateSelectedVariable() {
        variables.closeNode(selectedVariable);
        variables.expandNode(selectedVariable);
    }

    /** {@inheritDoc} */
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
}
