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
    this.logText(
      indentLevel,
      `[ERROR] ${this.getCallerInfo()} ${this.separator(text)} ${text}`
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
    this.logText(
      indentLevel,
      `[WARN] ${this.getCallerInfo()} ${this.separator(text)} ${text}`
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
    this.logText(
      indentLevel,
      `• ${this.getCallerInfo()} ${this.separator(text)} ${text}`
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
    this.logText(
      indentLevel,
      `▼ ${this.getCallerInfo()} ${this.separator(text)} ${text}`
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
    this.logText(
      indentLevel,
      `‣ ${this.getCallerInfo()} ${this.separator(text)} ${text}`
    );
  }

  private static logText(messageIndentationLevel: number, text: string): void {
    // start group for every level
    for (let i: number = 0; i < messageIndentationLevel; i++) {
      console.group();
    }
    // print the trimmed text
    // if multiline, the message should be properly padded
    console.log(text);
    // end group for every level
    for (let i: number = 0; i < messageIndentationLevel; i++) {
      console.groupEnd();
    }
  }

  private static getCallerInfo(): string {
    const e: Error = new Error();
    const stack: string[] = e.stack ? e.stack.split('\n') : [];
    // " at functionName ( ..." => "functionName"
    return stack[3].replace(/^\s+at\s+(.+?)\s.+/g, '$1');
  }

  private static separator(text: string): string {
    return text ? '-' : '';
  }
}
