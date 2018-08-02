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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link JavaCommandPagePresenter}.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(JavaCommandPageViewImpl.class)
public interface JavaCommandPageView extends View<JavaCommandPageView.ActionDelegate> {
  /** Returns project. */
  String getProject();

  /** Sets project. */
  void setProject(String project);

  /** Returns the path to main class. */
  String getMainClass();

  /** Sets the path to main class. */
  void setMainClass(String mainClass);

  /** Returns command line. */
  String getCommandLine();

  /** Sets command line. */
  void setCommandLine(String commandLine);

  /** Action handler for the view actions/controls. */
  interface ActionDelegate {

    /** Called when 'Choose Main Class' button has been clicked. */
    void onAddMainClassBtnClicked();

    /** Called when command line has been changed. */
    void onCommandLineChanged();
  }
}
