/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.core;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * Message that displays in status bar if user has not enough free disk space.
 *
 * @author Vlad Zhukovskyi
 * @since 6.9.0
 */
@Singleton
public class FreeDiskSpaceStatusBarAction extends BaseAction implements CustomComponentAction {

  private FreeDiskSpaceNotifier notifier;
  private CoreLocalizationConstant constant;
  private HorizontalPanel panel;

  @Inject
  public FreeDiskSpaceStatusBarAction(
      FreeDiskSpaceNotifier notifier, CoreLocalizationConstant constant) {
    super();
    this.notifier = notifier;
    this.constant = constant;

    panel = new HorizontalPanel();
    panel.ensureDebugId("statusBarFreeDiskSpacePanel");
  }

  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public void update(ActionEvent e) {
    panel.clear();

    if (notifier.isNotified()) {
      Widget icon = new HTML(FontAwesome.EXCLAMATION_TRIANGLE);
      icon.getElement().getStyle().setMarginRight(5., Style.Unit.PX);
      panel.add(icon);

      Label headLabel = new Label(constant.lowDiskSpaceStatusBarMessage());
      headLabel.ensureDebugId("statusBarProjectFreeDiskSpaceNotification");
      Style headLabelStyle = headLabel.getElement().getStyle();
      headLabelStyle.setMarginRight(5., Style.Unit.PX);
      panel.add(headLabel);
    }
  }

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    return panel;
  }
}
