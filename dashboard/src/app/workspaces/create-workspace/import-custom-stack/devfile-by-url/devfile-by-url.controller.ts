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

import {CheFactory} from '../../../../../components/api/che-factory.factory';

const ATTR_URL = 'factoryurl';

/**
 * This class is handling the controller for import devfile from a URL.
 *
 * @author Oleksii Orel
 */
export class DevfileByUrlController {

  static $inject = ['cheFactory'];

  workspaceDevfileOnChange: Function;
  workspaceDevfileLocation: string;

  private workspaceDevfile: che.IWorkspaceDevfile | {} = {};
  private cheFactory: CheFactory;

  /**
   * Default constructor that is using resource injection
   */
  constructor(cheFactory: CheFactory) {
    this.cheFactory = cheFactory;
  }

  $onInit(): void {
    if (this.workspaceDevfileLocation) {
      this.onUrlChanged(this.workspaceDevfileLocation);
    }
  }

  onUrlChanged(url: string): void {
    if (this.cheFactory.hasDevfile(url)) {
      const devfile = this.cheFactory.getDevfile(url);
      const attributes = {};
      attributes[ATTR_URL] = url;
      if (angular.isFunction(this.workspaceDevfileOnChange) && devfile) {
        this.workspaceDevfileOnChange({devfile, attributes});
      }
      this.workspaceDevfile = devfile;
    }
  }

  isUrlValid(url: string): ng.IPromise<void> {
    return this.cheFactory.fetchDevfile(url);
  }
}
