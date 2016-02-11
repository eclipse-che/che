/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.preference.inject;


import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.multibindings.GinMultibinder;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.preferences.PreferencePagePresenter;
import org.eclipse.che.ide.jseditor.client.preference.EditorPreferencePresenter;
import org.eclipse.che.ide.jseditor.client.preference.EditorPreferenceView;
import org.eclipse.che.ide.jseditor.client.preference.EditorPreferenceViewImpl;
import org.eclipse.che.ide.jseditor.client.preference.keymaps.KeyMapsPreferencePresenter;
import org.eclipse.che.ide.jseditor.client.preference.keymaps.KeymapsPreferenceView;
import org.eclipse.che.ide.jseditor.client.preference.keymaps.KeymapsPreferenceViewImpl;

/** Gin module for the editor preferences. */
@ExtensionGinModule
public class EditorPreferencesGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        // Bind the editor preference panel
        final GinMultibinder<PreferencePagePresenter> prefBinder = GinMultibinder.newSetBinder(binder(), PreferencePagePresenter.class);
        prefBinder.addBinding().to(EditorPreferencePresenter.class);

        bind(EditorPreferenceView.class).to(EditorPreferenceViewImpl.class);
        bind(KeymapsPreferenceView.class).to(KeymapsPreferenceViewImpl.class);
        bind(KeyMapsPreferencePresenter.class);
    }
}
