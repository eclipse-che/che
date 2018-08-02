/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.ext.java.client.project.classpath.ProjectClasspathResources;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The class describes special widget which is entry in list of nodes.
 *
 * @author Valeriy Svydenko
 */
public class NodeWidget extends Composite implements NodeEntry, ClickHandler {
  interface RecipeEntryWidgetUiBinder extends UiBinder<Widget, NodeWidget> {}

  private static final RecipeEntryWidgetUiBinder UI_BINDER =
      GWT.create(RecipeEntryWidgetUiBinder.class);

  @UiField SimplePanel icon;
  @UiField Label name;
  @UiField FlowPanel main;
  @UiField SimplePanel removeButton;

  private final ProjectClasspathResources resources;
  private final String nodeName;
  private final int nodeKind;

  private ActionDelegate delegate;

  public NodeWidget(
      String nodeName, ProjectClasspathResources resources, int nodeKind, SVGResource nodeIcon) {
    this.resources = resources;
    this.nodeName = nodeName;
    this.nodeKind = nodeKind;

    initWidget(UI_BINDER.createAndBindUi(this));

    SVGImage icon = new SVGImage(nodeIcon.getSvg());
    this.icon.getElement().appendChild(icon.getSvgElement().getElement());
    this.removeButton.getElement().appendChild(resources.removeNode().getSvg().getElement());

    name.setText(nodeName);

    removeButton.addDomHandler(
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            delegate.onRemoveButtonClicked(NodeWidget.this);
          }
        },
        ClickEvent.getType());
    addDomHandler(this, ClickEvent.getType());
  }

  /** {@inheritDoc} */
  @Override
  public void onClick(@NotNull ClickEvent event) {
    delegate.onNodeClicked(this);
  }

  /** {@inheritDoc} */
  @Override
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /** Changes style of widget as selected */
  public void select() {
    main.addStyleName(resources.getCss().selectNode());
  }

  /** Changes style of widget as unselected */
  public void deselect() {
    main.removeStyleName(resources.getCss().selectNode());
  }

  /** Sets name of the Recipe. */
  public void setName(@NotNull String name) {
    this.name.setText(name);
  }

  /** Returns name of the current node. */
  public String getName() {
    return nodeName;
  }

  /** Hides panel wit the Remove button. */
  public void hideRemoveButton() {
    removeButton.setVisible(false);
  }

  /** Returns type of the node. */
  public int getKind() {
    return nodeKind;
  }
}
