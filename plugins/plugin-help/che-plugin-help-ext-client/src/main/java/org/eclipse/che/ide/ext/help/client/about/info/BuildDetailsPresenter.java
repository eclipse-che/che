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
package org.eclipse.che.ide.ext.help.client.about.info;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.ext.help.client.BuildDetailsProvider;

/**
 * Presenter to manipulate with `Show Build Details` dialog.
 *
 * @author Vlad Zhukovskyi
 * @since 6.7.0
 */
@Singleton
public class BuildDetailsPresenter implements BuildDetailsView.ActionDelegate {
  private final BuildDetailsView view;

  @Inject
  public BuildDetailsPresenter(BuildDetailsView view, BuildDetailsProvider summaryProvider) {
    this.view = view;
    this.view.setDelegate(this);
    this.view.setBuildDetails(summaryProvider.getBuildDetails());
  }

  public void showBuildDetails() {
    view.showDialog();
  }
}
