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
package org.eclipse.che.ide.ext.help.client.about;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.help.client.BuildInfo;
import org.eclipse.che.ide.ext.help.client.about.info.BuildDetailsPresenter;

/**
 * Presenter for displaying About Che information.
 *
 * @author Ann Shumilova
 */
@Singleton
public class AboutPresenter implements AboutView.ActionDelegate {
  private AboutView view;
  private BuildDetailsPresenter buildDetailsPresenter;
  private BuildInfo buildInfo;

  @Inject
  public AboutPresenter(
      AboutView view, BuildInfo buildInfo, BuildDetailsPresenter buildDetailsPresenter) {
    this.view = view;
    this.buildDetailsPresenter = buildDetailsPresenter;
    view.setDelegate(this);

    this.buildInfo = buildInfo;
  }

  public void showAbout() {
    view.showDialog();
    view.setVersion(buildInfo.version());
  }

  /** {@inheritDoc} */
  @Override
  public void onOkClicked() {
    view.close();
  }

  @Override
  public void onShowBuildDetailsClicked() {
    buildDetailsPresenter.showBuildDetails();
  }
}
