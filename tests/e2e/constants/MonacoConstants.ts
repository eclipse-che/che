/*********************************************************************
 * Copyright (c) 2020-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
export const MonacoConstants: any = {
  /**
   * Base version of VSCode editor for monaco-page-objects, "1.37.0" by default.
   */
  TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION: process.env.TS_SELENIUM_MONACO_PAGE_OBJECTS_BASE_VERSION || '1.37.0',

  /**
   * Latest compatible version to be used, based on versions available in
   * https://github.com/redhat-developer/vscode-extension-tester/tree/master/locators/lib ,
   * "1.73.0" by default.
   */
  TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION: process.env.TS_SELENIUM_MONACO_PAGE_OBJECTS_USE_VERSION || '1.73.0'
};
