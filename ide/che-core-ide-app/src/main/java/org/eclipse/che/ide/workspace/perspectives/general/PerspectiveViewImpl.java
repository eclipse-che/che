/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.workspace.perspectives.general;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.parts.PerspectiveView;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.workspace.WorkBenchResources;

/**
 * General-purpose Perspective View
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
public class PerspectiveViewImpl extends LayoutPanel implements PerspectiveView<PerspectiveView.ActionDelegate> {

    interface PerspectiveViewImplUiBinder extends UiBinder<Widget, PerspectiveViewImpl> {
    }

    private static final PerspectiveViewImplUiBinder UI_BINDER = GWT.create(PerspectiveViewImplUiBinder.class);

    @UiField(provided = true)
    SplitLayoutPanel splitPanel = new SplitLayoutPanel(1);

    @UiField
    ScrollPanel editorPanel;
    @UiField
    SimplePanel navPanel;
    @UiField
    SimplePanel infoPanel;

    @UiField
    SimplePanel toolPanel;
    @UiField
    FlowPanel   rightPanelContainer;
    @UiField
    FlowPanel   leftPanelContainer;
    @UiField
    FlowPanel   bottomPanelContainer;

    private FlowPanel rightPanel;
    private FlowPanel leftPanel;
    private FlowPanel bottomPanel;

    @UiField(provided = true)
    final WorkBenchResources resources;

    private ActionDelegate delegate;

    @Inject
    public PerspectiveViewImpl(WorkBenchResources resources) {
        this.resources = resources;
        resources.workBenchCss().ensureInjected();
        add(UI_BINDER.createAndBindUi(this));

        rightPanel = new FlowPanel();
        rightPanelContainer.add(rightPanel);
        rightPanel.addStyleName(resources.workBenchCss().ideWorkBenchToolPanelRight());

        leftPanel = new FlowPanel();
        leftPanelContainer.add(leftPanel);
        leftPanel.addStyleName(resources.workBenchCss().ideWorkBenchToolPanelLeft());

        bottomPanel = new FlowPanel();
        bottomPanelContainer.add(bottomPanel);
        bottomPanel.addStyleName(resources.workBenchCss().ideWorkBenchToolPanelBottom());
         /* Makes splitters much better */
        tuneSplitters();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public SimplePanel getEditorPanel() {
        return editorPanel;
    }

    /** {@inheritDoc} */
    @Override
    public SimplePanel getNavigationPanel() {
        return navPanel;
    }

    /** {@inheritDoc} */
    @Override
    public SimplePanel getInformationPanel() {
        return infoPanel;
    }

    /** {@inheritDoc} */
    @Override
    public SimplePanel getToolPanel() {
        return toolPanel;
    }

    /** Returns split panel. */
    public SplitLayoutPanel getSplitPanel() {
        return splitPanel;
    }

    /**
     * Makes splitter better.
     */
    public void tuneSplitters() {
        NodeList<Node> nodes = splitPanel.getElement().getChildNodes();
        boolean firstFound = false;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            if (node.hasChildNodes()) {
                com.google.gwt.dom.client.Element el = node.getFirstChild().cast();
                if ("gwt-SplitLayoutPanel-HDragger".equals(el.getClassName())) {
                    if (!firstFound) {
                        firstFound = true;
                        tuneLeftSplitter(el);
                    } else {
                        tuneRightSplitter(el);
                    }
                } else if ("gwt-SplitLayoutPanel-VDragger".equals(el.getClassName())) {
                    tuneBottomSplitter(el);
                }
            }
        }
    }

    /**
     * Tunes left splitter. Makes it wider and adds double border to seem rich.
     *
     * @param el
     *         element to tune
     */
    private void tuneLeftSplitter(Element el) {
        /** Add Z-Index to move the splitter on the top and make content visible */
        el.getParentElement().getStyle().setProperty("zIndex", "1000");
        el.getParentElement().getStyle().setProperty("overflow", "visible");

        /** Tune splitter catch panel */
        el.getStyle().setProperty("boxSizing", "border-box");
        el.getStyle().setProperty("width", "5px");
        el.getStyle().setProperty("overflow", "hidden");
        el.getStyle().setProperty("marginLeft", "-3px");
        el.getStyle().setProperty("backgroundColor", "transparent");

        /** Add small border */
        DivElement smallBorder = Document.get().createDivElement();
        smallBorder.getStyle().setProperty("position", "absolute");
        smallBorder.getStyle().setProperty("width", "1px");
        smallBorder.getStyle().setProperty("height", "100%");
        smallBorder.getStyle().setProperty("left", "3px");
        smallBorder.getStyle().setProperty("top", "0px");
        smallBorder.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
        el.appendChild(smallBorder);

        /** Add large border */
        DivElement largeBorder = Document.get().createDivElement();
        largeBorder.getStyle().setProperty("position", "absolute");
        largeBorder.getStyle().setProperty("width", "2px");
        largeBorder.getStyle().setProperty("height", "100%");
        largeBorder.getStyle().setProperty("left", "1px");
        largeBorder.getStyle().setProperty("top", "0px");
        largeBorder.getStyle().setProperty("opacity", "0.4");
        largeBorder.getStyle().setProperty("backgroundColor", Style.getSplitterLargeBorderColor());
        el.appendChild(largeBorder);
    }

    /**
     * Tunes left splitter. Makes it wider and adds double border to seem rich.
     *
     * @param el
     *         element to tune
     */
    private void tuneRightSplitter(Element el) {
        /** Add Z-Index to move the splitter on the top and make content visible */
        el.getParentElement().getStyle().setProperty("zIndex", "1000");
        el.getParentElement().getStyle().setProperty("overflow", "visible");

        /** Tune splitter catch panel */
        el.getStyle().setProperty("boxSizing", "border-box");
        el.getStyle().setProperty("width", "5px");
        el.getStyle().setProperty("overflow", "hidden");
        el.getStyle().setProperty("marginLeft", "-1px");
        el.getStyle().setProperty("backgroundColor", "transparent");

        /** Add small border */
        DivElement smallBorder = Document.get().createDivElement();
        smallBorder.getStyle().setProperty("position", "absolute");
        smallBorder.getStyle().setProperty("width", "1px");
        smallBorder.getStyle().setProperty("height", "100%");
        smallBorder.getStyle().setProperty("left", "1px");
        smallBorder.getStyle().setProperty("top", "0px");
        smallBorder.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
        el.appendChild(smallBorder);

        /** Add large border */
        DivElement largeBorder = Document.get().createDivElement();
        largeBorder.getStyle().setProperty("position", "absolute");
        largeBorder.getStyle().setProperty("width", "2px");
        largeBorder.getStyle().setProperty("height", "100%");
        largeBorder.getStyle().setProperty("left", "2px");
        largeBorder.getStyle().setProperty("top", "0px");
        largeBorder.getStyle().setProperty("opacity", "0.4");
        largeBorder.getStyle().setProperty("backgroundColor", Style.getSplitterLargeBorderColor());
        el.appendChild(largeBorder);
    }

    /**
     * Tunes bottom splitter. Makes it tiny but with a transparent area for easy resizing.
     *
     * @param el
     *         element to tune
     */
    private void tuneBottomSplitter(Element el) {
        /** Add Z-Index to move the splitter on the top and make content visible */
        el.getParentElement().getStyle().setProperty("zIndex", "1000");
        el.getParentElement().getStyle().setProperty("overflow", "visible");

        el.getStyle().setProperty("height", "3px");
        el.getStyle().setProperty("marginTop", "-2px");
        el.getStyle().setProperty("backgroundColor", "transparent");

        /** Add small border */
        DivElement delimiter = Document.get().createDivElement();
        delimiter.getStyle().setProperty("position", "absolute");
        delimiter.getStyle().setProperty("width", "100%");
        delimiter.getStyle().setProperty("height", "1px");
        delimiter.getStyle().setProperty("left", "0px");
        delimiter.getStyle().setProperty("top", "2px");
        delimiter.getStyle().setProperty("backgroundColor", Style.theme.tabsPanelBackground());
        el.appendChild(delimiter);
    }

    /** Returns right panel.Outline tab is located on this panel. */
    public FlowPanel getRightPanel() {
        return rightPanel;
    }

    /** Returns left panel.Project explorer tab is located on this panel. */
    public FlowPanel getLeftPanel() {
        return leftPanel;
    }

    /**
     * Returns bottom panel. This panel can contains different tabs. When perspective is project, this panel contains Events and
     * Outputs tabs.
     */
    public FlowPanel getBottomPanel() {
        return bottomPanel;
    }

    /** {@inheritDoc} */
    @Override
    public void onResize() {
        editorPanel.onResize();
        super.onResize();
        Widget widget = infoPanel.getWidget();
        if (widget instanceof RequiresResize) {
            ((RequiresResize)widget).onResize();
        }

        int width = getOffsetWidth();
        int height = getOffsetHeight();

        if (delegate != null) {
            delegate.onResize(width, height);
        }
    }

}
