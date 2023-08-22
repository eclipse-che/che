/*********************************************************************
 * Copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { ReporterConstants } from '../constants/ReporterConstants';

export abstract class Logger {
  /**
   * Uses for logging of fatal errors.
   * @param text log text
   * @param indentLevel log level
   */
  static error(text: string = '', indentLevel: number = 1): void {
    const callerInfo: string = this.getCallerInfo();
    const logLevelSymbol: string = '[ERROR] ';
    this.logText(
      indentLevel,
      logLevelSymbol,
      `${this.getFullMessage(callerInfo, text)}`
    );
  }

  /**
   * Uses for logging of recoverable errors and general warnings.
   * @param text log text
   * @param indentLevel log level
   */
  static warn(text: string = '', indentLevel: number = 1): void {
    if (ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR') {
      return;
    }
    const callerInfo: string = this.getCallerInfo();
    const logLevelSymbol: string = '[WARN] ';
    this.logText(
      indentLevel,
      logLevelSymbol,
      `${this.getFullMessage(callerInfo, text)}`
    );
  }

  /**
   * Uses for logging of the public methods of the pageobjects.
   * @param text log text
   * @param indentLevel log level
   */
  static info(text: string = '', indentLevel: number = 3): void {
    if (
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'WARN'
    ) {
      return;
    }
    const callerInfo: string = this.getCallerInfo();
    const logLevelSymbol: string = '• ';
    this.logText(
      indentLevel,
      logLevelSymbol,
      `${this.getFullMessage(callerInfo, text)}`
    );
  }

  /**
   * Uses for logging of the public methods of the pageobjects.
   * @param text log text
   * @param indentLevel log level
   */
  static debug(text: string = '', indentLevel: number = 5): void {
    if (
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'WARN' ||
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'INFO'
    ) {
      return;
    }
    const callerInfo: string = this.getCallerInfo();
    const logLevelSymbol: string = '▼ ';
    this.logText(
      indentLevel,
      logLevelSymbol,
      `${this.getFullMessage(callerInfo, text)}`
    );
  }

  /**
   * Uses for logging of the public methods of the {@link DriverHelper} or
   * private methods inside of pageobjects.
   * @param text log text
   * @param indentLevel log level
   */
  static trace(text: string = '', indentLevel: number = 6): void {
    if (
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'WARN' ||
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'INFO' ||
      ReporterConstants.TS_SELENIUM_LOG_LEVEL === 'DEBUG'
    ) {
      return;
    }
    const callerInfo: string = this.getCallerInfo();
    const logLevelSymbol: string = '‣ ';
    this.logText(
      indentLevel,
      logLevelSymbol,
      `${this.getFullMessage(callerInfo, text)}`
    );
  }

  private static getFullMessage(callerInfo: string, text: string): string {
    return `${callerInfo}${this.separator(text, callerInfo)}${text}`;
  }

  private static logText(messageIndentationLevel: number, logLevelSymbol: string, text: string): void {
    if (text) {
      // start group for every level
      for (let i: number = 0; i < messageIndentationLevel; i++) {
        console.group();
      }
      // print the trimmed text
      // if multiline, the message should be properly padded
      console.log(logLevelSymbol + text);
      // end group for every level
      for (let i: number = 0; i < messageIndentationLevel; i++) {
        console.groupEnd();
      }
    }
  }

  private static getCallerInfo(): string {
    const e: Error = new Error();
    const stack: string[] = e.stack ? e.stack.split('\n') : [];
    // " at functionName ( ..." => "functionName"
    return stack[3].includes('.<anonymous') ? '' : stack[3].replace(/^\s+at\s+(.+?)\s.+/g, '$1');
  }

  private static separator(text: string, caller: string): string {
    return text ? caller ? ' - ' : '' : '';
  }
}
