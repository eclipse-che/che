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
package org.eclipse.che.plugin.maven.client.preference;

import com.google.inject.ImplementedBy;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View of Maven preferences page.
 *
 * @author Igor Vinokur
 */
@ImplementedBy(MavenPreferenceViewImpl.class)
public interface MavenPreferenceView extends View<MavenPreferenceView.ActionDelegate> {

  /**
   * Change the state of 'Show maven artifact id' checkbox.
   *
   * @param selected {@code true} to make the checkbox selected, {@code false} to deselect the
   *     checkbox
   */
  void setSelectedShowArtifactIdCheckBox(boolean selected);

  interface ActionDelegate {
    /**
     * Called when the value of 'Show maven artifact id' checkbox is changed.
     *
     * @param value new value
     */
    void onArtifactIdCheckBoxValueChanged(boolean value);
  }
}
