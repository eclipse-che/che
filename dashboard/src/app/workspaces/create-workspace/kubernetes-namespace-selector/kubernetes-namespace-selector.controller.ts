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

import { IKubernetesNamespaceScopeBindings, IKubernetesNamespaceOnChange } from './kubernetes-namespace-selector.directive';

/**
 * This class handles infrastructure namespace for new workspaces.
 *
 * @author Oleksii Kurinnyi
 */
export class KubernetesNamespaceSelectorController implements IKubernetesNamespaceScopeBindings {

  static $inject = [
    'cheKubernetesNamespace',
  ];

  /**
   * Directive scope bindings.
   */
  onChange: IKubernetesNamespaceOnChange;

  selectedName: string;
  namespaces: string[] = [];
  namespaceById: { [name: string]: che.IKubernetesNamespace };

  private cheKubernetesNamespace: che.api.ICheKubernetesNamespace;

  /**
   * Default constructor that is using resource injection
   */
  constructor(
    cheKubernetesNamespace: che.api.ICheKubernetesNamespace
  ) {
    this.cheKubernetesNamespace = cheKubernetesNamespace;
  }

  $onInit(): void {
    this.cheKubernetesNamespace.fetchKubernetesNamespace().then((namespaces: che.IKubernetesNamespace[]) => this.updateNamespacesList(namespaces));
  }

  updateNamespacesList(kubernetesNamespaces: che.IKubernetesNamespace[]): void {
    this.namespaces.length = 0;
    this.namespaceById = {};
    this.selectedName = undefined;

    kubernetesNamespaces.forEach(namespace => {
      const displayName = this.getDisplayName(namespace);
      this.namespaceById[displayName] = namespace;
      this.namespaces.push(displayName);
      if (this.selectedName === undefined || namespace.attributes.default) {
        this.selectedName = displayName;
      }
    });
    this.namespaces.sort();

    this.onChangeNamespace(this.selectedName);
  }

  onChangeNamespace(displayName: string): void {
    const namespace = this.namespaceById[displayName];

    // we don't need to propagate the namespace placeholder as namespace ID
    if (this.cheKubernetesNamespace.isPlaceholder(namespace)) {
      this.onChange({ namespaceId: undefined });
      return;
    }

    const namespaceId = namespace.name || displayName;
    this.onChange({ namespaceId });
  }

  getDisplayName(namespace: che.IKubernetesNamespace): string {
    return namespace.attributes.displayName || namespace.name;
  }

}
