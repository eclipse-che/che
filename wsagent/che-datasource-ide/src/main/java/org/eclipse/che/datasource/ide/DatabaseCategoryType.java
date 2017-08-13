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
package org.eclipse.che.datasource.ide;

/**
 * Created by Wafa on 28/03/14.
 */
public enum DatabaseCategoryType {
    NOTCLOUD ("HOSTED DATABASE","hosted_database"),
    GOOGLE ("GOOGLE","google"),
    AMAZON ("AMAZON","amazon");
    
    private final String id;
    private final String label;

    /**
     * @param text
     */
    private DatabaseCategoryType(final String id, final String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public String toString() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
}
