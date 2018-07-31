/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This is class of resources limits types.
 *
 * @author Ann Shumilova
 * @author Oleksii Kurinnyi
 */
export class CheResourceLimitsStatic {
  static get RAM(): string {
    return 'RAM';
  }
  static get WORKSPACE(): string {
    return 'workspace';
  }
  static get RUNTIME(): string {
    return 'runtime';
  }
  static get TIMEOUT(): string {
    return 'timeout';
  }
}

export const CheResourceLimits: che.resource.ICheResourceLimits = CheResourceLimitsStatic;
