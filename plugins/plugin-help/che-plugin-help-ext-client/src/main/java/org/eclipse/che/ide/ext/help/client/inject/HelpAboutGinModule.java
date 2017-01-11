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
package org.eclipse.che.ide.ext.help.client.inject;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.help.client.about.AboutView;
import org.eclipse.che.ide.ext.help.client.about.AboutViewImpl;

import com.google.gwt.inject.client.AbstractGinModule;

/** @author Vitalii Parfonov */
@ExtensionGinModule
public class HelpAboutGinModule extends AbstractGinModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(AboutView.class).to(AboutViewImpl.class);

    }
}
