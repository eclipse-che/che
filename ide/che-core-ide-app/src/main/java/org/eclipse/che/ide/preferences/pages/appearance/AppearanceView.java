/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.preferences.pages.appearance;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.theme.Theme;

import java.util.List;

/**
 * View of 'Appearance' preferences page.
 *
 * @author Evgen Vidolob
 * @author Igor Vinokur
 */
public interface AppearanceView extends View<AppearanceView.ActionDelegate> {

    void setThemes(List<Theme> themes, String currentThemeId);

    /**
     * Change the state of 'Show maven artifact id' checkbox.
     *
     * @param selected
     *         {@code true} to make the checkbox selected, {@code false} to deselect the checkbox
     */
    void setSelectedShowMavenArtifactIdCheckBox(boolean selected);

    interface ActionDelegate {
        void themeSelected(String themeId);

        /**
         * Called when the value of 'Show maven artifact id' checkbox is changed.
         *
         * @param value
         *         new value
         */
        void showMavenArtifactIdCheckBoxValueChanged(boolean value);
    }
}
