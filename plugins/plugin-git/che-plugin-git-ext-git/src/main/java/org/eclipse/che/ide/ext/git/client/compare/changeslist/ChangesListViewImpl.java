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
package org.eclipse.che.ide.ext.git.client.compare.changeslist;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link ChangesListView}.
 *
 * @author Igor Vinokur
 */
public class ChangesListViewImpl extends Window implements ChangesListView {
  private final GitLocalizationConstant locale;

  private ActionDelegate delegate;
  private Button btnCompare;

  @Inject
  protected ChangesListViewImpl(GitLocalizationConstant locale) {
    this.locale = locale;
    this.setTitle(locale.changeListTitle());

    createButtons();
  }

  @Override
  public void setEnableCompareButton(boolean enableCompareButton) {
    btnCompare.setEnabled(enableCompareButton);
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void close() {
    this.hide();
  }

  @Override
  public void showDialog() {
    this.show();
  }

  @Override
  public void setChangesPanelView(ChangesPanelView changesPanelView) {
    FlowPanel flowPanel = new FlowPanel();
    flowPanel.ensureDebugId("git-compare-window-changed-files");
    flowPanel.setWidth("600px");
    flowPanel.setHeight("345px");
    flowPanel.add((Widget) changesPanelView);
    this.setWidget(flowPanel);
  }

  private void createButtons() {
    Button btnClose =
        createButton(
            locale.buttonClose(), "git-compare-btn-close", event -> delegate.onCloseClicked());
    btnCompare =
        createButton(
            locale.buttonCompare(),
            "git-compare-btn-compare",
            event -> delegate.onCompareClicked());

    addButtonToFooter(btnClose);
    addButtonToFooter(btnCompare);
  }
}
