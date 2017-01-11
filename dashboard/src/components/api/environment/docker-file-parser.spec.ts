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

import {DockerfileParser} from './docker-file-parser';

/**
 * Test the simple dokerfile parser and dumper
 * @author Oleksii Kurinnyi
 */

describe('Simper dockerfile parser', () => {
  let parser;

  beforeEach(() => {
    parser = new DockerfileParser();
  });

  describe('method _parseArgument()', () => {
    it('should parse ENV argument with single environment variable', () => {
      let instruction = 'ENV',
          argument = 'name environment variable value';

      let result = parser._parseArgument(instruction, argument);

      let expectedResult = [{
        instruction: 'ENV',
        argument: ['name', 'environment variable value']
      }];
      expect(result).toEqual(expectedResult);
    });

    it('should parse ENV argument with several environment variables', () => {
      let instruction = 'ENV',
          argument = 'myName="John Doe" myDog=Rex\ The\ Dog myCat=fluffy';

      let result = parser._parseArgument(instruction, argument);

      let expectedResult = [{
        instruction: 'ENV',
        argument: ['myName', 'John Doe']
      }, {
        instruction: 'ENV',
        argument: ['myDog', 'Rex The Dog']
      }, {
        instruction: 'ENV',
        argument: ['myCat', 'fluffy']
      }];
      expect(result).toEqual(expectedResult);
    });
  });

  it('should parse a dockerfile', () => {
    let dockerfile = 'FROM codenvy/ubuntu_jdk8ENV'
    + '\n#ENV myCat fluffy'
    + '\nENV myDog Rex The Dog'
    + '\nENV myName John Doe';

    let result = parser.parse(dockerfile);

    let expectedResult = [{
      instruction: 'FROM',
      argument: 'codenvy/ubuntu_jdk8ENV'
    }, {
      comment: '#ENV myCat fluffy'
    }, {
      instruction: 'ENV',
      argument: ['myDog', 'Rex The Dog']
    }, {
      instruction: 'ENV',
      argument: ['myName', 'John Doe']
    }];
    expect(result).toEqual(expectedResult);
  });

  it('should stringify an object into a dockerfile', () => {
    let instructions = [{
      instruction: 'FROM',
      argument: 'codenvy/ubuntu_jdk8ENV'
    }, {
      comment: '#ENV myCat fluffy'
    }, {
      instruction: 'ENV',
      argument: ['myDog', 'Rex The Dog']
    }, {
      instruction: 'ENV',
      argument: ['myName', 'John Doe']
    }];

    let result = parser.dump(instructions);

    let expectedResult = 'FROM codenvy/ubuntu_jdk8ENV'
      + '\n#ENV myCat fluffy'
      + '\nENV myDog Rex The Dog'
      + '\nENV myName John Doe';
    expect(result.trim()).toEqual(expectedResult);
  });

});

