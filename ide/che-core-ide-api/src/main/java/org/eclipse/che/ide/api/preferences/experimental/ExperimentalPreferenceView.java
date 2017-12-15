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
package org.eclipse.che.ide.api.preferences.experimental;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/** View interface for the preference page for the experimental features. */
@ImplementedBy(ExperimentalPreferenceViewImpl.class)
public interface ExperimentalPreferenceView
    extends View<ExperimentalPreferenceView.ActionDelegate> {

  /**
   * Sets if the feature should be enabled
   *
   * @param enabled A boolean if the feature should be enabled
   */
  void setEnable(boolean enabled);

  interface ActionDelegate {

    /**
     * Call when enabled information has been changed giving the value
     *
     * @param enabled Whether the feature has been enabled or not
     */
    void enabledChanged(boolean enabled);
  }
}
