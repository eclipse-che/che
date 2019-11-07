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

interface IDevfileSourceSelectorScope extends ng.IScope {
  selectedSource: string;
  onSelectSource: (source: string) => void;
  onChange: (eventData: {source: string}) => void;
}

export const URL = 'url';
export const YAML = 'yaml';

/**
 * Defines a directive for source selector widget.
 *
 * @author Oleksii Orel
 */
export class DevfileSourceSelector implements ng.IDirective {
  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/import-custom-stack/devfile-source-selector/devfile-source-selector.html';
  replace: boolean = true;

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   */
  constructor() {
    this.scope = {
      selectedSource: '=',
      onChange: '&?'
    };
  }

  link($scope: IDevfileSourceSelectorScope) {
    $scope[URL] = URL;
    $scope[YAML] = YAML;
    $scope.selectedSource = URL;
    $scope.onSelectSource = (source: string) => {
      $scope.selectedSource = source;
      if ($scope.onChange !== undefined) {
        $scope.onChange({source});
      }
    };
  }

}
