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
package org.eclipse.che.plugin.maven.client.command;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link MavenCommandPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(MavenCommandPageViewImpl.class)
public interface MavenCommandPageView extends View<MavenCommandPageView.ActionDelegate> {

  /** Returns value of the 'Working directory' field. */
  String getWorkingDirectory();

  /** Sets value of the 'Working directory' field. */
  void setWorkingDirectory(String workingDirectory);

  /** Returns value of the 'Arguments' field. */
  String getArguments();

  /** Sets value of the 'Arguments' field. */
  void setArguments(String args);

  /** Action handler for the view actions/controls. */
  interface ActionDelegate {

    /** Called when value of the 'Working directory' field has been changed. */
    void onWorkingDirectoryChanged();

    /** Called when value of the 'Arguments' field has been changed. */
    void onArgumentsChanged();
  }
}
