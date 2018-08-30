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

import {AdminsAddUserController} from './add-user/add-user.controller';
import {AdminsUserManagementCtrl} from './user-management.controller';
import {AdminUserDetailsController} from './user-details/user-details.controller';
import {AccountProfile} from './account-profile/account-profile.directive';

export class AdminsUserManagementConfig {

  constructor(register: che.IRegisterService) {
    register.controller('AdminUserDetailsController', AdminUserDetailsController);
    register.controller('AdminsAddUserController', AdminsAddUserController);
    register.controller('AdminsUserManagementCtrl', AdminsUserManagementCtrl);
    register.directive('accountProfile', AccountProfile);

    const userDetailLocationProvider = {
      title: 'User Details',
      reloadOnSearch: false,
      templateUrl: 'app/admin/user-management/user-details/user-details.html',
      controller: 'AdminUserDetailsController',
      controllerAs: 'adminUserDetailsController',
      resolve: {
        initData: ['$q', 'cheUser', '$route', 'chePermissions', ($q: ng.IQService, cheUser: any, $route: any, chePermissions: che.api.IChePermissions) => {
          const userId = $route.current.params.userId;
          let defer = $q.defer();
          chePermissions.fetchSystemPermissions().finally(() => {
            cheUser.fetchUserId(userId).then((user: che.IUser) => {
              if (!chePermissions.getUserServices().hasAdminUserService) {
                defer.reject();
              }
              defer.resolve({userId: userId, userName: user.name});
            }, (error: any) => {
              defer.reject(error);
            });
          });
          return defer.promise;
        }]
      }
    };

    // configure routes
    register.app.config(['$routeProvider', ($routeProvider: che.route.IRouteProvider) => {
      $routeProvider.accessWhen('/admin/usermanagement', {
        title: 'Users',
        templateUrl: 'app/admin/user-management/user-management.html',
        controller: 'AdminsUserManagementCtrl',
        controllerAs: 'adminsUserManagementCtrl',
        resolve: {
          check: ['$q', 'chePermissions', ($q: ng.IQService, chePermissions: che.api.IChePermissions) => {
            let defer = $q.defer();
            chePermissions.fetchSystemPermissions().finally(() => {
              if (chePermissions.getUserServices().hasUserService) {
                defer.resolve();
              } else {
                defer.reject();
              }
            });
            return defer.promise;
          }]
        }
      })
        .accessWhen('/admin/userdetails/:userId', userDetailLocationProvider);
    }]);

  }
}
