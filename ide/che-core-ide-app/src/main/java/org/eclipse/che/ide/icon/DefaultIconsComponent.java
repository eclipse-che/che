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
package org.eclipse.che.ide.icon;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.component.Component;
import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class DefaultIconsComponent implements Component {

    private final IconRegistry iconRegistry;
    private final Resources    resources;

    @Inject
    public DefaultIconsComponent(IconRegistry iconRegistry, Resources resources) {
        this.iconRegistry = iconRegistry;
        this.resources = resources;
    }

    @Override
    public void start(Callback<Component, Exception> callback) {
        iconRegistry.registerIcon(new Icon("default.projecttype.small.icon", "default/project.png", resources.defaultProject()));
        iconRegistry.registerIcon(new Icon("default.folder.small.icon", "default/folder.png", resources.defaultFolder()));
        iconRegistry.registerIcon(new Icon("default.file.small.icon", "default/file.png", resources.defaultFile()));
        iconRegistry.registerIcon(new Icon("default", "default/default.jpg", resources.defaultIcon()));
        callback.onSuccess(this);
    }
}
