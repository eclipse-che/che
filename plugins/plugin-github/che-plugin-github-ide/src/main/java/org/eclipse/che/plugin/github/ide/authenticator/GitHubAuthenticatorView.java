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
package org.eclipse.che.plugin.github.ide.authenticator;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/** @author Roman Nikitenko */
@ImplementedBy(GitHubAuthenticatorViewImpl.class)
public interface GitHubAuthenticatorView extends View<GitHubAuthenticatorView.ActionDelegate> {

  interface ActionDelegate {
    /** Defines what's done when the user clicks cancel. */
    void onCancelled();

    /** Defines what's done when the user clicks OK. */
    void onAccepted();
  }

  /** Show dialog. */
  void showDialog();

  /** Performs when user select generate keys. */
  boolean isGenerateKeysSelected();
}
