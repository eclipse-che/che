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
 * @ngdoc directive
 * @name components.directive:cheAutoScroll
 * @restrict A
 * @function
 * @element
 *
 * @description
 * `che-auto-scroll` defines an attribute for auto scrolling to the bottom of the element applied.
 *
 * @usage
 *   <text-area che-auto-scroll></text-area>
 *
 * @author Florent Benoit
 */
export class CheAutoScroll {

    /**
     * Default constructor that is using resource
     * @ngInject for Dependency injection
     */
    constructor($timeout) {
        this.$timeout = $timeout;
        this.restrict = 'A';
    }

    /**
     * Keep reference to the model controller
     */
    link($scope, element, attr) {
        $scope.$watch(attr.ngModel, () => {
            this.$timeout(() => {
                element[0].scrollTop = element[0].scrollHeight;
            });
        });
    }

}
