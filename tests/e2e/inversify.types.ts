
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
    WorkspaceUtil: Symbol.for('WorkspaceUtil'),
    IAuthorizationHeaderHandler: Symbol.for('IAuthorizationHeaderHandler'),
    ITokenHandler: Symbol.for('ITokenHandler')


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
    RightToolBar: 'RightToolBar',
    LeftToolBar: 'LeftToolBar',
    Terminal: 'Terminal',
    DebugView: 'DebugView',
    DialogWindow: 'DialogWindow',
    ScreenCatcher: 'ScreenCatcher',
    OpenshiftPlugin: 'OpenshiftPlugin',
    OcpLoginPage: 'OcpLoginPage',
    OpenWorkspaceWidget: 'OpenWorkspaceWidget',
    ContextMenu: 'ContextMenu',
    CheLoginPage: 'CheLoginPage',
    GitHubUtil: 'GitHubUtil',
    CheGitApi: 'CheGitApi',
    GitPlugin: 'GitPlugin',
    NotificationCenter: 'NotificationCenter',
    PreferencesHandler: 'PreferencesHandler',
    CheApiRequestHandler: 'CheApiRequestHandler',
    CreateWorkspace: 'CreateWorkspace',
    OpenDialogWidget: 'OpenDialogWidget',
    UpdateAccountInformationPage: 'UpdateAccountInformationPage',
    KubernetesPlugin: 'KubernetesPlugin',
    BrowserTabsUtil: 'BrowserTabsUtil',
    LanguageServerTests: 'LanguageServerTests',
    CodeExecutionTests: 'CodeExecutionTests',
    ProjectAndFileTests: 'ProjectAndFileTests'
};

export { TYPES, CLASSES };
