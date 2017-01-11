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

/**
 * This class is handling the plugins management for admins
 * @author Florent Benoit
 */
export class CheAdminPlugins {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource) {

    // keep resource
    this.$resource = $resource;

    this.plugins = [];


    this.pluginsApi = this.$resource('/admin/plugins',{}, {
      getPlugins: {method: 'GET', url: '/admin/plugins', isArray: true},
      addPlugin: {method: 'POST', url: '/admin/plugins?plugin=:plugin'},
      updatePlugin: {method: 'PUT', url: '/admin/plugins/:plugin/?action=:action'},
      removePlugin: {method: 'DELETE', url: '/admin/plugins/:plugin'},
      startInstall: {method: 'POST', url: '/admin/plugins/install'},
      getInstallDetails: {method: 'GET', url: '/admin/plugins/install/:id'},
      reload: {method: 'POST', url: '/admin/plugins/che-reload'}
    });

  }

  getPluginsServicePath() {
    return 'plugins';
  }


  fetchPlugins() {
    let promise = this.pluginsApi.query().$promise;
    // check if if was OK or not
    let parsedResultPromise = promise.then((data) => {
      this.plugins.length = 0;
      data.forEach((plugin) => {
        this.plugins.push(plugin);
      });
    });

    return parsedResultPromise;
  }

  getPlugins() {
    return this.plugins;
  }


  addPlugin(pluginReference) {
    let add = this.pluginsApi.addPlugin({plugin:pluginReference}, {});
    return add.$promise;
  }

  updatePlugin(pluginReference, action) {
    let update = this.pluginsApi.updatePlugin({plugin:pluginReference, action: action}, {});
    return update.$promise;
  }

  removePlugin(pluginReference) {
    let remove = this.pluginsApi.removePlugin({plugin:pluginReference});
    return remove.$promise;
  }



  startInstall() {
    let start = this.pluginsApi.startInstall();
    return start.$promise;
  }

  getInstallDetails(id) {
    let installDetails = this.pluginsApi.getInstallDetails({id:id}, {});
    return installDetails.$promise;
  }

  reloadCheApp() {
    let reload = this.pluginsApi.reload();
    return reload.$promise;
  }


}
