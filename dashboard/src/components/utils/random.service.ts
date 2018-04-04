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

/**
 * This class is providing generator of random strings.
 *
 * @author Oleksii Kurinnyi
 */
export class RandomSvc {

  /**
   * Generates the random string.
   *
   * @param {string} prefix the prefix string
   * @param {string[]} list the list of strings to check the uniqueness of new one.
   * @return {string}
   */
  getRandString({prefix, list}: {prefix?: string, list?: string[]}) {
    let str: string,
        limit = 100;

    while (limit) {
      str = ('0000' + (Math.random() * Math.pow(36, 4)).toString(36)).slice(-4);

      if (prefix) {
        str = prefix + str;
      }

      if (!list || list.indexOf(str) === -1) {
        break;
      }

      limit--;
    }

    return str;
  }

}
