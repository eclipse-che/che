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

const MAIN_URL = '/api/kubernetes/namespace';

/**
 * This class is handling the interactions with Kubernetes Namespace API.
 *
 * @author Oleksii Kurinnyi
 */
export class CheKubernetesNamespace implements che.api.ICheKubernetesNamespace {

  static $inject = [
    '$http',
  ];

  private $http: ng.IHttpService;
  private fetchKubernetesNamespacePromise: ng.IPromise<Array<che.IKubernetesNamespace>>;
  private namespaces: Array<che.IKubernetesNamespace> = [];

  /**
   * Default constructor that is using resource
   */
  constructor(
    $http: ng.IHttpService,
  ) {
    this.$http = $http;
  }

  fetchKubernetesNamespace(): ng.IPromise<che.IKubernetesNamespace[]> {
    if (this.fetchKubernetesNamespacePromise) {
      return this.fetchKubernetesNamespacePromise;
    }

    this.fetchKubernetesNamespacePromise = this.$http.get<che.IKubernetesNamespace[]>(MAIN_URL).then(response => {
      this.namespaces.length = 0;

      response.data.forEach((namespace: che.IKubernetesNamespace) => {
        this.namespaces.push(namespace);
      });

      return this.namespaces;
    });

    return this.fetchKubernetesNamespacePromise;
  }

  /**
   * Returns `true` if provided namespace is the placeholder namespace, i.e. "<workspaceId>".
   * @param namespace infrastructure namespace
   */
  isPlaceholder(namespace: che.IKubernetesNamespace): boolean {
    return /^</.test(namespace.name);
  }

  /**
   * Returns `true` if at least one of kubernetes namespaces is the placeholder namespace (i.e. "<workspaceId>").
   */
  containsPlaceholder(): boolean {
    return this.namespaces.some(this.isPlaceholder);
  }

  getHintDescription(): string {
    if (this.containsPlaceholder()) {
      return 'The infrastructure namespace where the workspace will be created. If the placeholder (i.e. &lt;workspaceId&gt;) is chosen then new kubernetes namespace named as workspace ID will be created.';
    }
    return 'The infrastructure namespace where the workspace will be created.';
  }

}
