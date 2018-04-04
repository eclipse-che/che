/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CheFactory} from '../../../components/api/che-factory.factory';

/**
 * @ngdoc controller
 * @name factories.controller:LastFactoriesController
 * @description This class is handling the controller of the last factories to display in the dashboard
 * @author Oleksii Orel
 */
export class LastFactoriesController {


  static $inject = ['cheFactory'];

  private cheFactory: CheFactory;
  private factories: Array<che.IFactory>;
  private factoriesOrderBy: string;
  private maxItems: number;
  private isLoading: boolean;

  /**
   * Default constructor
   */
  constructor(cheFactory: CheFactory) {
    this.cheFactory = cheFactory;

    this.factories = this.cheFactory.getPageFactories();

    // todo we should change to modificationDate after model's change
    this.factoriesOrderBy = '-creator.created';
    this.maxItems = 5;

    // todo add OrderBy to condition in fetch API
    let promise = this.cheFactory.fetchFactories(this.maxItems);

    this.isLoading = true;
    promise.finally(() => {
      this.isLoading = false;
      this.updateFactories();
    });
  }

  /**
   * Update factories array
   */
  updateFactories(): void {
    this.factories = this.cheFactory.getPageFactories();
  }

  /**
   * Returns the list of factories.
   *
   * @returns {Array<che.IFactory>}
   */
  getFactories(): Array<che.IFactory> {
    return this.factories;
  }
}
