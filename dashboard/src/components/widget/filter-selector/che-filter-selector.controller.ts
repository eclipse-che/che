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
 * This class is handling the controller for the filter selector.
 * @author Ann Shumilova
 */
export class CheFilterSelectorController {

  private selectedValue: string;
  private onChange: Function;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
  }

  /**
   * Performs value selection and calls value changed handler.
   *
   * @param value
   */
  selectValue(value: string): void {
    this.selectedValue = value;
    this.onChange(value);
  }
}


