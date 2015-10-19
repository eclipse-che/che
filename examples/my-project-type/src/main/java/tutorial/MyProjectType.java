/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package tutorial;

import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.ide.Constants;

import com.google.inject.Singleton;

import static tutorial.MyAttributes.My_PROJECT_TYPE_ID;
import static tutorial.MyAttributes.My_PROJECT_TYPE_NAME;
import static tutorial.MyAttributes.PROGRAMMING_LANGUAGE;

@Singleton
public class MyProjectType extends ProjectType {

    public MyProjectType() {
        super(My_PROJECT_TYPE_ID, My_PROJECT_TYPE_NAME, true, false);
        addConstantDefinition(Constants.LANGUAGE, "language", PROGRAMMING_LANGUAGE);
    }
}
