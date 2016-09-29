/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * @ngdoc directive
 * @name components.directive:cheReloadHref
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-reload-href` defines an attribute for auto reloading the link if it is the same than current route.
 *
 * @usage
 *   <a che-reload-href="myLink"></a>
 *
 * @author Florent Benoit
 */
export class CheReloadHref {

    /**
     * Default constructor that is using resource
     * @ngInject for Dependency injection
     */
    constructor($location, $route) {
        this.restrict = 'A';
        this.$location = $location;
        this.$route = $route;
    }

    /**
     * Keep reference to the model controller
     */
    link($scope, element, attr) {
        if (attr.href) {
            element.bind('click', () => {
                $scope.$apply(() => {
                    if (this.$location.path() === attr.href || ('#' + this.$location.path()) === attr.href) {
                        console.log('reloading the route...');
                        this.$route.reload();
                    }
                });
            });
        }
    }

}
