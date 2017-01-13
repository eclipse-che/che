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

/**
 * This class is handling the branding data in Che
 * @author Florent Benoit
 */
export class CheBranding {
  $q: ng.IQService;
  $rootScope: che.IRootScopeService;
  $http: ng.IHttpService;
  cheService: CheService;

    /**
     * Default constructor that is using resource
     * @ngInject for Dependency injection
     */
    constructor($http: ng.IHttpService, $rootScope: che.IRootScopeService, $q: ng.IQService, cheService: CheService) {
        this.$http = $http;
        this.$rootScope = $rootScope;
        this.deferred = $q.defer();
        this.promise = this.deferred.promise;
        this.cheService = cheService;
        this.updateData();
        this.getVersion();
    }

    getVersion() {
      this.cheService.fetchServicesInfo().then(() => {
        let info = this.cheService.getServicesInfo();
        this.$rootScope.productVersion = (info && info.implementationVersion) ? info.implementationVersion : '';
      });
    }

    updateData() {

        let assetPrefix = 'assets/branding/';

        // load data
        this.$http.get(assetPrefix + 'product.json').then((data) => {

            let brandingData = data.data;

            this.$rootScope.branding = {
                title: brandingData.title,
                name: brandingData.name,
                logoURL: assetPrefix + brandingData.logoFile,
                logoText: assetPrefix + brandingData.logoTextFile,
                favicon : assetPrefix + brandingData.favicon,
                loaderURL: assetPrefix + brandingData.loader,
                ideResourcesPath : brandingData.ideResources,
                helpPath : brandingData.helpPath,
                helpTitle : brandingData.helpTitle,
                supportEmail: brandingData.supportEmail,
                oauthDocs: brandingData.oauthDocs
            };

            this.productName = this.$rootScope.branding.title;
            this.name = this.$rootScope.branding.name;
            this.productFavicon = this.$rootScope.branding.productFavicon;
            this.productLogo = this.$rootScope.branding.logoURL;
            this.productLogoText = this.$rootScope.branding.logoText;
            this.ideResourcesPath = this.$rootScope.branding.ideResourcesPath;
            this.helpPath = this.$rootScope.branding.helpPath;
            this.helpTitle = this.$rootScope.branding.helpTitle;
            this.supportEmail = this.$rootScope.branding.supportEmail;
            this.oauthDocs = this.$rootScope.branding.oauthDocs;
            this.deferred.resolve(this.$rootScope.branding);
        });

    }

    getName() {
      return this.name;
    }

    getProductName() {
        return this.productName;
    }

    getProductLogo() {
        return this.productLogo;
    }

    getProductFavicon() {
        return this.productFavicon;
    }

    getIdeResourcesPath() {
        return this.ideResourcesPath;
    }

    getProductHelpPath() {
        return this.helpPath;
    }

    getProductHelpTitle() {
        return this.helpTitle;
    }

    getProductSupportEmail() {
        return this.supportEmail;
    }
}

