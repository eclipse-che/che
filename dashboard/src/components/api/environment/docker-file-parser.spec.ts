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

import {DockerfileParser} from './docker-file-parser';

/**
 * Test the simple dokerfile parser and dumper
 * @author Oleksii Kurinnyi
 */

describe('Simple dockerfile parser >', () => {
  let parser;

  beforeEach(() => {
    parser = new DockerfileParser();
  });

  describe('parsing comments >', () => {

    it('should parse commented line', () => {
      const dockerfile = `# commented line
FROM eclipse/ubuntu_jdk8`;

      const result = parser.parse(dockerfile);

      const expectedResult = [{
        comment: '# commented line'
      }, {
        instruction: 'FROM',
        argument: 'eclipse/ubuntu_jdk8'
      }];

      expect(result).toEqual(expectedResult);
    });

    it('should throw an error if there no dockerfile instructions found', () => {
      const dockerfile = `# commented line`;

      const parseFn = function() {
        parser.parse(dockerfile);
      };

      expect(parseFn).toThrowError();
    });

  });

  describe('parsing directives >', () => {

    it(`should know 'escape' directive`, () => {
      const dockerfile = `# escape=\\
FROM eclipse/ubuntu_jdk8`;

      const result = parser.parse(dockerfile);

      const expectedResult = [{
        directive: '# escape=\\'
      }, {
        instruction: 'FROM',
        argument: 'eclipse/ubuntu_jdk8'
      }];

      expect(result).toEqual(expectedResult);
    });

    it(`should treat unknown directive as a comment`, () => {
      const dockerfile = `# directive=value
FROM eclipse/ubuntu_jdk8`;

      const result = parser.parse(dockerfile);

      const expectedResult = [{
        comment: '# directive=value'
      }, {
        instruction: 'FROM',
        argument: 'eclipse/ubuntu_jdk8'
      }];

      expect(result).toEqual(expectedResult);
    });

    it(`should throw an error if there are two identical directives`, () => {
      const dockerfile = `# escape=\\
# escape=\`
FROM eclipse/ubuntu_jdk8`;
      const parse =  () => {
        parser.parse(dockerfile);
      };

      expect(parse).toThrowError(TypeError);
    });

    it(`should treat known directive as a comment after an empty line`, () => {
      const dockerfile = `
# escape=\\
FROM eclipse/ubuntu_jdk8`;

      const result = parser.parse(dockerfile);

      const expectedResult = [{
        emptyLine: true
      }, {
        comment: '# escape=\\'
      }, {
        instruction: 'FROM',
        argument: 'eclipse/ubuntu_jdk8'
      }];

      expect(result).toEqual(expectedResult);
    });

    it(`should treat known directive as a comment after a comment`, () => {
      const dockerfile = `# comment line
# escape=\\
FROM eclipse/ubuntu_jdk8`;

      const result = parser.parse(dockerfile);

      const expectedResult = [{
        comment: '# comment line'
      }, {
        comment: '# escape=\\'
      }, {
        instruction: 'FROM',
        argument: 'eclipse/ubuntu_jdk8'
      }];

      expect(result).toEqual(expectedResult);
    });

    it(`should treat known directive as a comment after a builder instruction`, () => {
      const dockerfile = `FROM eclipse/ubuntu_jdk8
# escape=\\`;

      const result = parser.parse(dockerfile);

      const expectedResult = [{
        instruction: 'FROM',
        argument: 'eclipse/ubuntu_jdk8'
      }, {
        comment: '# escape=\\'
      }];

      expect(result).toEqual(expectedResult);
    });

  });

  describe('method parseArgument()', () => {

    describe('ENV argument as single variable form >', () => {

      it('should parse environment variable #1', () => {
        const instruction = 'ENV',
              argument = 'name environment variable value';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
          instruction: 'ENV',
          argument: ['name', 'environment variable value']
        }];
        expect(result).toEqual(expectedResult);
      });

      it('should parse environment variable #2', () => {
        const instruction = 'ENV',
              argument = 'SBT_OPTS \'-Dhttp.proxyHost=proxy.wdf.sap.corp -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.wdf.sap.corp -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts=nexus.wdf.sap.corp\'';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
          instruction: 'ENV',
          argument: ['SBT_OPTS', '\'-Dhttp.proxyHost=proxy.wdf.sap.corp -Dhttp.proxyPort=8080 -Dhttps.proxyHost=proxy.wdf.sap.corp -Dhttps.proxyPort=8080 -Dhttp.nonProxyHosts=nexus.wdf.sap.corp\'']
        }];
        expect(result).toEqual(expectedResult);
      });

      it(`should throw an error if incorrect ENV argument is written in single variable form`, () => {
        const instruction = 'ENV',
              argument = 'myNameJohnDoe'; // space between name and value is missed

        const parse =  () => {
          parser.parseArgument(instruction, argument);
        };

        expect(parse).toThrowError(TypeError);
      });

    });

    describe('ENV argument as multiple variables form >', () => {

      it('should parse single environment variable with backslashes', () => {
        const dockerfile = `# escape=\\
FROM eclipse/ubuntu_jdk8
ENV myDog=Rex\\ The\\ Dog`;

        const result = parser.parse(dockerfile);

        const expectedResult = [{
          directive: '# escape=\\'
        }, {
          instruction: 'FROM',
          argument: 'eclipse/ubuntu_jdk8'
        }, {
          instruction: 'ENV',
          argument: ['myDog', 'Rex The Dog']
        }];

        expect(result).toEqual(expectedResult);
      });

      it('should parse single environment variable with backtick', () => {
        const dockerfile = `# escape=\`
FROM eclipse/ubuntu_jdk8
ENV myDog=Rex\` The\` Dog`;

        const result = parser.parse(dockerfile);

        const expectedResult = [{
          directive: '# escape=\`'
        }, {
          instruction: 'FROM',
          argument: 'eclipse/ubuntu_jdk8'
        }, {
          instruction: 'ENV',
          argument: ['myDog', 'Rex The Dog']
        }];

        expect(result).toEqual(expectedResult);
      });

      it('should parse ENV argument as multiple variables form #1', () => {
        const instruction = 'ENV',
              argument = 'key=value';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
          instruction: 'ENV',
          argument: ['key', 'value']
        }];
        expect(result).toEqual(expectedResult);
      });

      it('should parse ENV argument as multiple variables form #2', () => {
        const instruction = 'ENV',
              argument = 'myName="John Doe" myDog=Rex\\ The\\ Dog myCat=fluffy';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
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

      it('should parse ENV argument as multiple variables form #3', () => {
        const instruction = 'ENV',
              argument = 'myName="John Doe" myDog=Rex\\ The\\ Dog \\\n    myCat=fluffy';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
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

      it(`should parse ENV argument as multiple variables form #4`, () => {
        const instruction = 'ENV',
              argument = 'myName="John Doe" myDog=Rex\\ The\\ Dog\\ myCat=fluffy';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
          instruction: 'ENV',
          argument: ['myName', 'John Doe']
        }, {
          instruction: 'ENV',
          argument: ['myDog', 'Rex The Dog myCat=fluffy']
        }];
        expect(result).toEqual(expectedResult);
      });

      it(`should parse ENV argument as multiple variables form #5`, () => {
        const instruction = 'ENV',
              argument = 'myVar=\\\\\\ \\\\\\\\';

        const result = parser.parseArgument(instruction, argument);

        const expectedResult = [{
          instruction: 'ENV',
          argument: ['myVar', '\\ \\\\']
        }];
        expect(result).toEqual(expectedResult);
      });

      it(`should throw an error if incorrect ENV argument is written in multiple variables form`, () => {
        const instruction = 'ENV',
              argument = 'myName="John Doe" myDog=Rex\\ The\\ Dog myCat fluffy'; // the 'equal' symbol is missed

        const parse =  () => {
          parser.parseArgument(instruction, argument);
        };

        expect(parse).toThrowError(TypeError);
      });

    });

  });

  it('should parse a dockerfile', () => {
    const dockerfile = `# escape=\\

FROM eclipse/ubuntu_jdk8
#ENV myCat fluffy
ENV myDog Rex The Dog
ENV myName="John Doe"
ENV myText long \\
multiline \\
value
ENV myVal=\\\\\\ \\\\\\\\`;

    const result = parser.parse(dockerfile);

    const expectedResult = [{
      directive: '# escape=\\'
    }, {
      emptyLine: true
    }, {
      instruction: 'FROM',
      argument: 'eclipse/ubuntu_jdk8'
    }, {
      comment: '#ENV myCat fluffy'
    }, {
      instruction: 'ENV',
      argument: ['myDog', 'Rex The Dog']
    }, {
      instruction: 'ENV',
      argument: ['myName', 'John Doe']
    }, {
      instruction: 'ENV',
      argument: ['myText', 'long \nmultiline \nvalue']
    }, {
      instruction: 'ENV',
      argument: ['myVal', '\\ \\\\']
    }];
    expect(result).toEqual(expectedResult);
  });

  it('should stringify an object into a dockerfile', () => {
    const instructions = [{
      directive: '# escape=\\'
    }, {
      emptyLine: true
    }, {
      instruction: 'FROM',
      argument: 'eclipse/ubuntu_jdk8'
    }, {
      comment: '#ENV myCat fluffy'
    }, {
      instruction: 'ENV',
      argument: ['myDog', 'Rex The Dog']
    }, {
      instruction: 'ENV',
      argument: ['myName', 'John Doe']
    }, {
      instruction: 'ENV',
      argument: ['myText', 'long \nmultiline \nvalue']
    }, {
      instruction: 'ENV',
      argument: ['myVal', '\\ \\\\']
    }];

    const result = parser.dump(instructions);

    const expectedResult = `# escape=\\

FROM eclipse/ubuntu_jdk8
#ENV myCat fluffy
ENV myDog Rex The Dog
ENV myName John Doe
ENV myText long \\
multiline \\
value
ENV myVal \\\\\ \\\\\\\\`;
    expect(result.trim()).toEqual(expectedResult);
  });

});

