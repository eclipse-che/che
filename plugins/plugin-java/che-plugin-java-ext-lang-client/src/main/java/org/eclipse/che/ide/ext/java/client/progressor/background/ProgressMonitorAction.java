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
package org.eclipse.che.ide.ext.java.client.progressor.background;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * Action which shows the process of resolving Maven dependencies.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class ProgressMonitorAction extends BaseAction implements CustomComponentAction {
  private final BackgroundLoaderPresenter loader;

  @Inject
  public ProgressMonitorAction(BackgroundLoaderPresenter loader) {
    this.loader = loader;
    loader.hide();
  }

  @Override
  public void actionPerformed(ActionEvent e) {}

  @Override
  public Widget createCustomComponent(Presentation presentation) {
    return loader.getCustomComponent();
  }
}
