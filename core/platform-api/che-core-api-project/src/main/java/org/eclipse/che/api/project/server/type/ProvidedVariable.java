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
package org.eclipse.che.api.project.server.type;

/**
 * @author gazarenkov
 */
public class ProvidedVariable extends Variable {

    protected ValueProviderFactory valueProviderFactory = null;

    public ProvidedVariable(String projectType, String name, String description, boolean required,
                            ValueProviderFactory valueProviderFactory) {
        super(projectType, name, description, required);
        this.valueProviderFactory = valueProviderFactory;
    }
}
