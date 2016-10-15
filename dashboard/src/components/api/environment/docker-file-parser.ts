/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * Simple parser and simple dumper of dockerfiles.
 * @author Oleksii Kurinnyi
 */
export class DockerfileParser {

  constructor() {
    this.backslashLineBreakRE = /\\\r?\n(\s+)?/;
    this.lineBreakRE = /\r?\n/;
    this.instructionRE = /(\w+)\s+?(.+)/;
    this.envVariablesRE = /(?:^|(?:\s+))([^\s=]+?)=([^=]+?)(?:(?=\s+\w+=)|$)/g;
    //                     |            |          |       |
    //                     |            |          |       \- start of next variable name or end of line
    //                     |            |          \- variable value
    //                     |            \-  variable name
    //                     \- start of line or spaces before variable name
    this.quotesRE = /^["]|["]$/g;
    this.backslashSpaceRE = /\\\s/g;
  }

  /**
   * Parses a dockerfile into array of pairs of instructions and arguments
   *
   * @param content
   * @returns {Array}
   */
  parse(content) {
    // join multiline instructions
    content = this._joinMultilineInstructions(content);

    // split dockerfile into separate instruction lines
    let instructionLines = content.split(this.lineBreakRE);

    // split instruction line into instruction and argument
    let instructions = [];
    instructionLines.forEach((line) => {
      let m = line.match(this.instructionRE);
      if (m) {
        let instruction = m[1],
            argument = m[2];

        // parse argument
        let results = this._parseArgument(instruction, argument);

        results.forEach((result) => {
          instructions.push(result);
        });
      }
    });

    return instructions;
  }

  /**
   * Remove line breaks from lines which end with backslash
   *
   * @param content {string}
   * @returns {*}
   * @private
   */
  _joinMultilineInstructions(content) {
    return content.replace(this.backslashLineBreakRE, '');
  }

  /**
   * Parses an argument string depending on instruction
   *
   * @param instruction {string}
   * @param argumentStr {string}
   * @returns {Array}
   * @private
   */
  _parseArgument(instruction, argumentStr) {
    let results = [];

    switch (instruction) {
      case 'ENV':
        if (argumentStr.includes('=')) {
          // this argument string contains one or more environment variables
          let match;
          while(match = this.envVariablesRE.exec(argumentStr)) {
            let name  = match[1],
                value = match[2];
            if (this.quotesRE.test(value)) {
              value = value.replace(this.quotesRE, '');
            }
            if (this.backslashSpaceRE.test(value)) {
              value = value.replace(this.backslashSpaceRE, ' ');
            }

            results.push({
              instruction: instruction,
              argument: [name, value]
            });
          }
        } else {
          // this argument string contains only one environment variable
          let firstSpaceIndex = argumentStr.indexOf(' ');
          results.push({
            instruction: instruction,
            argument: [argumentStr.slice(0, firstSpaceIndex), argumentStr.slice(firstSpaceIndex+1)]
          });
        }
        break;
      default:
        results.push({
          instruction: instruction,
          argument: argumentStr
        });
    }

    return results;
  }

  /**
   * Dumps an array into a dockerfile
   *
   * @param instructions {array}
   * @returns {string}
   */
  dump(instructions) {
    let content = '';

    instructions.forEach((line) => {
      content += line.instruction + ' ' + this._stringifyArgument(line.instruction, line.argument) + '\n';
    });

    return content;
  }

  /**
   * Dumps argument object depending on instruction.
   *
   * @param instruction {string}
   * @param argument {*|string}
   * @returns {string}
   * @private
   */
  _stringifyArgument(instruction, argument) {
    switch (instruction) {
      case 'ENV':
        return argument.join(' ');
      default:
        return argument;
    }
  }
}
