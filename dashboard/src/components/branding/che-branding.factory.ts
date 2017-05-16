/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
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
const DEFAULT_HELP_PATH = 'https://www.eclipse.org/che/';
const DEFAULT_HELP_TITLE = 'Community';
const DEFAULT_SUPPORT_EMAIL = 'wish@codenvy.com';
const DEFAULT_OAUTH_DOCS = 'Configure OAuth in the che.properties file.';
const DEFAULT_CLI_NAME = 'che.env';
const DEFAULT_CLI_CONFIG_NAME = 'CHE';
const DEFAULT_DOCS_STACK = '/docs/getting-started/runtime-stacks/index.html';
const DEFAULT_DOCS_WORKSPACE = '/docs/getting-started/intro/index.html';


/**
 * This class is handling the branding data in Che.
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CheBranding {
  private $rootScope: che.IRootScopeService;
  private $http: ng.IHttpService;
  private cheService: CheService;
  private brandingData: IBranding;
  private callbacks: Map<string, Function> = new Map();

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($http: ng.IHttpService, $rootScope: che.IRootScopeService, cheService: CheService) {
    this.$http = $http;
    this.$rootScope = $rootScope;
    this.cheService = cheService;
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
        helpPath: this.getProductHelpPath(),
        helpTitle: this.getProductHelpTitle(),
        supportEmail: this.getProductSupportEmail(),
        oauthDocs: this.getOauthDocs(),
        cli: this.getCLI(),
        docs: this.getDocs()
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
   * Gets product help path.
   * @returns {string}
   */
  getProductHelpPath(): string {
    return this.brandingData.helpPath ? this.brandingData.helpPath : DEFAULT_HELP_PATH;
  }

  /**
   * Gets product help title.
   * @returns {string}
   */
  getProductHelpTitle(): string {
    return this.brandingData.helpTitle ? this.brandingData.helpTitle : DEFAULT_HELP_TITLE;
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
    return this.brandingData.supportEmail ? this.brandingData.supportEmail : DEFAULT_SUPPORT_EMAIL;
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
   * @returns {{stack: string, workspace: string}}
   */
  getDocs(): { stack: string; workspace: string } {
    return {
      stack: this.brandingData.docs && this.brandingData.docs.stack ? this.brandingData.docs.stack : DEFAULT_DOCS_STACK,
      workspace: this.brandingData.docs && this.brandingData.docs.workspace ? this.brandingData.docs.workspace : DEFAULT_DOCS_WORKSPACE
    };
  }
}

