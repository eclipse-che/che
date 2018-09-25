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

interface IAccountProfileScope extends ng.IScope {
  profileAttributes: {
    phone?: string;
    country?: string;
    employer?: string;
    jobtitle?: string;
    lastName?: string;
    firstName?: string;
  };
  profileInformationForm: ng.IFormController;
  countries?: Array<{ 'name': string, 'code': string }>;
  jobs?: Array<{ 'name': string }>;
}

/**
 * @ngdoc directive
 * @name account.profile.directive:accountProfile
 * @restrict E
 * @element
 *
 * @description
 * <account-profile profile-attributes="ctrl.profileAttributes"></account-profile>` for displaying account profile.
 *
 * @usage
 *   <account-profile profile-attributes="ctrl.profileAttributes"></account-profile>
 *
 * @author Florent Benoit
 */
export class AccountProfile implements ng.IDirective {

  static $inject = ['jsonCountries', 'jsonJobs'];

  restrict = 'E';
  templateUrl = 'app/account/account-profile/account-profile.html';
  replace = true;
  scope = {
    profileAttributes: '=profileAttributes',
    profileInformationForm: '=?profileInformationForm'
  };

  jsonCountries: string;
  jsonJobs: string;

  /**
   * Default constructor that is using resource
   */
  constructor(jsonCountries: string, jsonJobs: string) {
    this.jsonCountries = jsonCountries;
    this.jsonJobs = jsonJobs;
  }

  link($scope: IAccountProfileScope) {
    $scope.countries = angular.fromJson(this.jsonCountries);
    $scope.jobs = angular.fromJson(this.jsonJobs);
  }
}
