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
package org.eclipse.che.ide.terminal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import javax.validation.constraints.NotNull;

/**
 * The class contains methods to display terminal.
 *
 * @author Dmitry Shnurenko
 * @author Oleksandr Andriienko
 */
final class TerminalViewImpl extends Composite implements TerminalView, Focusable, RequiresResize {

  interface TerminalViewImplUiBinder extends UiBinder<Widget, TerminalViewImpl> {}

  private static final TerminalViewImplUiBinder UI_BINDER =
      GWT.create(TerminalViewImplUiBinder.class);

  @UiField FlowPanel terminalPanel;

  @UiField Label unavailableLabel;

  private TerminalJso terminal;
  private boolean isOpen;
  private boolean focusOnOpen;

  public TerminalViewImpl() {
    initWidget(UI_BINDER.createAndBindUi(this));
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {}

  /** {@inheritDoc} */
  @Override
  public void setTerminal(@NotNull final TerminalJso terminal, boolean focusOnOpen) {
    unavailableLabel.setVisible(false);

    terminalPanel.setVisible(true);
    this.focusOnOpen = focusOnOpen;
    this.terminal = terminal;
  }

  /** {@inheritDoc} */
  @Override
  public void showErrorMessage(@NotNull String message) {
    unavailableLabel.setText(message);
    unavailableLabel.setVisible(true);

    terminalPanel.setVisible(false);
  }

  @Override
  public String[] getRenderedLines() {
    if (terminal != null) {
      return terminal.getRenderedLines();
    }
    return null;
  }

  /**
   * Resize {@link TerminalJso} to current widget size. To improve performance we should resize only
   * visible terminals, because "resize terminal" is quite expensive operation. When you click on
   * the tab to activate hidden terminal this method will be executed too, so terminal will be
   * resized anyway.
   */
  @Override
  public void onResize() {
    if (terminal != null) {
      if (isOpen) {
        resizeTimer.schedule(200);
      } else {
        open();
      }
    }
  }

  private Timer resizeTimer =
      new Timer() {
        @Override
        public void run() {
          resizeTerminal();
        }
      };

  private void open() {
    if (getElement().getOffsetWidth() > 0 && getElement().getOffsetHeight() > 0) {
      terminal.open(terminalPanel.getElement());
      if (focusOnOpen) {
        terminal.focus();
      }
      isOpen = true;
    }
  }

  private void resizeTerminal() {
    TerminalGeometryJso geometryJso = terminal.proposeGeometry();
    int x = geometryJso.getCols();
    int y = geometryJso.getRows();

    if (x > 0 && y > 0 && isVisible() && isAttached()) {
      terminal.resize(geometryJso.getCols(), geometryJso.getRows());
    }
  }

  @Override
  public int getTabIndex() {
    return 0;
  }

  @Override
  public void setAccessKey(char key) {}

  @Override
  public void setFocus(boolean focused) {
    if (terminal != null && isOpen) {
      terminal.focus();
    }
  }

  @Override
  public void setTabIndex(int index) {}
}
