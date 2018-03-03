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
