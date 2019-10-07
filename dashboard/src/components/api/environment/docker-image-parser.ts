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

import {IParser} from './parser';

export interface IDockerimage {
  repository: string;
  tag?: string;
}

export class DockerimageParser implements IParser {

  parse(content: string): IDockerimage {
    if (angular.isUndefined(content)) {
      throw new TypeError(`Docker image shouldn't be undefined.`);
    }

    content = content.trim();

    if (/\s/.test(content)) {
      throw new TypeError(`Docker image shouldn't contain any whitespace character.`);
    }

    const re = /^([a-z0-9]+(?:(?:(?:[._]|__|[-]*)[a-z0-9]+)+)(:\d*)?)(\/?[a-z0-9]+(?:(?:(?:[._]|__|[-]*)[a-z0-9]+)+)*)+(:(?![.-])[\w.-]{0,127})?$/;
    //            |                                                                                                    |
    //            |                                                                                                    |_ tag
    //            |_ repository

    const match = re.exec(content);
    if (match === null) {
      throw new TypeError(`A dockerimage should be written in form REPOSITORY[:TAG].`);
    }

    const obj = {} as IDockerimage;
    obj.repository = match[4] ? content.substr(0, content.indexOf(match[4])) : content;
    if (match[4]) {
      obj.tag = match[4].substr(1);
    }

    return obj;
  }

  dump(imageObj: IDockerimage): string {
    let dockerimage = imageObj.repository;

    if (imageObj.tag) {
      dockerimage += ':' + imageObj.tag;
    }

    return dockerimage;
  }
}
