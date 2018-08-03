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

import {CheNotification} from '../../../../components/notification/che-notification.factory';
import {CheProfile} from '../../../../components/api/che-profile.factory';

enum Tab {Profile, Organization}

interface IScope extends ng.IScope {
  profileInformationForm: ng.IFormController;
}

const MAX_ITEMS = 12;

/**
 * Controller for user details.
 *
 * @author Oleksii Orel
 */
export class AdminUserDetailsController {

  static $inject = ['cheProfile', '$location', '$timeout', '$scope', 'cheNotification', 'cheOrganization', 'initData'];

  tab: Object = Tab;

 /**
  * Angular Location service.
  */
  private $location: ng.ILocationService;
  /**
   * User profile service.
   */
  private cheProfile: CheProfile;
  /**
   * Notification service.
   */
  private cheNotification: CheNotification;
  /**
   * Index of the selected tab.
   */
  private selectedTabIndex: number = 0;
  /**
   * User profile.
   */
  private profile: che.IProfile;
  /**
   * Profile attributes.
   */
  private profileAttributes: che.IProfileAttributes;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * User ID.
   */
  private userId: string;
  /**
   * User Name.
   */
  private userName: string;

  private cheOrganization: che.api.ICheOrganization;

  private userOrganizations: Array<che.IOrganization>;
  /**
   * User's page info.
   */
  private pageInfo: che.IPageInfo;

  /**
   * Default constructor that is using resource injection
   */
  constructor(cheProfile: CheProfile,
              $location: ng.ILocationService,
              $timeout: ng.ITimeoutService,
              $scope: ng.IScope,
              cheNotification: CheNotification,
              cheOrganization: che.api.ICheOrganization,
              initData: {userId; userName}) {
    this.cheOrganization = cheOrganization;
    this.$location = $location;
    this.cheProfile = cheProfile;
    this.cheNotification = cheNotification;
    this.userId = initData.userId;
    this.userName = initData.userName;

    this.updateSelectedTab(this.$location.search().tab);
    let deRegistrationFn = $scope.$watch(() => {
      return $location.search().tab;
    }, (tab: string) => {
      if (!angular.isUndefined(tab)) {
        this.updateSelectedTab(tab);
      }
    }, true);

    let timeoutPromise: ng.IPromise<any>;
    $scope.$watch(() => {
      return angular.isUndefined(this.profileAttributes) || this.profileAttributes;
    }, () => {
      if (!this.profileAttributes || !(<IScope>$scope).profileInformationForm || (<IScope>$scope).profileInformationForm.$invalid) {
        return;
      }
      if (timeoutPromise) {
        $timeout.cancel(timeoutPromise);
      }
      timeoutPromise = $timeout(() => {
        this.setProfileAttributes();
      }, 500);
    }, true);

    $scope.$on('$destroy', () => {
      deRegistrationFn();
      if (timeoutPromise) {
        $timeout.cancel(timeoutPromise);
      }
    });

    this.updateData();
  }

  /**
   * Update user's data.
   */
  updateData(): void {
    this.isLoading = true;
    this.cheProfile.fetchProfileById(this.userId).then(() => {
      this.profile = this.cheProfile.getProfileById(this.userId);
      this.profileAttributes = angular.copy(this.profile.attributes);
      this.fetchOrganizations();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error && error.data && error.data.message !== null ? error.data.message : 'Failed to retrieve user\'s profile.');
    });
  }

  /**
   * Request the list of the user's organizations (first page).
   *
   * @returns {ng.IPromise<any>}
   */
  fetchOrganizations(): void {
    this.isLoading = true;
    this.cheOrganization.fetchUserOrganizations(this.userId, MAX_ITEMS).then((userOrganizations: Array<che.IOrganization>) => {
      this.userOrganizations = userOrganizations;
    },  (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message !== null ? error.data.message : 'Failed to retrieve organizations.');
    }).finally(() => {
      this.isLoading = false;
      this.pageInfo = this.cheOrganization.getUserOrganizationPageInfo(this.userId);
    });
  }

  /**
   * Returns the array of user's organizations.
   *
   * @returns {Array<any>}
   */
  getUserOrganizations(): Array<che.IOrganization> {
    return this.userOrganizations;
  }

  /**
   * Returns the the user's page info.
   *
   * @returns {che.IPageInfo}
   */
  getPagesInfo(): che.IPageInfo {
    return this.pageInfo;
  }

  /**
   * Request the list of the user's organizations for a page depends on page key('first', 'prev', 'next', 'last').
   * @param key {string}
   */
  fetchOrganizationPageObjects(key: string): void {
    this.isLoading = true;
    this.cheOrganization.fetchUserOrganizationPageObjects(this.userId, key).then((userOrganizations: Array<che.IOrganization>) => {
      this.userOrganizations = userOrganizations;
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Check if profile attributes have changed
   * @returns {boolean}
   */
  isAttributesChanged(): boolean {
    return !angular.equals(this.profile.attributes, this.profileAttributes);
  }

  /**
   * Set profile attributes
   */
  setProfileAttributes(): void {
    if (angular.equals(this.profile.attributes, this.profileAttributes)) {
      return;
    }
    let promise = this.cheProfile.setAttributes(this.profileAttributes, this.userId);

    promise.then(() => {
      this.cheNotification.showInfo('Profile successfully updated.');
      this.updateData();
    }, (error: any) => {
        this.profileAttributes = angular.copy(this.profile.attributes);
        this.cheNotification.showError(error.data.message ? error.data.message : 'Profile update failed.');
    });
  }

  /**
   * Update selected tab index by search part of URL.
   *
   * @param {string} tab
   */
  updateSelectedTab(tab: string): void {
    this.selectedTabIndex = parseInt(this.tab[tab], 10);
  }

  /**
   * Changes search part of URL.
   *
   * @param {number} tabIndex
   */
  onSelectTab(tabIndex?: number): void {
    let param: { tab?: string } = {};
    if (!angular.isUndefined(tabIndex)) {
      param.tab = Tab[tabIndex];
    }
    if (angular.isUndefined(this.$location.search().tab)) {
      this.$location.replace();
    }
    this.$location.search(param);
  }

}
