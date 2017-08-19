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
package org.eclipse.che.plugin.jsonexample.ide.project;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;

/** Simple wizard page that contains the {@link SchemaUrlPageView}. */
public class SchemaUrlWizardPage extends AbstractWizardPage<MutableProjectConfig> {

  private final SchemaUrlPageViewImpl view;

  /**
   * Constructor.
   *
   * @param view the view to be displayed.
   */
  @Inject
  public SchemaUrlWizardPage(SchemaUrlPageViewImpl view) {
    this.view = view;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    view.setDelegate(new SchemaUrlChangedDelegate(dataObject));
  }
}
