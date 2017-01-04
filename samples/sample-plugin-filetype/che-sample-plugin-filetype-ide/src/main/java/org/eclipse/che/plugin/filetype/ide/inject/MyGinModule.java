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
package org.eclipse.che.plugin.filetype.ide.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.plugin.filetype.ide.MyResources;

/**
 * A Gin module that registers a custom file type named "MyFileType".
 *
 * @author Edgar Mueller
 */
@ExtensionGinModule
public class MyGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        //Nothing to do here, yet
    }

    @Provides
    @Singleton
    @Named("MyFileType")
    protected FileType provideMyFile() {
        return new FileType(MyResources.INSTANCE.icon(), "my");
    }

}
