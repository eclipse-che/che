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

interface ICheClipboardScope extends ng.IScope {
  value: string;
  isCopied: boolean;
  onClick: Function;
}

/**
 * Defines a directive for the clipboard.
 * @author Oleksii Orel
 */
export class CheClipboard implements ng.IDirective {

  static $inject = ['$window', '$log'];

  restrict = 'E';
  replace = true;
  templateUrl = 'components/widget/copy-clipboard/che-clipboard.html';
  scope = {
    value: '=cheValue'
  };
  private $window: ng.IWindowService;
  private $log: ng.ILogService;

  /**
   * Default constructor that is using resource
   */
  constructor($window: ng.IWindowService, $log: ng.ILogService) {
    this.$window = $window;
    this.$log = $log;
  }


  link($scope: ICheClipboardScope, $element: ng.IAugmentedJQuery): void {
    const clipboardIconJq = $element.find('.copy-clipboard-area');
    const invInputJq = $element.find('input');
    $scope.onClick = () => {
      invInputJq.select();
      const copy = 'copy';
      if (this.$window.document.queryCommandSupported(copy)) {
        try {
          const isCopied = this.$window.document.execCommand(copy);
          if (isCopied) {
            this.$window.getSelection().removeAllRanges();
            clipboardIconJq.focus();
            $scope.isCopied = true;
          }
        } catch (error) {
          this.$log.error('Error. ' + error);
        }
      }
    };
  }
}
