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
package org.eclipse.che.plugin.cpp.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.cpp.ide.CppLocalizationConstant;
import org.eclipse.che.plugin.cpp.ide.CppResources;

import static org.eclipse.che.plugin.cpp.shared.Constants.H_EXT;

/**
 * Action to create new C Header file.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class CreateHeaderSourceFileAction extends NewClikeResourceAction {

    private static final String DEFAULT_CONTENT = "#ifndef VARIABLE\n" +
                                                  "#define VARIABLE\n" +
                                                  "// Write your header file here.\n" +
                                                  "#endif";

    @Inject
    public CreateHeaderSourceFileAction(CppLocalizationConstant localizationConstant,
                                        CppResources cppResources,
                                        AppContext appContext) {
        super(localizationConstant.createCHeaderFileActionTitle(),
              localizationConstant.createCHeaderFileActionDescription(),
              cppResources.cHeaderFile(),
              appContext);
    }

    @Override
    protected String getExtension() {
        return H_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
