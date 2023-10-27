/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import { REPORTER_CONSTANTS } from '../constants/REPORTER_CONSTANTS';
import { rpApi } from '../specs/MochaHooks';

export class Logger {
	/**
	 * uses for logging of fatal errors.
	 * @param text log text
	 * @param indentLevel log level
	 */

	static error(text: string = '', indentLevel: number = 1): void {
		const callerInfo: string = this.getCallerInfo();
		const logLevelSymbol: string = '[ERROR] ';
		const message: string = this.getFullMessage(callerInfo, text);
		this.logText(indentLevel, logLevelSymbol, message);
		if (this.sendLogMessageIntoReportPortal()) {
			rpApi.error(message);
		}
	}

	/**
	 * uses for logging of recoverable errors and general warnings.
	 * @param text log text
	 * @param indentLevel log level
	 */
	static warn(text: string = '', indentLevel: number = 1): void {
		if (REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'ERROR') {
			return;
		}
		const callerInfo: string = this.getCallerInfo();
		const logLevelSymbol: string = '[WARN] ';
		const message: string = this.getFullMessage(callerInfo, text);
		this.logText(indentLevel, logLevelSymbol, message);
		if (this.sendLogMessageIntoReportPortal()) {
			rpApi.warn(message);
		}
	}

	/**
	 * uses for logging of the public methods of the pageobjects.
	 * @param text log text
	 * @param indentLevel log level
	 */
	static info(text: string = '', indentLevel: number = 3): void {
		if (REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'ERROR' || REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'WARN') {
			return;
		}
		const callerInfo: string = this.getCallerInfo();
		const logLevelSymbol: string = '• ';
		const message: string = this.getFullMessage(callerInfo, text);
		this.logText(indentLevel, logLevelSymbol, message);
		if (this.sendLogMessageIntoReportPortal()) {
			rpApi.info(message);
		}
	}

	/**
	 * uses for logging of the public methods of the pageobjects.
	 * @param text log text
	 * @param indentLevel log level
	 */
	static debug(text: string = '', indentLevel: number = 5): void {
		if (
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'WARN' ||
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'INFO'
		) {
			return;
		}
		const callerInfo: string = this.getCallerInfo();
		const logLevelSymbol: string = '▼ ';
		const message: string = this.getFullMessage(callerInfo, text);
		this.logText(indentLevel, logLevelSymbol, message);
		if (this.sendLogMessageIntoReportPortal()) {
			rpApi.debug(message);
		}
	}

	/**
	 * uses for logging of the public methods of the {@link DriverHelper} or
	 * private methods inside of pageobjects.
	 * @param text log text
	 * @param indentLevel log level
	 */
	static trace(text: string = '', indentLevel: number = 6): void {
		if (
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'ERROR' ||
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'WARN' ||
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'INFO' ||
			REPORTER_CONSTANTS.TS_SELENIUM_LOG_LEVEL === 'DEBUG'
		) {
			return;
		}
		const callerInfo: string = this.getCallerInfo();
		const logLevelSymbol: string = '‣ ';
		const message: string = this.getFullMessage(callerInfo, text);
		this.logText(indentLevel, logLevelSymbol, message);
		if (this.sendLogMessageIntoReportPortal()) {
			rpApi.trace(message);
		}
	}

	private static getFullMessage(callerInfo: string, text: string): string {
		return `${callerInfo}${this.separator(text, callerInfo)}${text}`;
	}

	private static separator(text: string, caller: string): string {
		return text ? (caller ? ' - ' : '') : '';
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

	private static getCallerInfo(i: number = 4): string {
		const stack: string[] = this.getCallStackArray();
		// " at functionName ( ..." => "functionName"
		return stack[i].includes('.<anonymous') ? '' : stack[i].replace(/^\s+at\s+(.+?)\s.+/g, '$1');
	}

	private static getCallStackArray(): string[] {
		const e: Error = new Error();
		return e.stack ? e.stack.split('\n') : [];
	}

	private static sendLogMessageIntoReportPortal(): boolean {
		return REPORTER_CONSTANTS.SAVE_RP_REPORT_DATA && !this.isRootCaller();
	}

	private static isRootCaller(traceLevel: number = 6): boolean {
		return this.getCallStackArray()
			.slice(traceLevel, traceLevel + 2)
			.reduce((acc, e): boolean => {
				return acc || /MochaHooks|CheReporter/.test(e);
			}, false);
	}
}
