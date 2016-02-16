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

import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ValueStorageException;


/**
 * @author gazarenkov
 */
public class Variable extends AbstractAttribute {

    protected ValueProviderFactory valueProviderFactory = null;
    protected AttributeValue       value                = null;


    public Variable(String projectType, String name, String description, boolean required,
                    ValueProviderFactory valueProviderFactory) {
        this(projectType, name, description, required);
        this.valueProviderFactory = valueProviderFactory;
    }


    public Variable(String projectType, String name, String description, boolean required, AttributeValue value) {
        this(projectType, name, description, required);
        this.value = value;
    }

    public Variable(String projectType, String name, String description, boolean required) {
        super(projectType, name, description, required, true);
    }

    @Override
    public AttributeValue getValue() {
        return value;
    }


    public final AttributeValue getValue(FolderEntry projectFolder) throws ValueStorageException {
        if (valueProviderFactory != null) {
            return new AttributeValue(valueProviderFactory.newInstance(projectFolder).getValues(getName()));
        } else {
            return value;
        }
    }

//    public final void setValue(AttributeValue value, FolderEntry projectFolder) throws InvalidValueException, ValueStorageException {
//        if (valueProviderFactory != null) {
//            this.valueProviderFactory.newInstance(projectFolder).setValues(getName(), value.getList());
//        } else
//            this.value = value;
//    }

    public final ValueProviderFactory getValueProviderFactory() {
        return valueProviderFactory;
    }


}
