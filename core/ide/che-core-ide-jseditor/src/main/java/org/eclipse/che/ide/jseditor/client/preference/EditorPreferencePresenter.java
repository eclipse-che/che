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
package org.eclipse.che.ide.jseditor.client.preference;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.jseditor.client.preference.keymaps.KeyMapsPreferencePresenter;

/** Preference page presenter for the editors. */
@Singleton
public class EditorPreferencePresenter extends AbstractPreferencePagePresenter implements EditorPreferenceSection.ParentPresenter {

    /** The editor preferences page view. */
    private final EditorPreferenceView view;

    private final KeyMapsPreferencePresenter keymapsSection;

    @Inject
    public EditorPreferencePresenter(final EditorPreferenceView view,
                                     final EditorPrefLocalizationConstant constant,
                                     final EditorPreferenceResource resource,
                                     final KeyMapsPreferencePresenter keymapsSection) {

        super(constant.editorTypeTitle(),
              constant.editorTypeCategory(),
              resource.editorPrefIconTemporary());// TODO use svg icon when the PreferencesPagePresenter allow it

        this.view = view;
        this.keymapsSection = keymapsSection;

        this.keymapsSection.setParent(this);
    }

    @Override
    public boolean isDirty() {
        return keymapsSection.isDirty();
    }

    @Override
    public void go(final AcceptsOneWidget container) {
        keymapsSection.go(view.getKeymapsContainer());
        container.setWidget(view);
    }

    @Override
    public void signalDirtyState() {
        delegate.onDirtyChanged();
    }

    @Override
    public void storeChanges() {
        if (keymapsSection.isDirty()) {
            keymapsSection.storeChanges();
        }
    }

    @Override
    public void revertChanges() {
        keymapsSection.refresh();
        signalDirtyState();
    }

}
