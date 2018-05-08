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
package org.eclipse.che.ide.command.editor.page.renderer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.che.ide.api.console.OutputConsoleRenderer;

/**
 * Implementation of {@link RenderersPageView}.
 *
 * @author Victor Rubezhny
 */
public class RenderersPageViewImpl extends Composite implements RenderersPageView {

  private static final RenderersPageViewImplUiBinder UI_BINDER =
      GWT.create(RenderersPageViewImplUiBinder.class);

  @UiField FlowPanel mainPanel;

  @UiField FlowPanel renderersPanel;

  private ActionDelegate delegate;

  @Inject
  public RenderersPageViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));

    mainPanel.setVisible(false);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setRenderers(Map<OutputConsoleRenderer, Boolean> renderers) {
    renderersPanel.clear();
    mainPanel.setVisible(!renderers.isEmpty());

    renderers.forEach(this::addRendererSwitcherToPanel);
  }

  private void addRendererSwitcherToPanel(OutputConsoleRenderer renderer, boolean applicable) {
    final RendererSwitcher switcher = new RendererSwitcher(renderer.getName());
    switcher.setValue(applicable);
    switcher.addValueChangeHandler(
        event -> delegate.onApplicableRendererChanged(renderer, event.getValue()));

    renderersPanel.add(switcher);
  }

  interface RenderersPageViewImplUiBinder extends UiBinder<Widget, RenderersPageViewImpl> {}
}
