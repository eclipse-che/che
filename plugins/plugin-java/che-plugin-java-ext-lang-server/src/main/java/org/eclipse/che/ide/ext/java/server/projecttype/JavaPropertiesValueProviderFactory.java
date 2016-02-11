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
package org.eclipse.che.ide.ext.java.server.projecttype;

import org.eclipse.che.api.project.server.*;
import org.eclipse.che.ide.ext.java.shared.Constants;

import java.util.Collections;
import java.util.List;

/**
 * Value
 *
 * @author gazarenkov
 */
public class JavaPropertiesValueProviderFactory implements ValueProviderFactory {

    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new JavaPropertiesValueProvider();
    }

    protected class JavaPropertiesValueProvider implements ValueProvider {
        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            if (attributeName.equals(Constants.LANGUAGE_VERSION))
                return Collections.singletonList(System.getProperty("java.version"));
            return null;
        }

        @Override
        public void setValues(String attributeName, List<String> value) throws ValueStorageException, InvalidValueException {

        }
    }
}