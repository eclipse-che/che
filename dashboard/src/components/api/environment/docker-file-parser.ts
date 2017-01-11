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
 * Simple parser and simple dumper of dockerfiles.
 * @author Oleksii Kurinnyi
 */

interface IRecipeLine {
  instruction?: string;
  argument?: string | string[];
  comment?: string;
}

export class DockerfileParser {
  fromRE: RegExp;
  backslashLineBreakRE: RegExp;
  lineBreakRE: RegExp;
  commentLineRE: RegExp;
  instructionRE: RegExp;
  envVariablesRE: RegExp;
  quotesRE: RegExp;
  backslashSpaceRE: RegExp;

  constructor() {
    this.fromRE = /^FROM\s+\w+/m;
    this.backslashLineBreakRE = /\\\r?\n(\s+)?/;
    this.lineBreakRE = /\r?\n/;
    this.commentLineRE = /^#/;
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
   * @param {string} content recipe content
   * @returns {IRecipeLine[]}
   */
  parse(content: string): IRecipeLine[] {
    if (!this.fromRE.test(content)) {
      throw new TypeError('Dockerfile should start with \'FROM\' instruction. Cannot parse this recipe.');
    }

    // join multiline instructions
    content = this._joinMultilineInstructions(content);

    // split dockerfile into separate instruction lines
    let instructionLines: string[] = content.split(this.lineBreakRE);

    // split instruction line into instruction and argument
    let instructions: IRecipeLine[] = [];
    instructionLines.forEach((line: string) => {
      line = line.trim();

      // check for comment line
      if (this.commentLineRE.test(line)) {
        instructions.push({comment: line});
        return;
      }

      let m = line.match(this.instructionRE);
      if (m) {
        let instruction = m[1],
            argument = m[2];

        // parse argument
        let results: IRecipeLine[] = this._parseArgument(instruction, argument);

        results.forEach((result: IRecipeLine) => {
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
   * @returns {string}
   * @private
   */
  _joinMultilineInstructions(content: string): string {
    return content.replace(this.backslashLineBreakRE, '');
  }

  /**
   * Parses an argument string depending on instruction
   *
   * @param instruction {string}
   * @param argumentStr {string}
   * @returns {IRecipeLine[]}
   * @private
   */
  _parseArgument(instruction: string, argumentStr: string): IRecipeLine[] {
    let results: IRecipeLine[] = [];

    switch (instruction) {
      case 'ENV':
        if (argumentStr.indexOf('=') >= 0) {
          // this argument string contains one or more environment variables
          let match;
          while (match = this.envVariablesRE.exec(argumentStr)) {
            let name: string  = match[1],
                value: string = match[2];
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
            argument: [argumentStr.slice(0, firstSpaceIndex), argumentStr.slice(firstSpaceIndex + 1)]
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
   * Dumps an array of instructions into a dockerfile
   *
   * @param instructions {IRecipeLine[]}
   * @returns {string}
   */
  dump(instructions: IRecipeLine[]): string {
    let content = '';

    instructions.forEach((line: IRecipeLine) => {
      if (line.comment) {
        content += line.comment + '\n';
      } else {
        content += line.instruction + ' ' + this._stringifyArgument(line) + '\n';
      }
    });

    return content;
  }

  /**
   * Dumps argument object depending on instruction.
   *
   * @param line {IRecipeLine}
   * @returns {string}
   * @private
   */
  _stringifyArgument(line: IRecipeLine): string {
    switch (line.instruction) {
      case 'ENV':
        return (line.argument as string[]).join(' ');
      default:
        return line.argument as string;
    }
  }
}
