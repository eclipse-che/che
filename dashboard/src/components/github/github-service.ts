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
 * Provides GitHub access
 * @author Stéphane Daviet
 * @author Florent Benoit
 */

/*global angular*/

export class GitHubService {

  constructor(register) {
    register.app.constant('gitHubApiUrlRoot', 'https://api.github.com')
      .constant('gitHubApiVersionHeader', 'application/vnd.github.moondragon+json')
      .constant('localGitHubToken', 'gitHubToken')
      .constant('gitHubCallbackPage', 'gitHubCallback.html')
      .service('githubOrganizationNameResolver', function () {
        this.resolve = function (organization) {
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
        function ($q, $interval, $window) {
          var popupWindow = null;
          var polling = null;

          var popup = {};

          popup.popupWindow = popupWindow;

          popup.open = function (url, options, config) {
            var optionsString = popup.stringifyOptions(popup.prepareOptions(options || {}));
            popupWindow = window.open(url, '_blank', optionsString); // jshint ignore:line

            if (popupWindow && popupWindow.focus) {
              popupWindow.focus();
            }

            return popup.pollPopup(config);
          };

          popup.pollPopup = function () {
            var deferred = $q.defer();
            polling = $interval(function () {
              try {
                if (popupWindow.document.title === 'callbackSuccess') {
                  //var queryParams = popupWindow.location.search.substring(1).replace(/\/$/, '');
                  //var hashParams = popupWindow.location.hash.substring(1).replace(/\/$/, '');
                  //var hash = utils.parseQueryString(hashParams);
                  //var qs = utils.parseQueryString(queryParams);

                  //angular.extend(qs, hash);

                  //if (qs.error) {
                  //    deferred.reject({ error: qs.error });
                  //} else {
                  //    deferred.resolve(qs);
                  //}
                  deferred.resolve(true);

                  popupWindow.close();
                  $interval.cancel(polling);
                }
              } catch (error) {
              }

              if (popupWindow.closed) {
                $interval.cancel(polling);
                deferred.reject({data: 'Authorization Failed'});
              }
            }, 35);
            return deferred.promise;
          };

          popup.prepareOptions = function (options) {
            var width = options.width || 500;
            var height = options.height || 500;
            return angular.extend({
              width: width,
              height: height,
              left: $window.screenX + (($window.outerWidth - width) / 2),
              top: $window.screenY + (($window.outerHeight - height) / 2.5)
            }, options);
          };

          popup.stringifyOptions = function (options) {
            var parts = [];
            angular.forEach(options, function (value, key) {
              parts.push(key + '=' + value);
            });
            return parts.join(',');
          };

          return popup;
        }
      ])
      .service('camelutils', function () {
        this.camelCase = function (name) {
          return name.replace(/([\:\-\_]+(.))/g, function (_, separator, letter, offset) {
            return offset ? letter.toUpperCase() : letter;
          });
        };

        this.parseQueryString = function (keyValue) {
          var obj = {}, key, value;
          angular.forEach((keyValue || '').split('&'), function (keyValue) {
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
          setToken: function (token) {
            localStorage.setItem('gitHubToken', token); // jshint ignore:line
          },
          getToken: function () {
            return localStorage.getItem('gitHubToken'); // jshint ignore:line
          }
        };
      }).factory('gitHubApiUtils', ['gitHubApiUrlRoot', function (gitHubApiUrlRoot) {
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
        function parseLinkHeader(linkHeaderValue) {
          var extractor = new RegExp('(<([^;]+)>;\\s?rel="(\\w+)")', 'g');
          // Sample Link Header content
          // "<https://api.github.com/user/repos?per_page=5&sort=full_name&page=2>; rel="next" \
          //   , <https://api.github.com/user/repos?per_page=5&sort=full_name&page=10>; rel="last""
          var links = {};
          var extraction;
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
        function isGitHubApiUrl(url) {
          var checked = url && url.indexOf(gitHubApiUrlRoot) === 0;
          return checked;
        }

        return {
          parseLinkHeader: parseLinkHeader,
          isGitHubApiUrl: isGitHubApiUrl
        };
      }])
      .filter('githubFilterRepositories', function () {
        return function (repositories, organizationFilter, repositoryNameFilter) {
          if (!repositories) {
            return [];
          }
          var filtered = [];
          for (var i = 0; i < repositories.length; i++) {
            var repository = repositories[i];
            if ((!organizationFilter || repository.owner.login === organizationFilter.login)
              && (!repositoryNameFilter || repository.name.toLocaleLowerCase().indexOf(repositoryNameFilter.toLocaleLowerCase()) >= 0)) {
              filtered.push(repository);
            }
          }
          return filtered;
        };
      }).factory('GitHubHeadersInjectorInterceptor', ['$q', 'gitHubTokenStore', 'gitHubApiUtils', function ($q, gitHubTokenStore, gitHubApiUtils) {
        /**
         * Inject the token inside config as HTTP request header if the request is targeted to http://api.github.com.
         *
         * @param config the classic request config object
         * @returns the config passed a input param with token injected inside if needed
         */
        function injectToken(config) {
          if (gitHubApiUtils.isGitHubApiUrl(config.url)) {
            var token = gitHubTokenStore.getToken();
            if (token) {
              config.headers['Authorization'] = 'token ' + token;
            }
          }
          return config;
        }

        return {
          request: injectToken
        };
      }]).factory('GitHubUnpaginateInterceptor', ['$q', '$injector', 'gitHubApiUtils', function ($q, $injector, gitHubApiUtils) {
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
        function unpaginate(response) {
          if (!gitHubApiUtils.isGitHubApiUrl(response.config.url)) {
            return response;
          }


          var nextUrl = gitHubApiUtils.parseLinkHeader(response.headers('link'))['next'];
          if (!nextUrl) {
            return response;
          }
          var $http = $injector.get('$http');
          return $http({
            url: nextUrl,
            method: 'GET',
            transformResponse: $http.defaults.transformResponse.concat([function (data) {
              return response.data.concat(data);
            }])
          });
        }

        return {
          response: unpaginate
        };
      }]).factory('GitHubAPIVersionSetterInterceptor', ['$q', 'gitHubApiUtils', 'gitHubApiVersionHeader', function ($q, gitHubApiUtils, gitHubApiVersionHeader) {
        /**
         * Set the right header to indicate to GitHub API the targeted version.
         *
         * @param config the classic request config object
         */
        function setGitHubApiVersionHeader(config) {
          if (gitHubApiUtils.isGitHubApiUrl(config.url)) {
            config.headers['Accept'] = gitHubApiVersionHeader;
          }
          return config;
        }

        return {
          request: setGitHubApiVersionHeader
        };
      }]).config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push('GitHubHeadersInjectorInterceptor');
        $httpProvider.interceptors.push('GitHubUnpaginateInterceptor');
        $httpProvider.interceptors.push('GitHubAPIVersionSetterInterceptor');
      }]).factory('GitHub', ['$resource', function ($resource) {
        var sort = 'full_name';
        var per_page = 50;
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
          organizationRepositories: function (organizationLogin) {
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
