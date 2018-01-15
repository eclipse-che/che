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

import {IParser} from './parser';

export interface IDockerimage {
  repository: string;
  tag?: string;
}

export class DockerimageParser implements IParser {

  parse(content: string): IDockerimage {
    content = content.trim();

    if (/\s/.test(content)) {
      throw new TypeError(`Docker image shouldn't contain any whitespace character.`);
    }

    const re = /^([^:]+)(?::([^:]+))?$/;
    //            |          |
    //            |          |_ tag
    //            |_ repository

    const match = re.exec(content);
    if (match === null) {
      throw new TypeError(`A dockerimage should be written in form REPOSITORY[:TAG].`);
    }

    const obj = {} as IDockerimage;
    obj.repository = match[1];
    if (match[2]) {
      obj.tag = match[2];
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
