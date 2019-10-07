
/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

const TYPES = {
    Driver: Symbol.for('Driver'),
    CheLogin: Symbol.for('CheLogin'),
    OcpLogin: Symbol.for('OcpLogin'),
    WorkspaceUtil: Symbol.for('WorkspaceUtil')
};

const CLASSES = {
    DriverHelper: 'DriverHelper',
    Dashboard: 'Dashboard',
    Workspaces: 'Workspaces',
    NewWorkspace: 'NewWorkspace',
    WorkspaceDetails: 'WorkspaceDetails',
    WorkspaceDetailsPlugins: 'WorkspaceDetailsPlugins',
    Ide: 'Ide',
    ProjectTree: 'ProjectTree',
    Editor: 'Editor',
    TopMenu: 'TopMenu',
    QuickOpenContainer: 'QuickOpenContainer',
    PreviewWidget: 'PreviewWidget',
    GitHubPlugin: 'GitHubPlugin',
    RightToolbar: 'RightToolbar',
    Terminal: 'Terminal',
    DebugView: 'DebugView',
    WarningDialog: 'WarningDialog',
    ScreenCatcher: 'ScreenCatcher',
    OcpLoginPage: 'OcpLoginPage',
    OcpWebConsolePage: 'OcpWebConsolePage',
    OpenWorkspaceWidget: 'OpenWorkspaceWidget',
    ContextMenu: 'ContextMenu'
};

export { TYPES, CLASSES };
