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
 * @ngdoc controller
 * @name admin.plugins.controller:PluginsCtrl
 * @description This class is handling the controller of the plugins
 * @author Florent Benoit
 */
export class AdminPluginsCtrl {


  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($scope, $q, $mdDialog, $interval, $location, $anchorScroll, cheNotification, cheAdminPlugins) {
    this.$scope = $scope;
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.$interval = $interval;
    this.$location = $location;
    this.$anchorScroll = $anchorScroll;
    this.cheNotification = cheNotification;
    this.cheAdminPlugins = cheAdminPlugins;

    this.isLoading = true;
    this.buildInProgress = false;
    this.displayReloadChe = false;
    this.buildCheFailed = false;

    this.plugins = [];

    this.refreshPlugins();
  }


  refreshPlugins() {
    let promise = this.cheAdminPlugins.fetchPlugins();

    promise.then(() => {
        this.updateData();
        this.isLoading = false;
      },
      (error) => {
        this.isLoading = false;
        if (error.status === 304) {
          this.updateData();
        }
      });
  }

  /**
   * Require to remove the given plugin
   * @param pluginName name of the plugin to remove
   */
  remove(event, pluginName) {

    let confirm = this.$mdDialog.confirm()
      .title('Would you like to remove plug-in ' + pluginName + '?')
      .content('')
      .ariaLabel('Remove plug-in')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      let promise = this.cheAdminPlugins.removePlugin(pluginName);
      promise.then(() => {
        this.refreshPlugins();
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Delete failed.');
        console.log('error', error);
      });
    });


  }


  /**
   * Require to install the given plugin
   * @param pluginName name of the plugin to install and then add into staged
   */
  install(pluginName) {
    let promise = this.cheAdminPlugins.updatePlugin(pluginName, 'TO_INSTALL');
    let installPromise = promise.then(() => {
      this.refreshPlugins();
    });


    return installPromise;
  }

  /**
   * Require to install the given plugin
   * @param pluginName name of the plugin to install and then add into staged
   */
  uninstall(pluginName) {
    let promise = this.cheAdminPlugins.updatePlugin(pluginName, 'TO_UNINSTALL');
    let uninstallPromise = promise.then(() => {
      this.refreshPlugins();
    });


    return uninstallPromise;
  }



  prettyPrintStatus(plugin) {
    if ('STAGED_INSTALL' === plugin.status) {
      return 'To Be Installed';
    } else if ('STAGED_UNINSTALL' === plugin.status) {
      return 'To Be Uninstalled';
    }


  }

  cancelStage(plugin) {

    let action;
    if ('STAGED_INSTALL' === plugin.status) {
      action = 'UNDO_TO_INSTALL';
    } else if ('STAGED_UNINSTALL' === plugin.status) {
      action = 'UNDO_TO_UNINSTALL';
    } else {
      return;
    }


    let promise = this.cheAdminPlugins.updatePlugin(plugin.name, action);
    let cancelPromise = promise.then(() => {
      this.refreshPlugins();
    });


    return cancelPromise;
  }


  updateData() {

    this.plugins.length = 0;
    let updatePlugins = this.cheAdminPlugins.getPlugins();
    updatePlugins.forEach((plugin) => {
      this.plugins.push(plugin);
    });

  }


  dropzoneAcceptURL(url) {

    if (!url.startsWith('upload:') && !url.startsWith('http://eclipse.org/che/?install')) {
      let deferred = this.$q.defer();
      deferred.reject({data: {message:'The plugin URL is invalid'}});
      return deferred.promise;
    }

    var pluginReference = this.getPluginReference(url);

    let promise = this.cheAdminPlugins.addPlugin(pluginReference);
    let addPromise = promise.then(() => {
      this.refreshPlugins();
    }, (error) => {
      throw error;
    });

    return addPromise;
  }


  /**
   * Gets the plugin URI based on groupId, etc or if not found return the input URL
   * @param uri
   * @returns {*}
   */
  getPluginReference(uri) {

    var query = uri;
    var result = {};
    query.split('&').forEach(function(part) {
      var item = part.split('=');
      result[item[0]] = decodeURIComponent(item[1]);
    });

    return result.uri || uri;
  }


  buildAssembly() {

    this.buildInProgress = true;
    this.displayReloadChe = false;
    this.buildCheFailed = false;
    this.displayLog = false;


    let startInstallPromise = this.cheAdminPlugins.startInstall();
    startInstallPromise.then((data) => {
      // get ID
      let id = data.id;

      this.follow = this.$interval(() => {
        this.followProcess(id);
      }, 5000);

    }, (error) => {
      this.buildInProgress = false;
      this.cheNotification.showError(error.data.message ? error.data.message : error);
    });

  }



  followProcess(id) {
    this.checkInstall(id);
  }

  checkInstall(id) {
    let checkStatusPromise = this.cheAdminPlugins.getInstallDetails(id);

    checkStatusPromise.then((data) => {

      var regExp = new RegExp('\n', 'g');
      this.currentBuildLog = data.log.replace(regExp, '<br>');

      if ('SUCCESS' === data.status || 'FAILED' === data.status) {
        // cancel scan
        this.$interval.cancel(this.follow);
        this.finishInstall(data.status);
      }

    });

  }

  finishInstall(status) {
    this.refreshPlugins();
    this.buildInProgress = false;
    if (status === 'SUCCESS') {
      this.displayReloadChe = true;
    } else {
      this.displayReloadChe = false;
      this.buildCheFailed = true;

    }
  }

  /**
   * Reload the app
   */
  reloadChe() {
    this.reloadcheInProgress = true;
    let promise = this.cheAdminPlugins.reloadCheApp();
    promise.then(() => {
      this.reloadcheInProgress = false;
      this.reloadcheDone = true;
      this.displayReloadChe = false;
    }, () => {
      this.reloadcheInProgress = false;
      this.displayReloadChe = false;
    });

  }

  scrollToStaged() {
    this.$location.hash('plugin-staged');
    this.$anchorScroll();
  }

  toggleDisplayLog() {
    this.displayLog = !this.displayLog;
  }


}
