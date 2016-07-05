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
package org.eclipse.che.plugin.csharp.ide.action;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.plugin.csharp.shared.Constants;
import org.eclipse.che.plugin.csharp.ide.CSharpLocalizationConstant;
import org.eclipse.che.plugin.csharp.ide.CSharpResources;

/**
 * Action to create new C# source file.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class CreateCSharpSourceFileAction extends NewCSharplikeResourceAction {


    private static final String DEFAULT_CONTENT = "// A Hello World! program in C#.\n" +
                                                  "using System;\n" +
                                                  "namespace HelloWorld\n" +
                                                  "{\n" +
                                                  "    class Hello \n" +
                                                  "    {\n" +
                                                  "        static void Main() \n" +
                                                  "        {\n" +
                                                  "            Console.WriteLine(\"Hello World!\");\n" +
                                                  "        }\n" +
                                                  "    }\n" +
                                                  "}";

    @Inject
    public CreateCSharpSourceFileAction(CSharpLocalizationConstant localizationConstant,
                                        AppContext appContext,
                                        CSharpResources csharpResources) {
        super(localizationConstant.createCSharpFileActionTitle(),
              localizationConstant.createCSharpFileActionDescription(),
              csharpResources.csharpFile(), appContext);
    }

    @Override
    protected String getExtension() {
        return Constants.CSHARP_EXT;
    }

    @Override
    protected String getDefaultContent() {
        return DEFAULT_CONTENT;
    }
}
