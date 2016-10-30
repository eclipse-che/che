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
package org.eclipse.che.ide.part.editor.multipart;

import com.google.common.collect.BiMap;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.api.constraints.Direction;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.api.theme.Style;

import static org.eclipse.che.ide.api.constraints.Direction.HORIZONTALLY;
import static org.eclipse.che.ide.api.constraints.Direction.VERTICALLY;

/**
 * @author Roman Nikitenko
 */
public class SplitEditorPartViewImpl implements SplitEditorPartView {
    private final static int    SPLITTER_SIZE            = 5;
    private final static String VERTICAL_DRAGGER_CLASS   = "gwt-SplitLayoutPanel-VDragger";
    private final static String HORIZONTAL_DRAGGER_CLASS = "gwt-SplitLayoutPanel-HDragger";

    private       SplitEditorPartView parent;
    private       SplitEditorPartView specimenView;
    private       SplitEditorPartView replicaView;
    private final IsWidget            specimenWidget;
    private final SimpleLayoutPanel   rootPanel;
    private       SplitLayoutPanel    splitLayoutPanel;
    private Direction direction;


    @AssistedInject
    public SplitEditorPartViewImpl(@Assisted IsWidget specimenWidget) {
        this.specimenWidget = specimenWidget;
        rootPanel = new SimpleLayoutPanel();
        rootPanel.add(specimenWidget);
    }

    @AssistedInject
    public SplitEditorPartViewImpl(@Assisted IsWidget specimen, @Assisted SplitEditorPartView parent) {
        this(specimen);
        this.parent = parent;
    }

    @Override
    public SplitEditorPartView getSpecimen() {
        return specimenView;
    }

    @Override
    public SplitEditorPartView getReplica() {
        return replicaView;
    }

    @Override
    public void split(IsWidget replicaWidget, Direction direction, double size) {
        this.direction = direction;
        splitLayoutPanel = new SplitLayoutPanel(SPLITTER_SIZE);
        specimenView = new SplitEditorPartViewImpl(specimenWidget, this);
        replicaView = new SplitEditorPartViewImpl(replicaWidget, this);

        if (direction == VERTICALLY) {
            splitVertically(size);
        } else if (direction == HORIZONTALLY) {
            splitHorizontally(size);
        }

        splitLayoutPanel.add(replicaView);
        rootPanel.remove(specimenWidget);
        rootPanel.add(splitLayoutPanel);

        tuneSplitter(splitLayoutPanel);
    }

    private void splitVertically(double size) {
        double newSize = size == -1 ? rootPanel.getOffsetWidth() / 2 : size;
        splitLayoutPanel.addWest(specimenView, newSize);
    }

    private void splitHorizontally(double size) {
        double newSize = size == -1 ? rootPanel.getOffsetHeight() / 2 : size;
        splitLayoutPanel.addNorth(specimenView, newSize);
    }

    @Override
    public void removeChild(SplitEditorPartView child) {
        splitLayoutPanel.remove(child);
        if (child == replicaView && specimenView != null) {
            splitLayoutPanel.add(specimenView);
        }

        if (child == specimenView) {
            specimenView = null;
        } else if (child == replicaView) {
            replicaView = null;
        }

        if (parent != null && isEmpty()) {
            parent.removeChild(this);
        }

        if (isEmpty()) {
            rootPanel.removeFromParent();
        }
        splitLayoutPanel.forceLayout();
    }

    @Override
    public void removeFromParent() {
        if (parent != null) {
            parent.removeChild(this);
        } else {
            rootPanel.removeFromParent();
        }
    }

    @Override
    public EditorMultiPartStackState getState(BiMap<SplitEditorPartView, EditorPartStack> splitEditorParts) {
        if (splitLayoutPanel == null) {
            return new EditorMultiPartStackState(splitEditorParts.get(this));

        } else if(specimenView == null && replicaView != null){
            return replicaView.getState(splitEditorParts);
        } else if(replicaView == null && specimenView != null){
            return specimenView.getState(splitEditorParts);
        }
        if(specimenView != null){
            return new EditorMultiPartStackState(direction,
                                                 splitLayoutPanel.getWidgetSize(specimenView.asWidget()),
                                                 specimenView.getState(splitEditorParts),
                                                 replicaView.getState(splitEditorParts));
        }
        throw new IllegalStateException("Can't create state, specimenView and replicaView are both null.");
    }

    private boolean isEmpty() {
        return specimenView == null && replicaView == null;
    }

    /**
     * Improves splitter visibility.
     */
    private void tuneSplitter(SplitLayoutPanel splitLayoutPanel) {
        NodeList<Node> nodes = splitLayoutPanel.getElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            if (node.hasChildNodes()) {
                Element el = node.getFirstChild().cast();
                String className = el.getClassName();
                if (HORIZONTAL_DRAGGER_CLASS.equals(className)) {
                    tuneVerticalSplitter(el);
                } else if (VERTICAL_DRAGGER_CLASS.equals(className)) {
                    tuneHorizontalSplitter(el);
                }
            }
        }
    }

    /**
     * Tunes splitter. Makes it wider and adds double border to seem rich.
     *
     * @param el
     *         element to tune
     */
    private void tuneVerticalSplitter(Element el) {
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
    }

    /**
     * Tunes bottom splitter. Makes it tiny but with a transparent area for easy resizing.
     *
     * @param el
     *         element to tune
     */
    private void tuneHorizontalSplitter(Element el) {
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
        delimiter.getStyle().setProperty("backgroundColor", Style.getSplitterSmallBorderColor());
        delimiter.getStyle().setProperty("top", "2px");
        el.appendChild(delimiter);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }
}
