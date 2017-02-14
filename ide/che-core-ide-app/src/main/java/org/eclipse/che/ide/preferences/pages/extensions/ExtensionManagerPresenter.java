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
package org.eclipse.che.ide.preferences.pages.extensions;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.extension.ExtensionDescription;
import org.eclipse.che.ide.api.extension.ExtensionRegistry;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;

import java.util.ArrayList;
import java.util.List;

/** @author Evgen Vidolob */
@Singleton
public class ExtensionManagerPresenter extends AbstractPreferencePagePresenter implements ExtensionManagerView.ActionDelegate {

    private ExtensionManagerView view;
    private ExtensionRegistry    extensionRegistry;
    private boolean              dirty;

    @Inject
    public ExtensionManagerPresenter(CoreLocalizationConstant constant, ExtensionManagerView view, ExtensionRegistry extensionRegistry) {
        super(constant.extensionTitle(), constant.extensionCategory());
        this.view = view;
        this.extensionRegistry = extensionRegistry;
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
        List<ExtensionDescription> extensions = new ArrayList<>();
        for (ExtensionDescription ed : extensionRegistry.getExtensionDescriptions().values()) {
            extensions.add(ed);
        }
        view.setExtensions(extensions);
    }

    @Override
    public void storeChanges() {
        dirty = false;
    }

    @Override
    public void revertChanges() {
    }
}
