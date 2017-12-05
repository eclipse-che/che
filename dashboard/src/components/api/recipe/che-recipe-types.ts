/*
 * Copyright (c) 2015-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

  static get OPENSHIFT(): string {
    return 'openshift';
  }

  static getValues(): Array<string> {
    return [
      CheRecipeTypesStatic.DOCKERFILE,
      CheRecipeTypesStatic.DOCKERIMAGE,
      CheRecipeTypesStatic.COMPOSE,
      CheRecipeTypesStatic.OPENSHIFT
    ];
  }

}

export const CheRecipeTypes: che.resource.ICheRecipeTypes = CheRecipeTypesStatic;
