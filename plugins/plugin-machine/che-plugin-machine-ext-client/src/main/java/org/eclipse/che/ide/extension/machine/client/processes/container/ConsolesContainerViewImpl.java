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
package org.eclipse.che.ide.extension.machine.client.processes.container;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.api.parts.base.ToolButton;
import org.eclipse.che.ide.api.theme.Style;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.ui.FontAwesome;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;

/**
 * Implementation of {@link ConsolesContainerView}.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ConsolesContainerViewImpl extends BaseView<ConsolesContainerView.ActionDelegate> implements ConsolesContainerView {
    private final static String VERTICAL_DRAGGER_CLASS            = "gwt-SplitLayoutPanel-VDragger";
    private final static String HORIZONTAL_DRAGGER_CLASS          = "gwt-SplitLayoutPanel-HDragger";
    private final static String ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE = "turnOn";

    private final MachineResources            machineResources;
    private final MachineLocalizationConstant localizationConstant;

    SplitLayoutPanel  splitLayoutPanel;
    ResizeLayoutPanel processesPanel;
    ResizeLayoutPanel terminalsPanel;
    ToolButton        splitVerticallyButton;
    ToolButton        splitHorizontallyButton;
    ToolButton        defaultModeButton;

    @Inject
    public ConsolesContainerViewImpl(PartStackUIResources partStackUIResources,
                                     MachineResources machineResources,
                                     MachineLocalizationConstant localizationConstant) {
        super(partStackUIResources);
        this.machineResources = machineResources;
        this.localizationConstant = localizationConstant;

        splitLayoutPanel = new SplitLayoutPanel(1);
        setContentWidget(splitLayoutPanel);

        processesPanel = new ResizeLayoutPanel();
        terminalsPanel = new ResizeLayoutPanel();
        splitLayoutPanel.add(processesPanel);

        addToolButtons();
        tuneSplitter();
        defaultModeButton.getElement().getFirstChildElement().setAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE, "");
    }

    private void addToolButtons() {
        String toolButtonStyle = machineResources.getCss().consolesActiveToolButton();
        splitVerticallyButton = new ToolButton(FontAwesome.COLUMNS);
        splitVerticallyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSplitVerticallyClick();
            }
        });
        addToolButton(splitVerticallyButton);
        addToolTip(splitVerticallyButton, localizationConstant.consolesSplitVerticallyTooltip());
        splitVerticallyButton.getElement().getFirstChildElement().addClassName(toolButtonStyle);

        splitHorizontallyButton = new ToolButton(FontAwesome.MINUS_SQUARE_O);
        splitHorizontallyButton.getElement().getFirstChildElement().addClassName(toolButtonStyle);
        splitHorizontallyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSplitHorizontallyClick();
            }
        });
        addToolButton(splitHorizontallyButton);
        addToolTip(splitHorizontallyButton, localizationConstant.consolesSplitHorizontallyTooltip());

        defaultModeButton = new ToolButton(FontAwesome.SQUARE_O);
        defaultModeButton.getElement().getFirstChildElement().addClassName(toolButtonStyle);
        defaultModeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onDefaultModeClick();
            }
        });
        addToolButton(defaultModeButton);
        addToolTip(defaultModeButton, localizationConstant.consolesDefaultModeTooltip());
    }

    private void addToolTip(ToolButton button, String tooltip) {
        if (button.getElement() instanceof elemental.dom.Element) {
            Tooltip.create((elemental.dom.Element)button.getElement(),
                           PositionController.VerticalAlign.BOTTOM, PositionController.HorizontalAlign.MIDDLE, tooltip);
        }
    }

    @Override
    public SimplePanel getProcessesContainer() {
        return processesPanel;
    }

    @Override
    public SimplePanel getTerminalsContainer() {
        return terminalsPanel;
    }

    @Override
    public void applyDefaultMode() {
        clear();
        splitLayoutPanel.add(processesPanel);
        defaultModeButton.getElement().getFirstChildElement().setAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE, "");
    }

    @Override
    public void splitVertically() {
        int width = splitLayoutPanel.getOffsetWidth() / 2;

        clear();
        splitLayoutPanel.addWest(processesPanel, width);
        splitLayoutPanel.add(terminalsPanel);
        tuneSplitter();
        splitVerticallyButton.getElement().getFirstChildElement().setAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE, "");
    }

    @Override
    public void splitHorizontally() {
        int height = splitLayoutPanel.getOffsetHeight() / 2;

        clear();
        splitLayoutPanel.addNorth(processesPanel, height);
        splitLayoutPanel.add(terminalsPanel);
        tuneSplitter();
        splitHorizontallyButton.getElement().getFirstChildElement().setAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE, "");
    }

    private void clear() {
        processesPanel.clear();
        terminalsPanel.clear();
        splitLayoutPanel.remove(processesPanel);
        splitLayoutPanel.remove(terminalsPanel);
        defaultModeButton.getElement().getFirstChildElement().removeAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE);
        splitHorizontallyButton.getElement().getFirstChildElement().removeAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE);
        splitVerticallyButton.getElement().getFirstChildElement().removeAttribute(ACTIVE_STATE_TOOLBUTTON_ATTRIBUTE);
    }

    /**
     * Improves splitter visibility.
     */
    private void tuneSplitter() {
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
}
