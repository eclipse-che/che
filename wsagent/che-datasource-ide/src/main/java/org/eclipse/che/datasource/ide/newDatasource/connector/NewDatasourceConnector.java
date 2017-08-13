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
package org.eclipse.che.datasource.ide.newDatasource.connector;

import com.google.gwt.resources.client.ImageResource;
import com.google.inject.Provider;

import org.eclipse.che.ide.collections.Array;
import org.eclipse.che.datasource.ide.DatabaseCategoryType;

import java.util.List;


public class NewDatasourceConnector implements Comparable<NewDatasourceConnector> {

    private final int                                                            priority;
    private final String                                                         id;
    private final String                                                         title;
    private final ImageResource                                                  image;
    private final String                                                         jdbcClassName;
    private final List<Provider< ? extends AbstractNewDatasourceConnectorPage>> wizardPages;

    private final DatabaseCategoryType                                           categoryType;

    public NewDatasourceConnector(final String connectorId,
                                  final int priority,
                                  final String title,
                                  final ImageResource logo,
                                  final String jdbcClassName,
                                  final List<Provider< ? extends AbstractNewDatasourceConnectorPage>> wizardPages,
                                  final DatabaseCategoryType categoryType) {
        this.id = connectorId;
        this.priority = priority;
        this.title = title;
        this.image = logo;
        this.jdbcClassName = jdbcClassName;
        this.wizardPages = wizardPages;
        this.categoryType = categoryType;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ImageResource getImage() {
        return image;
    }

    public String getJdbcClassName() {
        return jdbcClassName;
    }

    public List<Provider< ? extends AbstractNewDatasourceConnectorPage>> getWizardPages() {
        return wizardPages;
    }

    public DatabaseCategoryType getCategoryType() {
        return categoryType;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NewDatasourceConnector other = (NewDatasourceConnector)obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public int compareTo(final NewDatasourceConnector o) {
        return new Integer(this.priority).compareTo(o.priority);
    }
}
