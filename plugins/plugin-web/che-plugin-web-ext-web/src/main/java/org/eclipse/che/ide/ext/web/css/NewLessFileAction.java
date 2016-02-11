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
package org.eclipse.che.ide.ext.web.css;

import org.eclipse.che.ide.ext.web.WebLocalizationConstant;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to create new Less file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewLessFileAction extends AbstractNewResourceAction {
    private static final String DEFAULT_CONTENT = "@CHARSET \"UTF-8\"\n;";

    @Inject
    public NewLessFileAction(WebLocalizationConstant localizationConstant) {
        super(localizationConstant.newLessFileActionTitle(),
              localizationConstant.newLessFileActionDescription(),
              null);
    }

    @Override
    protected String getExtension() {
        return "less";
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
