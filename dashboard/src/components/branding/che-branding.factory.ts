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
  websocketContext?: string;
  helpPath?: string;
  helpTitle?: string;
  supportEmail?: string;
  oauthDocs?: string;
  cli?: {
    configName?: string;
    name?: string;
  };
  docs?: IBrandingDocs;
  workspace?: IBrandingWorkspace;
  footer?: IBrandingFooter;
  configuration?: IBrandingConfiguration;
}

interface IBrandingDocs {
  devfile?: string;
  workspace?: string;
  factory?: string;
  organization?: string;
  general?: string;
  converting?: string;
  faq?: string;
}
interface IBrandingWorkspace {
  priorityStacks?: Array<string>;
  defaultStack?: string;
  creationLink?: string;
}
interface IBrandingFooter {
  content?: string;
  links?: Array<{ title: string, location: string }>;
  email?: { title: string, address: string, subject: string };
}
interface IBrandingConfiguration {
  menu: {
    disabled: che.ConfigurableMenuItem[];
  };
  prefetch: {
    cheCDN: string;
    resources: string[];
  };
  features: {
    disabled: TogglableFeature[];
  };
}

export enum TogglableFeature {
  WORKSPACE_SHARING = 'workspaceSharing',
  KUBERNETES_NAMESPACE_SELECTOR = 'kubernetesNamespaceSelector',
}

const ASSET_PREFIX = 'assets/branding/';
const DEFAULT_PRODUCT_NAME = 'Eclipse Che';
const DEFAULT_NAME = 'Eclipse Che';
const DEFAULT_PRODUCT_FAVICON = 'favicon.ico';
const DEFAULT_LOADER = 'loader.svg';
const DEFAULT_PRODUCT_LOGO = 'che-logo.svg';
const DEFAULT_PRODUCT_LOGO_TEXT = 'che-logo-text.svg';
const DEFAULT_OAUTH_DOCS = 'Configure OAuth in the che.properties file.';
const DEFAULT_CLI_NAME = 'che.env';
const DEFAULT_CLI_CONFIG_NAME = 'CHE';
const DEFAULT_DOCS_DEVFILE = '/docs/che-7/making-a-workspace-portable-using-a-devfile/';
const DEFAULT_DOCS_WORKSPACE = '/docs/che-7/workspaces-overview/';
const DEFAULT_DOCS_ORGANIZATION = '/docs/organizations.html';
const DEFAULT_DOCS_FACTORY = '/docs/factories-getting-started.html';
const DEFAULT_DOCS_GENERAL = '/docs/che-7';
const DEFAULT_DOCS_CONVERTING = '/docs/che-7/converting-a-che-6-workspace-to-a-che-7-devfile/';
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

  static $inject = [
    '$http',
    '$q',
    '$rootScope',
    'cheService'
  ];

  private $http: ng.IHttpService;
  private $q: ng.IQService;
  private $rootScope: che.IRootScopeService;
  private cheService: CheService;

  private branding: IBranding;
  private callbacks: Map<string, Function> = new Map();
  private _readyDeferred: ng.IDeferred<void>;

  /**
   * Default constructor that is using resource
   */
  constructor(
    $http: ng.IHttpService,
    $q: ng.IQService,
    $rootScope: che.IRootScopeService,
    cheService: CheService
  ) {
    this.$http = $http;
    this.$q = $q;
    this.$rootScope = $rootScope;
    this.cheService = cheService;

    this.branding = {};
    this._readyDeferred = this.$q.defer();

    this.initialize();
    this.updateData();
    this.updateVersion();
  }

  get ready(): ng.IPromise<void> {
    return this._readyDeferred.promise;
  }

  initialize(): void {
    this.$rootScope.branding = {
      title: this.getProductName(),
      name: this.getName(),
      logoURL: this.getProductLogo(),
      logoText: this.getProductLogoText(),
      favicon: this.getProductFavicon(),
      loaderURL: this.getLoaderUrl(),
      websocketContext: this.getWebsocketContext(),
      helpPath: this.getProductHelpPath(),
      helpTitle: this.getProductHelpTitle(),
      footer: this.getFooter(),
      supportEmail: this.getProductSupportEmail(),
      oauthDocs: this.getOauthDocs(),
      cli: this.getCLI(),
      docs: this.getDocs(),
      workspace: this.getWorkspace(),
      configuration: this.getConfiguration(),
    };
  }

  /**
   * Update product version.
   */
  updateVersion(): void {
    this.cheService.fetchServicesInfo().then(() => {
      let info = this.cheService.getServicesInfo();
      this.$rootScope.productVersion = (info && info.buildInfo) ? info.buildInfo : '';
    });
  }

  /**
   * Update branding data.
   */
  updateData(): void {
    this.$http.get(ASSET_PREFIX + 'product.json').then(res => res.data).then((branding: IBranding) => {
      this.branding = branding;
      this.initialize();
      this.callbacks.forEach(callback => {
        if (angular.isFunction(callback)) {
          callback(this.$rootScope.branding);
        }
      });
      this._readyDeferred.resolve();
    });
  }

  /**
   * Registers a callback function.
   * @param callbackId {string}
   * @param callback {Function}
   */
  registerCallback(callbackId: string, callback: Function): void {
    this.callbacks.set(callbackId, callback);
    if (Object.keys(this.branding).length !== 0) {
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
    return this.branding.name ? this.branding.name : DEFAULT_NAME;
  }

  /**
   * Gets product name.
   * @returns {string}
   */
  getProductName(): string {
    return  this.branding.title ? this.branding.title : DEFAULT_PRODUCT_NAME;
  }

  /**
   * Gets product logo.
   * @returns {string}
   */
  getProductLogo(): string {
    return this.branding.logoFile ? ASSET_PREFIX + this.branding.logoFile : ASSET_PREFIX + DEFAULT_PRODUCT_LOGO;
  }

  /**
   * Gets product favicon.
   * @returns {string}
   */
  getProductFavicon(): string {
    return this.branding.favicon ? ASSET_PREFIX + this.branding.favicon : ASSET_PREFIX + DEFAULT_PRODUCT_FAVICON;
  }

  /**
   * Gets product loader.
   * @returns {string}
   */
  getLoaderUrl(): string {
    return this.branding.loader ? ASSET_PREFIX + this.branding.loader : ASSET_PREFIX + DEFAULT_LOADER;
  }

  /**
   * Gets ide resources path.
   * @returns {string}
   */
  getWebsocketContext(): string {
    return this.branding.websocketContext ? this.branding.websocketContext : DEFAULT_WEBSOCKET_CONTEXT;
  }

  /**
   * Gets product help path.
   * @returns {string}
   */
  getProductHelpPath(): string {
    return this.branding.helpPath ? this.branding.helpPath : null;
  }

  /**
   * Gets product help title.
   * @returns {string}
   */
  getProductHelpTitle(): string {
    return this.branding.helpTitle ? this.branding.helpTitle : null;
  }

  /**
   * Gets product logo text.
   * @returns {string}
   */
  getProductLogoText(): string {
    return this.branding.logoTextFile ? ASSET_PREFIX + this.branding.logoTextFile : ASSET_PREFIX + DEFAULT_PRODUCT_LOGO_TEXT;
  }

  /**
   * Gets oauth docs.
   * @returns {string}
   */
  getOauthDocs(): string {
    return this.branding.oauthDocs ? this.branding.oauthDocs : DEFAULT_OAUTH_DOCS;
  }

  /**
   * Gets product support email.
   * @returns {string}
   */
  getProductSupportEmail(): string {
    return this.branding.supportEmail ? this.branding.supportEmail : null;
  }

  /**
   * Returns footer additional elements (email button, content, button links).
   */
  getFooter(): IBrandingFooter {
    return {
      content: this.branding.footer && this.branding.footer.content ? this.branding.footer.content : '',
      links: this.branding.footer && this.branding.footer.links ? this.branding.footer.links : [],
      email: this.branding.footer && this.branding.footer.email ? this.branding.footer.email : null
    };
  }

  /**
   * Returns object with configName and name.
   */
  getCLI(): { configName: string; name: string } {
    return {
      configName: this.branding.cli && this.branding.cli.configName ? this.branding.cli.configName : DEFAULT_CLI_CONFIG_NAME,
      name: this.branding.cli && this.branding.cli.name ? this.branding.cli.name : DEFAULT_CLI_NAME
    };
  }

  /**
   * Returns object with docs URLs.
   */
  getDocs(): IBrandingDocs {
    return {
      devfile: this.branding.docs && this.branding.docs.devfile ? this.branding.docs.devfile : DEFAULT_DOCS_DEVFILE,
      workspace: this.branding.docs && this.branding.docs.workspace ? this.branding.docs.workspace : DEFAULT_DOCS_WORKSPACE,
      factory: this.branding.docs && this.branding.docs.factory ? this.branding.docs.factory : DEFAULT_DOCS_FACTORY,
      organization: this.branding.docs && this.branding.docs.organization ? this.branding.docs.organization : DEFAULT_DOCS_ORGANIZATION,
      general: this.branding.docs && this.branding.docs.general ? this.branding.docs.general : DEFAULT_DOCS_GENERAL,
      converting: this.branding.docs && this.branding.docs.converting ? this.branding.docs.converting : DEFAULT_DOCS_CONVERTING,
      faq: this.branding.docs && this.branding.docs.faq ? this.branding.docs.faq : undefined
    };
  }

  /**
   * Returns object with workspace dedicated data.
   */
  getWorkspace(): IBrandingWorkspace {
    return {
      priorityStacks: this.branding.workspace && this.branding.workspace.priorityStacks ? this.branding.workspace.priorityStacks : DEFAULT_WORKSPACE_PRIORITY_STACKS,
      defaultStack: this.branding.workspace && this.branding.workspace.defaultStack ? this.branding.workspace.defaultStack : DEFAULT_WORKSPACE_DEFAULT_STACK,
      creationLink: this.branding.workspace && this.branding.workspace.creationLink ? this.branding.workspace.creationLink : DEFAULT_WORKSPACE_CREATION_LINK
    };
  }

  /**
   * Returns object with UD configuration options.
   */
  getConfiguration(): IBrandingConfiguration {
    return {
      menu: {
        disabled:
          this.branding.configuration &&
            this.branding.configuration.menu && this.branding.configuration.menu.disabled
            ? this.branding.configuration.menu.disabled
            : []
      },
      features: {
        disabled:
          this.branding.configuration &&
            this.branding.configuration.features &&
            this.branding.configuration.features.disabled
            ? this.branding.configuration.features.disabled
            : []
      },
      prefetch: {
        cheCDN: this.branding.configuration &&
          this.branding.configuration.prefetch &&
          this.branding.configuration.prefetch.cheCDN
          ? this.branding.configuration.prefetch.cheCDN
          : undefined,
        resources:
          this.branding.configuration &&
            this.branding.configuration.prefetch &&
            this.branding.configuration.prefetch.resources
            ? this.branding.configuration.prefetch.resources
            : []
      }
    };
  }

}
