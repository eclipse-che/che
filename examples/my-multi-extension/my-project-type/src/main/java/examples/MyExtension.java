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
package examples;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import static examples.MyAttributes.My_PROJECT_TYPE_CATEGORY;

@Singleton
@Extension(title = "My Project Type Extension", version = "1.0.0")
public class MyExtension {

    @Inject
    public MyExtension(MyResources resources, IconRegistry iconRegistry) {
        iconRegistry.registerIcon(new Icon(My_PROJECT_TYPE_CATEGORY + ".samples.category.icon",
                resources.MyProjectTypeIcon()));
    }
}
