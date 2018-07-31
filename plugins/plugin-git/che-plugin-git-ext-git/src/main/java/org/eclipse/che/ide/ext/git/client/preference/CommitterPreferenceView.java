/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.preference;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View interface for the preference page for the information about git committer.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(CommitterPreferenceViewImpl.class)
public interface CommitterPreferenceView extends View<CommitterPreferenceView.ActionDelegate> {

  /** Sets user name */
  void setName(String name);

  /** Sets user email */
  void setEmail(String email);

  interface ActionDelegate {
    /** User name is being changed */
    void nameChanged(String name);

    /** User email is being changed */
    void emailChanged(String email);
  }
}
