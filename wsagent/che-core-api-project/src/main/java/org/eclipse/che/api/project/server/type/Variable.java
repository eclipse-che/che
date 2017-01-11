/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
 * Attribute which value can be changed
 * @author gazarenkov
 */
public class Variable extends AbstractAttribute {

    protected ValueProviderFactory valueProviderFactory;
    protected AttributeValue       value;

    /**
     * Constructor for Value Provided Variable
     * @param projectType project type
     * @param name attribute name
     * @param description description
     * @param required if required
     * @param valueProviderFactory factory
     */
    public Variable(String projectType, String name, String description, boolean required, ValueProviderFactory valueProviderFactory) {
        this(projectType, name, description, required);
        this.valueProviderFactory = valueProviderFactory;
    }

    /**
     * Constructor for persisted value
     * @param projectType project type
     * @param name attribute name
     * @param description description
     * @param required if required
     * @param value attribute value
     */
    public Variable(String projectType, String name, String description, boolean required, AttributeValue value) {
        this(projectType, name, description, required);
        this.value = value;
    }

    public Variable(String projectType, String name, String description, boolean required) {
        super(projectType, name, description, required, true);
    }

    @Override
    public final AttributeValue getValue() {
        return value;
    }

    /**
     * @return whether the value provided externally using ValueProviderFactory
     */
    public final boolean isValueProvided() {
        return valueProviderFactory != null;
    }


    /**
     * @return value provider factory or null if not provided
     */
    public final ValueProviderFactory getValueProviderFactory() {
        return valueProviderFactory;
    }


//    /**
//     * @deprecated use getValueProviderFactory()..newInstance(projectFolder).getValue() instead
//     * @param projectFolder
//     * @return
//     * @throws ValueStorageException
//     */
//    public final AttributeValue getValue(FolderEntry projectFolder) throws ValueStorageException {
//        if (valueProviderFactory != null) {
//            return new AttributeValue(valueProviderFactory.newInstance(projectFolder).getValues(getName()));
//        } else {
//            return value;
//        }
//    }


}
