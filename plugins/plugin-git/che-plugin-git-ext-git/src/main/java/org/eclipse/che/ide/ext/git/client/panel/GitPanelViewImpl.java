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
package org.eclipse.che.ide.ext.git.client.panel;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ext.git.client.compare.changespanel.ChangesPanelView;
import org.eclipse.che.ide.ext.git.client.panel.GitPanelView.ActionDelegate;

/** @author Mykola Morhun */
public class GitPanelViewImpl extends BaseView<ActionDelegate> implements GitPanelView {
  interface GitPanelViewImplUiBinder extends UiBinder<Widget, GitPanelViewImpl> {}

  @UiField FlowPanel changesPanel;

  @Inject
  public GitPanelViewImpl(
      PartStackUIResources resources, GitPanelViewImpl.GitPanelViewImplUiBinder uiBinder) {
    super(resources);

    setContentWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setChangesPanelView(ChangesPanelView changesPanelView) {
    this.changesPanel.add(changesPanelView);
  }
}
