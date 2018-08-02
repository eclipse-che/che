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
package org.eclipse.che.plugin.pullrequest.client.preference;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View interface for the Contribute Part preference page.
 *
 * @author Roman Nikitenko
 */
@ImplementedBy(ContributePreferenceViewImpl.class)
public interface ContributePreferenceView extends View<ContributePreferenceView.ActionDelegate> {

  /** Sets 'Activate by project selection' property */
  void setActivateByProjectSelection(boolean isActivate);

  interface ActionDelegate {
    /** 'Activate by project selection' property is being changed */
    void onActivateByProjectSelectionChanged(boolean isActivated);
  }
}
