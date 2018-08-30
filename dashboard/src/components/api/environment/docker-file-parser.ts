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

/**
 * Simple parser and simple dumper of dockerfiles.
 * @author Oleksii Kurinnyi
 */

interface IRecipeLine {
  instruction?: string;
  argument?: string | string[];
  comment?: string;
  directive?: string;
  emptyLine?: boolean;
}

export class DockerfileParser implements IParser {
  /**
   * RegExp to match the very first instruction to be 'FROM'.
   */
  fromRE: RegExp;
  /**
   * RegExp to match an empty line.
   */
  emptyLineRE: RegExp;
  /**
   * RegExp to match a comment line.
   */
  commentRE: RegExp;
  /**
   * RegExp to match a single dockerfile instruction.
   */
  instructionRE: RegExp;
  /**
   * RegExp to match leading and trailing quotes.
   */
  quotesRE: RegExp;
  /**
   * RegExp to match a dockerfile parsing directive.
   */
  directiveRE: RegExp;
  /**
   * RegExp to match an environment variable in form <code>name="quoted value"</code>.
   */
  envVariableQuotedValueRE: RegExp;
  /**
   * RegExp to match any line break.
   */
  linebreaksRE: RegExp;
  /**
   * RegExp to match any escaped whitespace or line break.
   */
  escapedWhitespacesAndLinebreaksRE: RegExp;
  /**
   * RegExp to match any escaped escape symbol.
   */
  escapedEscapeSymbolsRE: RegExp;
  /**
   * RegExp to match escaped line break at start of the line.
   */
  escapedLineBreakAtStartRE: RegExp;
  /**
   * RegExp to match environment variable name.
   */
  variableNameRE: RegExp;
  /**
   * RegExp to match first unescaped whitespace or line break.
   */
  unEscapedWhitespaceRE: RegExp;

  /**
   * Allowed values for parsing directives.
   */
  directiveValues: {
    escape: string[],
    [name: string]: string[]
  };
  /**
   * Parsing directives with default values.
   */
  directives: {
    escape?: string,
    [name: string]: string
  };
  /**
   * Current escape to be used as part of a RegExp
   */
  escape: string;
  /**
   * RegExp to match any escape symbol.
   */
  escapeRE: RegExp;

  constructor() {
    this.fromRE = /^FROM$/i;
    this.emptyLineRE = /^\s*\r?\n/;
    this.commentRE = /^\s*#/;
    this.quotesRE = /^["']|['"]$/g;
    this.envVariableQuotedValueRE = /^([^\s=]+?)=['"]([^'"]+?)['"]\s*/;
    this.linebreaksRE = /(\r?\n)/g;
    this.variableNameRE = /^([a-z_][a-z0-9_]*)=/i;

    this.directiveValues = {
      escape: ['\\', '`']
    };
    this.directives = {};
    const knownDirectives = 'escape';
    this.directiveRE = new RegExp('^\\s*#\\s*(' + knownDirectives + ')\\s*=\\s*([^\\s]+)', 'i');

    // set default parsing directive
    this.updateDirectives('escape', this.directiveValues.escape[0]);
  }

  /**
   * Parses a dockerfile into array of pairs of instructions and arguments
   *
   * @param {string} content recipe content
   * @returns {IRecipeLine[]}
   */
  parse(content: string): IRecipeLine[] {
    let recipeContent = content;

    const instructions: IRecipeLine[] = [];
    const uniqueDirectives: string[] = [];
    let counter = 1000;
    let lookingForDirectives = true;
    let firstInstructionFound = false;

    // set default parsing directive
    this.updateDirectives('escape', this.directiveValues.escape[0]);

    while (recipeContent.length && counter) {
      counter--;

      // process parsing directive
      if (lookingForDirectives) {
        if (!this.emptyLineRE.test(recipeContent) && this.directiveRE.test(recipeContent)) {
          const parts = this.splitBySymbolAtIndex(recipeContent, this.getSplitIndex(recipeContent, '\n')),
                directiveStr = parts[0];
          recipeContent = parts[1];

          let [ , name, value] = this.directiveRE.exec(directiveStr);

          if (this.directiveValues[name].indexOf(value) === -1) {
            // directive value is not allowed
            // hence this line should be treated as comment
            instructions.push({comment: directiveStr});
            lookingForDirectives = false;
            continue;
          }

          name = name.toLowerCase();
          if (uniqueDirectives.indexOf(name) !== -1) {
            throw new TypeError(`Directive "${name}" is invalid due to appearing twice.`);
          }
          uniqueDirectives.push(name);

          this.updateDirectives(name, value);
          instructions.push({
            directive: directiveStr
          });
          continue;
        }
        lookingForDirectives = false;
      }

      // process empty line
      if (this.emptyLineRE.test(recipeContent)) {
        const parts = this.splitBySymbolAtIndex(recipeContent, this.getSplitIndex(recipeContent, '\n'));
        recipeContent = parts[1];

        instructions.push({emptyLine: true});

        continue;
      }

      // process comment
      if (this.commentRE.test(recipeContent)) {
        const parts = this.splitBySymbolAtIndex(recipeContent, this.getSplitIndex(recipeContent, '\n')),
              commentStr = parts[0];
        recipeContent = parts[1];

        instructions.push({comment: commentStr});

        continue;
      }

      // process instruction
      if (this.instructionRE.test(recipeContent)) {
        const [fullMatch, instruction, argument] = this.instructionRE.exec(recipeContent);

        if (!firstInstructionFound && !this.fromRE.test(instruction)) {
          throw new TypeError('Dockerfile should start with \'FROM\' instruction.');
        }
        firstInstructionFound = true;

        // parse argument
        let results: IRecipeLine[] = this.parseArgument(instruction, argument);
        results.forEach((result: IRecipeLine) => {
          instructions.push(result);
        });

        const parts = this.splitBySymbolAtIndex(recipeContent, fullMatch.length);
        recipeContent = parts[1];

        continue;
      }

      // got weird line
      const [line, ] = this.splitBySymbolAtIndex(recipeContent, this.getSplitIndex(recipeContent, '\n'));
      throw new TypeError(`Cannot parse recipe from line: ${line}`);
    }

    // check if recipe contains any dockerfile instruction
    if (!firstInstructionFound) {
      throw new TypeError(`Cannot find any dockerfile instruction.`);
    }

    return instructions;
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
      if (line.emptyLine) {
        content += '\n';
      } else if (line.directive) {
        content += line.directive + '\n';
      } else if (line.comment) {
        content += line.comment + '\n';
      } else {
        content += line.instruction + ' ' + this.stringifyArgument(line) + '\n';
      }
    });

    return content;
  }

  /**
   * Returns index of given symbol in the string.
   *
   * @param {string} content a string
   * @param {string} delimiter a substring, position of which has to be found.
   * @return {number}
   */
  private getSplitIndex(content: string, delimiter: string): number {
    return content.indexOf(delimiter) === -1 ? content.length : content.indexOf(delimiter);
  }

  /**
   * Builds a RegExp to match any escape symbol.
   * Builds a RegExp to match a dockerfile instruction.
   * Builds a RegExp to match any escaped whitespace or line break.
   * Builds a RegExp to match any escaped whitespace or line break.
   * Builds a RegExp to match escaped line break at line start.
   * Builds a RegExp to match first unescaped whitespace or line break.
   */
  private buildParsingDirectiveSpecificRegExps(): void {
    this.escapeRE = new RegExp('(' + this.escape + ')', 'g');

    const instructions = 'FROM|RUN|CMD|LABEL|MAINTAINER|EXPOSE|ENV|ADD|COPY|ENTRYPOINT|VOLUME|USER|WORKDIR|ARG|ONBUILD|STOPSIGNAL|HEALTHCHECK|SHELL';
    this.instructionRE = new RegExp('^\\s*(' + instructions + ')\\s+((?:.|' + this.escape + '\\r?\\n)+(?!\\r?\\n).)', 'i');

    this.escapedWhitespacesAndLinebreaksRE = new RegExp('([^' + this.escape + ']?(?:' + this.escape + '{2})*)' + this.escape + '(\\s|\\r?\\n|' + this.escape + ')', 'g');

    this.escapedEscapeSymbolsRE = new RegExp('' + this.escape + '(' + this.escape + ')', 'g');

    this.escapedLineBreakAtStartRE = new RegExp('^' + this.escape + '\\r?\\n');

    this.unEscapedWhitespaceRE = new RegExp('[^' + this.escape + '](?:' + this.escape + '{2})*(\\s|\\r?\\n)');
  }

  /**
   * Updates parsing directives object and rebuilds appropriate RegExp.
   *
   * @param {string} directive a directive's name
   * @param {string} value a directive's value
   */
  private updateDirectives(directive: string, value: string): void {
    this.escape = value === '\\' ? '\\\\' : value;
    this.directives[directive] = value;

    switch (directive) {
      case 'escape':
        this.buildParsingDirectiveSpecificRegExps();
        break;
    }
  }

  /**
   * Splits a string by symbol at an index.
   *
   * @param {string} what a string to be split.
   * @param {number} where an index to split the string.
   * @return {[string,string]}
   */
  private splitBySymbolAtIndex(what: string, where: number): string[] {
    if (where < 0 || where >= what.length) {
      where = what.length;
      return [what, ''];
    }

    return [what.slice(0, where), what.slice(where + 1)];
  }

  /**
   * Parses an argument string depending on instruction
   *
   * @param instruction {string}
   * @param argumentStr {string}
   * @returns {IRecipeLine[]}
   */
  private parseArgument(instruction: string, argumentStr: string): IRecipeLine[] {
    const results: IRecipeLine[] = [];

    switch (instruction) {
      case 'ENV':
        const variables: string[][] = this.parseENVInstruction(argumentStr);
        variables.forEach((variable: string[]) => {
          results.push({
            instruction: instruction,
            argument: variable
          });
        });
        break;
      default:
        results.push({
          instruction: instruction,
          argument: argumentStr
        });
    }

    return results;
  }

  private parseENVInstruction(content: string): string[][] {
    const results: string[][] = [];

    const firstSpaceIndex = content.indexOf(' '),
          firstEqualIndex = content.indexOf('=');

    if (firstEqualIndex === -1 && firstSpaceIndex === -1) {
      throw new TypeError(`Cannot parse environment variable name and value from string "${content}"`);
    }

    if (firstSpaceIndex > -1 && (firstEqualIndex === -1 || firstSpaceIndex < firstEqualIndex)) {

      // this argument string contains only one environment variable
      let [name, value] = [content.slice(0, firstSpaceIndex), content.slice(firstSpaceIndex + 1)];

      value = this.unEscapeString(value);

      results.push([name, value]);

    } else {

      // this argument string contains one or more environment variables
      let count = 100;
      while (content.length && count) {
        count--;

        // remove a linebreak at the start of string
        content = content.replace(this.escapedLineBreakAtStartRE, '');

        // remove a whitespace at the start of string
        content = content.replace(/^\s+/, '');

        // check if string begins with variable name
        if (!this.variableNameRE.test(content)) {
          throw new TypeError(`Cannot parse environment variable name and value from string "${content}"`);
        }

        if (this.envVariableQuotedValueRE.test(content)) {

          /* variable with quoted value */

          let [fullMatch, name, value] = this.envVariableQuotedValueRE.exec(content);

          value = value.replace(this.quotesRE, '');

          results.push([name, value]);

          const parts = this.splitBySymbolAtIndex(content, fullMatch.length - 1);
          // cut processed variable from the string
          content = parts[1];
        } else {

          /* variable with escaped value */

          // look for the point where the variable ends
          const unEscapedWhitespaceMatch = this.unEscapedWhitespaceRE.exec(content);

          let variableLength: number;
          if (!unEscapedWhitespaceMatch) {
            // the rest of string is a single variable
            variableLength = content.length;
          } else {
            variableLength = unEscapedWhitespaceMatch.index + unEscapedWhitespaceMatch.length - 1;
          }

          const parts = this.splitBySymbolAtIndex(content, variableLength);
          const variableStr = parts[0];
          // cut processed variable from the string
          content = parts[1];

          const equalIndex = variableStr.indexOf('=');

          const varParts = this.splitBySymbolAtIndex(variableStr, equalIndex);
          let name = varParts[0],
              value = varParts[1];

          value = this.unEscapeString(value);

          results.push([name, value]);
        }
      }
    }

    return results;
  }

  /**
   * Dumps argument object depending on instruction.
   *
   * @param {IRecipeLine} line
   * @returns {string}
   */
  private stringifyArgument(line: IRecipeLine): string {
    switch (line.instruction) {
      case 'ENV':
        const [name, value] = line.argument as string[];
        return name + ' ' + this.escapeString(value);
      default:
        return line.argument as string;
    }
  }

  private escapeString(content: string): string {
    // this replace should be the very first
    content = content.replace(this.escapeRE, this.directives.escape + '$1');

    content = content.replace(this.linebreaksRE, this.directives.escape + '$1');

    return content;
  }

  /**
   * Unescapes whitespaces and escape symbols.
   *
   * @param {string} content a string to process.
   */
  private unEscapeString(content: string): string {
    content = content.replace(this.escapedWhitespacesAndLinebreaksRE, '$1$2');

    // this replace should be the very last
    content = content.replace(this.escapedEscapeSymbolsRE, '$1');

    return content;
  }
}
