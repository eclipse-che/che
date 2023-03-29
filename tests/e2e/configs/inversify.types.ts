/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
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
    WorkspaceDetails: 'WorkspaceDetails',
    ScreenCatcher: 'ScreenCatcher',
    OcpLoginPage: 'OcpLoginPage',
    CheLoginPage: 'CheLoginPage',
    CheApiRequestHandler: 'CheApiRequestHandler',
    CreateWorkspace: 'CreateWorkspace',
    BrowserTabsUtil: 'BrowserTabsUtil',
    ProjectAndFileTests: 'ProjectAndFileTests',
    Sanitizer: 'Sanitizer',
    ApiUrlResolver: 'ApiUrlResolver',
    LoginTests: 'LoginTests',
    WorkspaceHandlingTests: 'WorkspaceHandlingTests',
    RedHatLoginPage: 'RedHatLoginPage',
    OcpRedHatLoginPage: 'OcpRedHatLoginPage',
};

export { TYPES, CLASSES };
