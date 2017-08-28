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
package org.eclipse.che.datasource.ide.newDatasource;

import com.google.gwt.i18n.client.Messages;

public interface NewDatasourceWizardMessages extends Messages {

    @DefaultMessage("New Datasource Connection")
    String newDatasourceMenuText();

    @DefaultMessage("Create a new datasource connection")
    String newDatasourceMenuDescription();

    @DefaultMessage("Datasource Name:")
    String datasourceName();

    @DefaultMessage("Name:")
    String datasourceOnlyName();


    @DefaultMessage("New Datasource")
    String wizardTitle();

    @DefaultMessage("Create a new datasource connection for your project to execute your sql queries and explore results")
    String wizardDescription();

    @DefaultMessage("An error occured while creating the datasource connection")
    String defaultNewDatasourceWizardErrorMessage();

    @DefaultMessage("Establishing Database Connection...")
    String startConnectionTest();

    @DefaultMessage("Succesfully connected to database")
    String connectionTestSuccessNotification();

    @DefaultMessage("Failed to connect to database")
    String connectionTestFailureSuccessNotification();

    @DefaultMessage("Connection succeeded")
    String connectionTestSuccessMessage();

    @DefaultMessage("Connection failed")
    String connectionTestFailureSuccessMessage();

    @DefaultMessage("Select the type of Datasource you want to connect to:")
    String selectDatasourceType();

    @DefaultMessage("New Datasource")
    String newDatasource();

    @DefaultMessage("PostgreSQL")
    String postgresql();

    @DefaultMessage("MySQL")
    String mysql();

    @DefaultMessage("Oracle")
    String oracle();

    @DefaultMessage("MS SQL Server")
    String mssqlserver();

    @DefaultMessage("NuoDB")
    String nuodb();

    @DefaultMessage("Google Cloud SQL")
    String googlecloudsql();

    @DefaultMessage("PostgreSQL on AWS")
    String awspg();

    @DefaultMessage("MySQL on AWS")
    String awsmysql();

    @DefaultMessage("Oracle on AWS")
    String awsoracle();

    @DefaultMessage("MS SQL Server on AWS")
    String awssqlserver();
}
