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
 * @name components.directive:cheLabelContainer
 * @restrict E
 * @element
 *
 * @description
 * <che-label-container> defines a directive for creating label container.
 *
 * @param {string} che-label-name
 * @param {string} che-label-description
 * @param {boolean} che-transclude-layout this option allows to pass 'layout{-breakpoint-suffix} attributes inside the directive
 *
 * @usage
 * <che-label-container che-label-name="Delete"
 *                      che-label-description="This action is irreversible."
 *                      che-transclude-layout="true" layout="column" layout-gt-md="row">
 *    <che-button-danger che-button-title="Delete"></che-button-danger>
 * </che-label-container>
 *
 * @author Florent Benoit
 */
export class CheLabelContainer {
  restrict: string = 'E';
  replace: boolean = true;
  transclude: boolean = true;
  templateUrl: string = 'components/widget/label-container/che-label-container.html';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    // scope values
    this.scope = {
      labelName: '@cheLabelName',
      labelDescription: '@?cheLabelDescription',
      transcludeLayout: '=?cheTranscludeLayout'
    };
  }

  compile(element: ng.IAugmentedJQuery, attrs: any) {
    if (!attrs.$attr.cheTranscludeLayout) {
      return;
    }

    let keys = Object.keys(attrs.$attr),
        container = element.find('.che-label-container-layout');

    keys.forEach((key: string) => {
      if (/^layout/.test(key)) {
        container.removeAttr(attrs.$attr[key]);
        container.attr(attrs.$attr[key], attrs[key]);

        element.removeAttr(attrs.$attr[key]);
      }
    });

  }

}
