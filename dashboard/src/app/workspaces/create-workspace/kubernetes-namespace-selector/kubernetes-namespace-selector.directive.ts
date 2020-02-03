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

export interface IKubernetesNamespaceScopeBindings {
  onChange: IKubernetesNamespaceOnChange;
}
export interface IKubernetesNamespaceOnChange {
  (eventData: { namespaceId: string }): void;
}

export class KubernetesNamespaceSelectorDirective implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/workspaces/create-workspace/kubernetes-namespace-selector/kubernetes-namespace-selector.html';
  controller: string = 'KubernetesNamespaceSelectorController';
  controllerAs: string = 'ctrl';
  bindToController: boolean = true;

  transclude: boolean = true;

  scope: {
    onChange: string;
  };

  constructor() {
    this.scope = {
      onChange: '&onChange',
    };
  }

}
