/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

interface IUniqueTeamNameValidatorAttributes {
  uniqueTeamName: string;
  parentAccount: string;
}

/**
 * Defines a directive for checking whether team name already exists.
 *
 * @author Ann Shumilova
 */
export class UniqueTeamNameValidator {

  static $inject = ['cheTeam', '$q'];

  /**
   * Team interection API.
   */
  private cheTeam: che.api.ICheTeam;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  private restrict: string;
  private require: string;

  /**
   * Default constructor that is using resource
   */
  constructor (cheTeam: che.api.ICheTeam, $q: ng.IQService) {
    this.cheTeam = cheTeam;
    this.$q = $q;
    this.restrict = 'A';
    this.require = 'ngModel';
  }

  /**
   * Check that the name of team is unique
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attributes: IUniqueTeamNameValidatorAttributes, ngModel: any) {

    // validate only input element
    if ('input' === element[0].localName) {

      ngModel.$asyncValidators.uniqueTeamName = (modelValue: any) => {

        // parent scope ?
        let scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }
        let deferred = this.$q.defer();
        let currentTeamName = scopingTest.$eval(attributes.uniqueTeamName),
          parentAccount = scopingTest.$eval(attributes.parentAccount),
          teams = this.cheTeam.getTeams();

        if (teams.length) {
          for (let i = 0; i < teams.length; i++) {
            if (teams[i].qualifiedName === parentAccount + '/' + currentTeamName) {
              continue;
            }
            if (teams[i].qualifiedName === parentAccount + '/' + modelValue) {
              deferred.reject(false);
            }
          }
          deferred.resolve(true);
        } else {
          deferred.resolve(true);
        }
        return deferred.promise;
      };
    }
  }
}
