/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class ExpandEditorAction extends BaseAction implements CustomComponentAction {

  private final Resources resources;
  private final CoreLocalizationConstant constant;
  private final PerspectiveManager perspectiveManager;

  private FlowPanel buttonPanel;
  private FlowPanel button;

  @Inject
  public ExpandEditorAction(
      Resources resources,
      PerspectiveManager perspectiveManager,
      CoreLocalizationConstant constant) {
    super(constant.actionExpandEditorTitle(), null, FontAwesome.EXPAND);
    this.resources = resources;
    this.perspectiveManager = perspectiveManager;
    this.constant = constant;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    toggleExpand();
  }

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    if (buttonPanel != null) {
      return buttonPanel;
    }

    final Element tooltip = DOM.createSpan();
    tooltip.setInnerHTML(constant.actionExpandEditorTitle());

    buttonPanel = new FlowPanel();
    buttonPanel.addStyleName(resources.coreCss().editorFullScreen());

    button = new FlowPanel();
    button.getElement().setInnerHTML(FontAwesome.EXPAND);
    button.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            toggleExpand();
          }
        },
        ClickEvent.getType());

    buttonPanel.add(button);
    buttonPanel.getElement().appendChild(tooltip);

    buttonPanel.addDomHandler(
        new MouseOverHandler() {
          @Override
          public void onMouseOver(MouseOverEvent event) {
            final Element panel = event.getRelativeElement();
            tooltip
                .getStyle()
                .setProperty("top", (panel.getAbsoluteTop() + panel.getOffsetHeight() + 9) + "px");
            tooltip
                .getStyle()
                .setProperty(
                    "right",
                    (Document.get().getClientWidth() - panel.getAbsoluteRight() - 2) + "px");
          }
        },
        MouseOverEvent.getType());

    return buttonPanel;
  }

  /** Expands or restores the editor. */
  public void toggleExpand() {
    Perspective perspective = perspectiveManager.getActivePerspective();
    if (perspective == null) {
      return;
    }

    PartStack partStack = perspective.getPartStack(PartStackType.EDITING);
    if (partStack == null) {
      return;
    }

    if (partStack.getPartStackState() == PartStack.State.NORMAL) {
      perspective.maximizeCentralPartStack();
      button.getElement().setInnerHTML(FontAwesome.COMPRESS);
    } else {
      perspective.restore();
      button.getElement().setInnerHTML(FontAwesome.EXPAND);
    }
  }
}
