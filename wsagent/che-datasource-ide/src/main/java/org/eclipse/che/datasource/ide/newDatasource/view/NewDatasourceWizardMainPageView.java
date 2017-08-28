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
package org.eclipse.che.datasource.ide.newDatasource.view;

import com.google.inject.ImplementedBy;

import org.eclipse.che.datasource.ide.newDatasource.connector.NewDatasourceConnector;
import org.eclipse.che.ide.api.mvp.View;

import java.util.Collection;

@ImplementedBy(NewDatasourceWizardMainPageViewImpl.class)
public interface NewDatasourceWizardMainPageView extends View<NewDatasourceWizardMainPageView.ActionDelegate> {

    void reset();

    void setConnectors(Collection<NewDatasourceConnector> connectors);
    
    void selectConnector(NewDatasourceConnector data);

    /** Required for delegating functions in view. */
    public interface ActionDelegate {

        void onConnectorSelected(String id);

        void onCategorySelected();

        boolean connectorEnabled(String id);
    }
}
