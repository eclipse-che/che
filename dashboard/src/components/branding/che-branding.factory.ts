/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheService} from '../api/che-service.factory';

interface IBranding {
  title?: string;
  name?: string;
  logoFile?: string;
  logoTextFile?: string;
  favicon?: string;
  loader?: string;
  ideResources?: string;
  websocketContext?: string;
  helpPath?: string;
  helpTitle?: string;
  supportEmail?: string;
  oauthDocs?: string;
  cli?: {
    configName?: string;
    name?: string;
  };
  docs?: {
    stack?: string;
    workspace?: string;
    factory?: string;
    organization?: string;
    general?: string;
  };
  workspace?: {
    priorityStacks?: Array<string>;
    defaultStack?: string;
    creationLink?: string;
  };
  footer?: {
    content?: string;
    links?: Array<{title: string, location: string}>;
    email?: {title: string, address: string, subject: string};
  };
}

const ASSET_PREFIX = 'assets/branding/';
const DEFAULT_PRODUCT_NAME = 'Eclipse Che';
const DEFAULT_NAME = 'Eclipse Che';
const DEFAULT_PRODUCT_FAVICON = 'favicon.ico';
const DEFAULT_LOADER = 'loader.svg';
const DEFAULT_PRODUCT_LOGO = 'che-logo.svg';
const DEFAULT_PRODUCT_LOGO_TEXT = 'che-logo-text.svg';
const DEFAULT_IDE_RESOURCES_PATH = '/_app/';
const DEFAULT_OAUTH_DOCS = 'Configure OAuth in the che.properties file.';
const DEFAULT_CLI_NAME = 'che.env';
const DEFAULT_CLI_CONFIG_NAME = 'CHE';
const DEFAULT_DOCS_STACK = '/docs/getting-started/runtime-stacks/index.html';
const DEFAULT_DOCS_WORKSPACE = '/docs/getting-started/intro/index.html';
const DEFAULT_DOCS_ORGANIZATION = '/docs/organizations.html';
const DEFAULT_DOCS_FACTORY = '/docs/factories-getting-started.html';
const DEFAULT_DOCS_GENERAL = '/docs';
const DEFAULT_WORKSPACE_PRIORITY_STACKS = ['Java', 'Java-MySQL', 'Blank'];
const DEFAULT_WORKSPACE_DEFAULT_STACK = 'java-mysql';
const DEFAULT_WORKSPACE_CREATION_LINK = '#/create-workspace';
const DEFAULT_WEBSOCKET_CONTEXT = '/api/websocket';

/**
 * This class is handling the branding data in Che.
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CheBranding {

  static $inject = ['$http', '$rootScope', 'cheService'];

  private $rootScope: che.IRootScopeService;
  private $http: ng.IHttpService;
  private cheService: CheService;
  private brandingData: IBranding;
  private callbacks: Map<string, Function> = new Map();

  /**
   * Default constructor that is using resource
   */
  constructor($http: ng.IHttpService, $rootScope: che.IRootScopeService, cheService: CheService) {
    this.$http = $http;
    this.$rootScope = $rootScope;
    this.cheService = cheService;
    this.brandingData = {};
    this.updateData();
    this.updateVersion();
  }

  /**
   * Update product version.
   */
  updateVersion(): void {
    this.cheService.fetchServicesInfo().then(() => {
      let info = this.cheService.getServicesInfo();
      this.$rootScope.productVersion = (info && info.implementationVersion) ? info.implementationVersion : '';
    });
  }

  /**
   * Update branding data.
   */
  updateData(): void {
    this.$http.get(ASSET_PREFIX + 'product.json').then((branding: { data: any }) => {
      return branding && branding.data ? branding.data : {};
    }, () => {
      return {};
    }).then((brandingData: IBranding) => {
      this.brandingData = brandingData;
      this.$rootScope.branding = {
        title: this.getProductName(),
        name: this.getName(),
        logoURL: this.getProductLogo(),
        logoText: this.getProductLogoText(),
        favicon: this.getProductFavicon(),
        loaderURL: this.getLoaderUrl(),
        ideResourcesPath: this.getIdeResourcesPath(),
        websocketContext: this.getWebsocketContext(),
        helpPath: this.getProductHelpPath(),
        helpTitle: this.getProductHelpTitle(),
        footer: this.getFooter(),
        supportEmail: this.getProductSupportEmail(),
        oauthDocs: this.getOauthDocs(),
        cli: this.getCLI(),
        docs: this.getDocs(),
        workspace: this.getWorkspace()
      };
      this.callbacks.forEach((callback: Function) => {
        if (angular.isFunction(callback)) {
          callback(this.$rootScope.branding);
        }
      });
    });
  }

  /**
   * Registers a callback function.
   * @param callbackId {string}
   * @param callback {Function}
   */
  registerCallback(callbackId: string, callback: Function): void {
    this.callbacks.set(callbackId, callback);
    if (this.$rootScope.branding) {
      callback(this.$rootScope.branding);
    }
  }

  /**
   * Unregisters the callback function by Id.
   * @param callbackId {string}
   */
  unregisterCallback(callbackId: string): void {
    if (!this.callbacks.has(callbackId)) {
      return;
    }
    this.callbacks.delete(callbackId);
  }

  /**
   * Gets name.
   * @returns {string}
   */
  getName(): string {
    return this.brandingData.name ? this.brandingData.name : DEFAULT_NAME;
  }

  /**
   * Gets product name.
   * @returns {string}
   */
  getProductName(): string {
    return  this.brandingData.title ? this.brandingData.title : DEFAULT_PRODUCT_NAME;
  }

  /**
   * Gets product logo.
   * @returns {string}
   */
  getProductLogo(): string {
    return this.brandingData.logoFile ? ASSET_PREFIX + this.brandingData.logoFile : ASSET_PREFIX + DEFAULT_PRODUCT_LOGO;
  }

  /**
   * Gets product favicon.
   * @returns {string}
   */
  getProductFavicon(): string {
    return this.brandingData.favicon ? ASSET_PREFIX + this.brandingData.favicon : ASSET_PREFIX + DEFAULT_PRODUCT_FAVICON;
  }

  /**
   * Gets product loader.
   * @returns {string}
   */
  getLoaderUrl(): string {
    return this.brandingData.loader ? ASSET_PREFIX + this.brandingData.loader : ASSET_PREFIX + DEFAULT_LOADER;
  }

  /**
   * Gets ide resources path.
   * @returns {string}
   */
  getIdeResourcesPath(): string {
    return this.brandingData.ideResources ? this.brandingData.ideResources : DEFAULT_IDE_RESOURCES_PATH;
  }

  /**
   * Gets ide resources path.
   * @returns {string}
   */
  getWebsocketContext(): string {
    return this.brandingData.websocketContext ? this.brandingData.websocketContext : DEFAULT_WEBSOCKET_CONTEXT;
  }

  /**
   * Gets product help path.
   * @returns {string}
   */
  getProductHelpPath(): string {
    return this.brandingData.helpPath ? this.brandingData.helpPath : null;
  }

  /**
   * Gets product help title.
   * @returns {string}
   */
  getProductHelpTitle(): string {
    return this.brandingData.helpTitle ? this.brandingData.helpTitle : null;
  }

  /**
   * Gets product logo text.
   * @returns {string}
   */
  getProductLogoText(): string {
    return this.brandingData.logoTextFile ? ASSET_PREFIX + this.brandingData.logoTextFile : ASSET_PREFIX + DEFAULT_PRODUCT_LOGO_TEXT;
  }

  /**
   * Gets oauth docs.
   * @returns {string}
   */
  getOauthDocs(): string {
    return this.brandingData.oauthDocs ? this.brandingData.oauthDocs : DEFAULT_OAUTH_DOCS;
  }

  /**
   * Gets product support email.
   * @returns {string}
   */
  getProductSupportEmail(): string {
    return this.brandingData.supportEmail ? this.brandingData.supportEmail : null;
  }

  /**
   * Returns footer additional elements (email button, content, button links).
   *
   * @returns {any} additional elements (email button, content, button links).
   */
  getFooter():  {content?: string; links?: Array<{title: string, location: string}>; email: {title: string, address: string, subject: string}} {
    return {
      content: this.brandingData.footer && this.brandingData.footer.content ? this.brandingData.footer.content : '',
      links: this.brandingData.footer && this.brandingData.footer.links ? this.brandingData.footer.links : [],
      email: this.brandingData.footer && this.brandingData.footer.email ? this.brandingData.footer.email : null
    };
  }

  /**
   * Returns object with configName and name.
   * @returns {{configName: string, name: string}}
   */
  getCLI(): { configName: string; name: string } {
    return {
      configName: this.brandingData.cli && this.brandingData.cli.configName ? this.brandingData.cli.configName : DEFAULT_CLI_CONFIG_NAME,
      name: this.brandingData.cli && this.brandingData.cli.name ? this.brandingData.cli.name : DEFAULT_CLI_NAME
    };
  }

  /**
   * Returns object with docs URLs.
   * @returns {{stack: string, workspace: string, factory: string, organization: string, general: string}}
   */
  getDocs(): { stack: string; workspace: string; factory: string; organization: string; general: string } {
    return {
      stack: this.brandingData.docs && this.brandingData.docs.stack ? this.brandingData.docs.stack : DEFAULT_DOCS_STACK,
      workspace: this.brandingData.docs && this.brandingData.docs.workspace ? this.brandingData.docs.workspace : DEFAULT_DOCS_WORKSPACE,
      factory: this.brandingData.docs && this.brandingData.docs.factory ? this.brandingData.docs.factory : DEFAULT_DOCS_FACTORY,
      organization: this.brandingData.docs && this.brandingData.docs.organization ? this.brandingData.docs.organization : DEFAULT_DOCS_ORGANIZATION,
      general: this.brandingData.docs && this.brandingData.docs.general ? this.brandingData.docs.general : DEFAULT_DOCS_GENERAL
    };
  }

  /**
   * Returns object with workspace dedicated data.
   * @returns {{stack: string, workspace: string}}
   */
  getWorkspace(): { priorityStacks: Array<string>; defaultStack: string, creationLink: string} {
    return {
      priorityStacks: this.brandingData.workspace && this.brandingData.workspace.priorityStacks ? this.brandingData.workspace.priorityStacks : DEFAULT_WORKSPACE_PRIORITY_STACKS,
      defaultStack: this.brandingData.workspace && this.brandingData.workspace.defaultStack ? this.brandingData.workspace.defaultStack : DEFAULT_WORKSPACE_DEFAULT_STACK,
      creationLink: this.brandingData.workspace && this.brandingData.workspace.creationLink ? this.brandingData.workspace.creationLink : DEFAULT_WORKSPACE_CREATION_LINK
    };
  }
}
