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

/**
 * This is constants of recipe types.
 *
 *  @author Oleksii Orel
 */
class CheRecipeTypesStatic {

  static get DOCKERFILE(): string {
    return 'dockerfile';
  }

  static get DOCKERIMAGE(): string {
    return 'dockerimage';
  }

  static get COMPOSE(): string {
    return 'compose';
  }

  static get KUBERNETES(): string {
    return 'kubernetes';
  }

  static get OPENSHIFT(): string {
    return 'openshift';
  }

  static getValues(): Array<string> {
    return [
      CheRecipeTypesStatic.DOCKERFILE,
      CheRecipeTypesStatic.DOCKERIMAGE,
      CheRecipeTypesStatic.COMPOSE,
      CheRecipeTypesStatic.KUBERNETES,
      CheRecipeTypesStatic.OPENSHIFT
    ];
  }

}

export const CheRecipeTypes: che.resource.ICheRecipeTypes = CheRecipeTypesStatic;
