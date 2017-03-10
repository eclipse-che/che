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
package org.eclipse.che.plugin.urlfactory;

/**
 * Parameters holder when wanting to create a factory
 * @author Florent Benoit
 */
public class CreateFactoryParams {

    /**
     * Location (url) to the json file.
     */
    private String codenvyJsonFileLocation;

    /**
     * Use the create method
     */
    private CreateFactoryParams() {
    }

    /**
     * Creates and returns arguments holder.
     */
    public static CreateFactoryParams create() {
        return new CreateFactoryParams();
    }


    /**
     * Defines the path to the codenvy json file location
     * @param codenvyJsonFileLocation the url to grab the json file location
     * @return the current instance.
     */
    public CreateFactoryParams codenvyJsonFileLocation(String codenvyJsonFileLocation) {
        this.codenvyJsonFileLocation = codenvyJsonFileLocation;
        return this;
    }

    /**
     * Defines the path to the codenvy json file location
     * @return the url to grab the json file location
     */
    public String codenvyJsonFileLocation() {
        return this.codenvyJsonFileLocation;
    }

}
