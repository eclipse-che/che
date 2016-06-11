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
package org.eclipse.che.plugin.filetype.ide.action;

import com.google.inject.Inject;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.plugin.filetype.ide.MyResources;

/**
 * An action that creates a new file with a "my" extension.
 *
 * @author Edgar Mueller
 */
public class CreateMyFileAction extends AbstractNewResourceAction {

    @Inject
    public CreateMyFileAction(MyResources myResources) {
        super("Create my File", "Create a new file ", myResources.icon());
    }

    @Override
    protected String getExtension() {
        return "my";
    }
}
