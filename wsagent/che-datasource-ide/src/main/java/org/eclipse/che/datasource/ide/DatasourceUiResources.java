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
package org.eclipse.che.datasource.ide;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Client bundle interface for datasource plugin resources.
 */
public interface DatasourceUiResources extends ClientBundle {

    /**
     * The PostgreSQL logo.
     * 
     * @return an image resource for the PostGreSQL logo.
     */
    @Source("postgresql.png")
    ImageResource getPostgreSqlLogo();

    @Source("mysql.png")
    ImageResource getMySqlLogo();

    @Source("sql.svg")
    SVGResource sqlIcon();

    @Source("sqlserver.png")
    ImageResource getSqlServerLogo();

    @Source("oracle.png")
    ImageResource getOracleLogo();

    @Source("nuodb.png")
    ImageResource getNuoDBLogo();

    @Source("google.png")
    ImageResource getGoogleCloudSQLLogo();

    @Source("aws-postgresql.png")
    ImageResource getAwsPostgresLogo();

    @Source("aws-mysql.png")
    ImageResource getAwsMysqlLogo();

    @Source("aws-oracle.png")
    ImageResource getAwsOracleLogo();

    @Source("aws-sqlserver.png")
    ImageResource getAwsSqlServerLogo();

    @Source("refresh.svg")
    SVGResource getRefreshIcon();

    @Source("NewDatasource.svg")
    SVGResource newDatasourceMenuIcon();

    @Source("ManageDatasource.svg")
    SVGResource manageDatasourceMenuIcon();

    @Source({"datasource-ui.css", "org/eclipse/che/ide/api/ui/style.css"})
    DatasourceUiStyle datasourceUiCSS();

    public interface DatasourceUiStyle extends CssResource {

        @ClassName("formField-Label")
        String formFieldLabel();

        @ClassName("explorer-datasourceList")
        String explorerDatasourceList();

        @ClassName("explorer-topPanel")
        String explorerTopPanel();

        @ClassName("explorer-refreshButton")
        String explorerRefreshButton();

        @ClassName("explorer-refreshButton-up")
        String explorerRefreshButtonUp();

        @ClassName("explorer-refreshButton-up-hover")
        String explorerRefreshButtonUpHover();

        @ClassName("explorer-refreshButton-down")
        String explorerRefreshButtonDown();

        @ClassName("explorer-refreshButton-down-hover")
        String explorerRefreshButtonDownHover();

        @ClassName("requestLauncher-editorBar")
        String requestLauncherEditorBar();

        @ClassName("requestLauncher-listBox")
        String requestLauncherListBox();

        @ClassName("resultHeader-clear-button")
        String resultHeaderClearButton();

        @ClassName("requestLauncher-executionModeListBox")
        String requestLauncherExecutionModeListBox();

        @ClassName("requestLauncher-datasourceListBox")
        String requestLauncherDatasourceListBox();

        @ClassName("requestLauncher-textBox")
        String requestLauncherTextBox();

        @ClassName("requestLauncher-resultLimitInput")
        String requestLauncherResultLimitInp();

        @ClassName("requestLauncher-executeButton")
        String requestLauncherExecuteButton();

        @ClassName("requestLauncher-label")
        String requestLauncherLabel();

        @ClassName("requestLauncher-executionModeLabel")
        String requestLauncherExecutionModeLabel();

        @ClassName("requestLauncher-resultLimitLabel")
        String requestLauncherResultLimitLabel();

        @ClassName("requestLauncher-selectDatasourceLabel")
        String requestLauncherSelectDatasourceLabel();

        @ClassName("resultItemHeader-queryReminder")
        String resultItemQueryReminder();

        @ClassName("resultItemHeader-csvButton")
        String resultItemCsvButton();

        @ClassName("resultItemHeader-csvLink")
        String resultItemCsvLink();

        @ClassName("resultZoneHeader-text")
        String resultZoneHeaderText();

        @ClassName("resultZoneHeader-bar")
        String resultZoneHeaderBar();

        @ClassName("resultZoneOutput")
        String resultZoneOutput();

        @ClassName("resultItemHeader-bar")
        String resultItemHeaderBar();

        @ClassName("resultItem")
        String resultItem();

        @ClassName("propertiesTable-firstColumn")
        String propertiesTableFirstColumn();

        @ClassName("propertiesPanel-background")
        String propertiesPanelBackground();

        @ClassName("editDatasourceList-datasourceTypeStyle")
        String datasourceTypeStyle();

        @ClassName("editDatasourceList-datasourceIdStyle")
        String datasourceIdStyle();

        @ClassName("editDatasourceList-datasourceIdCellStyle")
        String datasourceIdCellStyle();

        @ClassName("editDatasourceList-datasourceTypeCellStyle")
        String datasourceTypeCellStyle();

        @ClassName("datasourceWizard-testConnection")
        String datasourceWizardTestConnection();

        @ClassName("datasourceWizard-testConnection-ok")
        String datasourceWizardTestConnectionOK();

        @ClassName("datasourceWizard-testConnection-ko")
        String datasourceWizardTestConnectionKO();
    }
}
