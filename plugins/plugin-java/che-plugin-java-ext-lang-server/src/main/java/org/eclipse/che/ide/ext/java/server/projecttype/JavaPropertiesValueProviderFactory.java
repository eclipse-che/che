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

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.ide.ext.java.shared.Constants;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;

/**
 * {@link ValueProviderFactory} for Java project type.
 *
 * @author gazarenkov
 * @author Artem Zatsarynnyi
 */
public class JavaPropertiesValueProviderFactory implements ValueProviderFactory {

    private final String javaVersion;

    @Inject
    public JavaPropertiesValueProviderFactory(@Named("sys.java.version") String javaVersion) {
        this.javaVersion = javaVersion;
    }

    @Override
    public ValueProvider newInstance(FolderEntry projectFolder) {
        return new JavaPropertiesValueProvider();
    }

    protected class JavaPropertiesValueProvider implements ValueProvider {
        @Override
        public List<String> getValues(String attributeName) throws ValueStorageException {
            if (attributeName.equals(Constants.LANGUAGE_VERSION))
                return Collections.singletonList(javaVersion);
            return null;
        }
    }
}
