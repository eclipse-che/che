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
package org.eclipse.che.ide.factory.welcome.preferences;

import com.google.gwt.user.client.ui.HasValue;
import org.eclipse.che.ide.api.mvp.View;

/** @author Vitaliy Guliy */
public interface ShowWelcomePreferencePageView
    extends View<ShowWelcomePreferencePageView.ActionDelegate> {

  interface ActionDelegate {
    void onDirtyChanged();
  }

  HasValue<Boolean> welcomeField();
}
