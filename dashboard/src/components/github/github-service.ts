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

/**
 * Provides GitHub access
 * @author Stéphane Daviet
 * @author Florent Benoit
 */

/*global angular*/

export class GitHubService {

  constructor(register: che.IRegisterService) {
    register.app.constant('gitHubApiUrlRoot', 'https://api.github.com')
      .constant('gitHubApiVersionHeader', 'application/vnd.github.moondragon+json')
      .constant('localGitHubToken', 'gitHubToken')
      .constant('gitHubCallbackPage', 'gitHubCallback.html')
      .service('githubOrganizationNameResolver', function () {
        this.resolve = function (organization: any) {
          if (!organization) {
            return '';
          }
          return organization.name ? organization.name : organization.login;
        };
      })
      .factory('githubPopup', [
        '$q',
        '$interval',
        '$window',
        '$location',
        function ($q: ng.IQService, $interval: ng.IIntervalService, $window: ng.IWindowService, $log: ng.ILogService) {
          let popupWindow = null;
          let polling = null;

          let popup = {} as any;

          popup.popupWindow = popupWindow;

          popup.open = function (url: string, options: any, config: any) {
            const optionsString = popup.stringifyOptions(popup.prepareOptions(options || {}));
            popupWindow = window.open(url, '_blank', optionsString); // jshint ignore:line

            if (popupWindow && popupWindow.focus) {
              popupWindow.focus();
            }

            return popup.pollPopup(config);
          };

          popup.pollPopup = function () {
            const deferred = $q.defer();
            polling = $interval(function () {
              try {
                if (popupWindow.document.title === 'callbackSuccess') {
                  deferred.resolve(true);

                  popupWindow.close();
                  $interval.cancel(polling);
                }
              } catch (error) {
                $log.error(error);
              }

              if (popupWindow && popupWindow.closed) {
                $interval.cancel(polling);
                deferred.reject({data: 'Authorization Failed'});
              }
            }, 35);
            return deferred.promise;
          };

          popup.prepareOptions = function (options: any) {
            const width = options.width || 500;
            const height = options.height || 500;
            return angular.extend({
              width: width,
              height: height,
              left: $window.screenX + (($window.outerWidth - width) / 2),
              top: $window.screenY + (($window.outerHeight - height) / 2.5)
            }, options);
          };

          popup.stringifyOptions = function (options: any) {
            const parts = [];
            angular.forEach(options, function (value: string, key: string) {
              parts.push(key + '=' + value);
            });
            return parts.join(',');
          };

          return popup;
        }
      ])
      .service('camelutils', function () {
        this.camelCase = function (name: string) {
          return name.replace(/([\:\-\_]+(.))/g, function (_: any, separator: string, letter: string, offset: string) {
            return offset ? letter.toUpperCase() : letter;
          });
        };

        this.parseQueryString = function (keyValue: any) {
          let obj = {}, key, value;
          angular.forEach((keyValue || '').split('&'), function (keyValue: any) {
            if (keyValue) {
              value = keyValue.split('=');
              key = decodeURIComponent(value[0]);
              obj[key] = angular.isDefined(value[1]) ? decodeURIComponent(value[1]) : true;
            }
          });
          return obj;
        };
      })
      .factory('gitHubTokenStore', function () {
        return {
          setToken: function (token: string) {
            localStorage.setItem('gitHubToken', token); // jshint ignore:line
          },
          getToken: function () {
            return localStorage.getItem('gitHubToken'); // jshint ignore:line
          }
        };
      }).factory('gitHubApiUtils', ['gitHubApiUrlRoot', function (gitHubApiUrlRoot: string) {
        /**
         * Util function to parse a concatenated GitHub Link header value and return a object with rel value as properties and associated URL
         * as values.
         *
         * For instance, passing "<https://api.github.com/user/repos?per_page=5&sort=full_name&page=2>; rel="next" \
         *    , <https://api.github.com/user/repos?per_page=5&sort=full_name&page=10>; rel="last"" gives back:
         *   {
         *     next: 'https://api.github.com/user/repos?per_page=5&sort=full_name&page=2',
         *     last: 'https://api.github.com/user/repos?per_page=5&sort=full_name&page=10'
         *   }
         *
         * @param linkHeaderValue the value of the HTTP Link header to parse. {} is returned for null, empty, undefined or unparsable value
         * @returns a map kind object rel_value: URL
         */
        function parseLinkHeader(linkHeaderValue: string) {
          const extractor = new RegExp('(<([^;]+)>;\\s?rel="(\\w+)")', 'g');
          const links = {};
          let extraction;
          while ((extraction = extractor.exec(linkHeaderValue)) !== null) {
            links[extraction[3]] = extraction[2];
          }
          return links;
        }

        /**
         * Check is the URL is targeted to GitHub REST API.
         *
         * @param url the URL to check
         * @returns true if targeted to GitHub API, false either
         */
        function isGitHubApiUrl(url: string) {
          const checked = url && url.indexOf(gitHubApiUrlRoot) === 0;
          return checked;
        }

        return {
          parseLinkHeader: parseLinkHeader,
          isGitHubApiUrl: isGitHubApiUrl
        };
      }])
      .filter('githubFilterRepositories', function () {
        return function (repositories: any[], organizationFilter: any, repositoryNameFilter: string) {
          if (!repositories) {
            return [];
          }
          const filtered = [];
          for (let i = 0; i < repositories.length; i++) {
            const repository = repositories[i];
            if ((!organizationFilter || repository.owner.login === organizationFilter.login)
              && (!repositoryNameFilter || repository.name.toLocaleLowerCase().indexOf(repositoryNameFilter.toLocaleLowerCase()) >= 0)) {
              filtered.push(repository);
            }
          }
          return filtered;
        };
      }).factory('GitHubHeadersInjectorInterceptor', ['$q', 'gitHubTokenStore', 'gitHubApiUtils', function ($q: ng.IQService, gitHubTokenStore: any, gitHubApiUtils: any) {
        /**
         * Inject the token inside config as HTTP request header if the request is targeted to http://api.github.com.
         *
         * @param config the classic request config object
         * @returns the config passed a input param with token injected inside if needed
         */
        function injectToken(config: any) {
          if (gitHubApiUtils.isGitHubApiUrl(config.url)) {
            const token = gitHubTokenStore.getToken();
            if (token) {
              config.headers.Authorization = 'token ' + token;
            }
          }
          return config;
        }

        return {
          request: injectToken
        };
      }]).factory('GitHubUnpaginateInterceptor', ['$q', '$injector', 'gitHubApiUtils', function ($q: ng.IQService, $injector: ng.auto.IInjectorService, gitHubApiUtils: any) {
        /**
         * Unpaginate GitHub API request when it can. It means:
         * - detect if the url is targeted to http://api.github.com, unless return direct response,
         * - detect if URL headers contain a Link one with a rel="next", unless return direct response, endpoint of recursion in case of
         * unpagination (see below explanation).
         * - call this next page link to retrieve next page of result and concat it to first response.
         *
         * All the pages would be indirectly recursively retrieved by subsequent activation of the interceptor for next pages requests, which
         * will concatenate themselves to previous one and so on, until the last one with no next link is retrieved.
         *
         * @param response the classic response object
         * @returns a response with unpaginated results in possible
         */
        function unpaginate(response: any) {
          if (!gitHubApiUtils.isGitHubApiUrl(response.config.url)) {
            return response;
          }


          const nextUrl = gitHubApiUtils.parseLinkHeader(response.headers('link')).next;
          if (!nextUrl) {
            return response;
          }
          const $http = $injector.get('$http') as ng.IHttpService;
          return $http({
            url: nextUrl,
            method: 'GET',
            transformResponse: $http.defaults.transformResponse.concat([function (data: any) {
              return response.data.concat(data);
            }])
          });
        }

        return {
          response: unpaginate
        };
      }]).factory('GitHubAPIVersionSetterInterceptor', ['$q', 'gitHubApiUtils', 'gitHubApiVersionHeader', function ($q: ng.IQService, gitHubApiUtils: any, gitHubApiVersionHeader: any) {
        /**
         * Set the right header to indicate to GitHub API the targeted version.
         *
         * @param config the classic request config object
         */
        function setGitHubApiVersionHeader(config: any) {
          if (gitHubApiUtils.isGitHubApiUrl(config.url)) {
            config.headers.Accept = gitHubApiVersionHeader;
          }
          return config;
        }

        return {
          request: setGitHubApiVersionHeader
        };
      }]).config(['$httpProvider', function ($httpProvider: ng.IHttpProvider) {
        $httpProvider.interceptors.push('GitHubHeadersInjectorInterceptor');
        $httpProvider.interceptors.push('GitHubUnpaginateInterceptor');
        $httpProvider.interceptors.push('GitHubAPIVersionSetterInterceptor');
      }]).factory('GitHub', ['$resource', function ($resource: ng.resource.IResourceService) {
        const sort = 'full_name';
        const per_page = 50;
        return {
          user: function () {
            return $resource('https://api.github.com/user');
          },
          organizations: function () {
            return $resource('https://api.github.com/user/orgs');
          },
          userRepositories: function () {
            return $resource('https://api.github.com/user/repos', {sort: sort, per_page: per_page});
          },
          organizationRepositories: function (organizationLogin: any) {
            return $resource('https://api.github.com/orgs/:organizationLogin/repos', {
              organizationLogin: organizationLogin,
              sort: sort,
              per_page: per_page
            });
          }
        };
      }]);
  }
}
