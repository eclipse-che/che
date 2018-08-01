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
package org.eclipse.che.ide.command.type.custom;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link CustomPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(CustomPageViewImpl.class)
public interface CustomPageView extends View<CustomPageView.ActionDelegate> {

  /** Returns value of the 'Command line' field. */
  String getCommandLine();

  /** Sets value of the 'Command line' field. */
  void setCommandLine(String commandLine);

  /** Action handler for the view actions/controls. */
  interface ActionDelegate {

    /** Called when value of the 'Command line' field has been changed. */
    void onCommandLineChanged();
  }
}
