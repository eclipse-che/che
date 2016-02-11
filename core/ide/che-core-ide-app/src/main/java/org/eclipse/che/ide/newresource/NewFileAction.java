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
package org.eclipse.che.ide.newresource;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Action to create new file.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NewFileAction extends AbstractNewResourceAction {
    @Inject
    public NewFileAction(CoreLocalizationConstant localizationConstant, Resources resources) {
        super(localizationConstant.actionNewFileTitle(),
              localizationConstant.actionNewFileDescription(),
              resources.defaultFile());
    }
}
