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

/**
 * This class is handling the controller for the import devfile by URL
 *
 * @author Oleksii Orel
 */
export class DevfileByUrlController {

  static $inject = ['cheFactory'];

  workspaceDevfileOnChange: Function;

  private cheFactory: CheFactory;

  private validInfo: { isValid: boolean; errors: string[]};

  /**
   * Default constructor that is using resource injection
   */
  constructor(cheFactory: CheFactory) {
    this.cheFactory = cheFactory;

    this.validInfo = { isValid: true, errors: [] };
  }

  $onInit(): void { }

  onUrlChanged(url: string): void {
    this.cheFactory.fetchParameterFactory({url}).then((res: { devfile: che.IWorkspaceDevfile }) => {
      const {devfile} = res;
      this.validInfo = { isValid: true, errors: [] };
      if (angular.isFunction(this.workspaceDevfileOnChange) && devfile) {
        this.workspaceDevfileOnChange({devfile});
      }
    }, (error: any) => {
      this.validInfo.isValid = false;
      let message = error && error.data && error.data.message ? error.data.message : 'Error. HTTP request failed';
      this.validInfo.errors.push(message);
      console.log('>>>>>>>>>>> DevfileByUrlController error', error);
    });
  }

}
