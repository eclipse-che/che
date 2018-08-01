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
package org.eclipse.che.ide.ext.git.client.preference;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;

/**
 * Preference page presenter for the information about git committer.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class CommitterPreferencePresenter extends AbstractPreferencePagePresenter
    implements CommitterPreferenceView.ActionDelegate {

  public static final String COMMITTER_NAME = "git.committer.name";
  public static final String COMMITTER_EMAIL = "git.committer.email";

  private CommitterPreferenceView view;
  private PreferencesManager preferencesManager;
  private boolean dirty = false;
  private String name;
  private String email;

  @Inject
  public CommitterPreferencePresenter(
      CommitterPreferenceView view,
      GitLocalizationConstant constant,
      PreferencesManager preferencesManager) {
    super(constant.committerTitle(), constant.committerPreferenceCategory());
    this.view = view;
    this.preferencesManager = preferencesManager;

    name = preferencesManager.getValue(COMMITTER_NAME);
    email = preferencesManager.getValue(COMMITTER_EMAIL);

    view.setDelegate(this);
  }

  /** {@inheritDoc} */
  @Override
  public boolean isDirty() {
    return dirty;
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);

    view.setName(name);
    view.setEmail(email);
  }

  /** {@inheritDoc} */
  @Override
  public void nameChanged(String name) {
    this.name = name;
    dirty = !name.equals(preferencesManager.getValue(COMMITTER_NAME));
    delegate.onDirtyChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void emailChanged(String email) {
    this.email = email;
    dirty = !email.equals(preferencesManager.getValue(COMMITTER_EMAIL));
    delegate.onDirtyChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void storeChanges() {
    preferencesManager.setValue(COMMITTER_NAME, name);
    preferencesManager.setValue(COMMITTER_EMAIL, email);

    dirty = false;
  }

  /** {@inheritDoc} */
  @Override
  public void revertChanges() {
    name = preferencesManager.getValue(COMMITTER_NAME);
    email = preferencesManager.getValue(COMMITTER_EMAIL);

    view.setName(name);
    view.setEmail(email);

    dirty = false;
  }
}
