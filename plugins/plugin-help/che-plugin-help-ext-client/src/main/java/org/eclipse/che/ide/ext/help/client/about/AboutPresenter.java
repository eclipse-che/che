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
package org.eclipse.che.ide.ext.help.client.about;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.help.client.BuildInfo;

/**
 * Presenter for displaying About Codenvy information.
 *
 * @author Ann Shumilova
 */
@Singleton
public class AboutPresenter implements AboutView.ActionDelegate {
  private AboutView view;
  private BuildInfo buildInfo;

  @Inject
  public AboutPresenter(AboutView view, BuildInfo buildInfo) {
    this.view = view;
    view.setDelegate(this);

    this.buildInfo = buildInfo;
  }

  /** Show About Codenvy information. */
  public void showAbout() {
    view.showDialog();
    view.setRevision(buildInfo.revision());
    view.setVersion(buildInfo.version());
    view.setTime(buildInfo.buildTime());
  }

  /** {@inheritDoc} */
  @Override
  public void onOkClicked() {
    view.close();
  }
}
