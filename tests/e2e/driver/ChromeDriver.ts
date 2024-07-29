/** *******************************************************************
 * copyright (c) 2019-2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
import 'chromedriver';
import 'reflect-metadata';
import { injectable } from 'inversify';
import { Builder, ThenableWebDriver } from 'selenium-webdriver';
import { IDriver } from './IDriver';
import { Options } from 'selenium-webdriver/chrome';
import { CHROME_DRIVER_CONSTANTS } from '../constants/CHROME_DRIVER_CONSTANTS';

@injectable()
export class ChromeDriver implements IDriver {
	private readonly driver: ThenableWebDriver | undefined;

	constructor() {
		const options: Options = this.getDriverOptions();
		options.setLoggingPrefs({ performance: 'ALL' });
		if (CHROME_DRIVER_CONSTANTS.TS_USE_WEB_DRIVER_FOR_TEST) {
			this.driver = this.getDriverBuilder(options).build();
		}
	}

	get(): ThenableWebDriver {
		return this.driver as ThenableWebDriver;
	}

	private getDriverOptions(): Options {
		let options: Options = new Options()
			.addArguments('--no-sandbox')
			.addArguments('--disable-web-security')
			.addArguments('--allow-running-insecure-content')
			.addArguments('--ignore-certificate-errors');

		// if 'true' run in 'headless' mode
		if (CHROME_DRIVER_CONSTANTS.TS_SELENIUM_HEADLESS) {
			options = options.addArguments('headless');
		}

        if (CHROME_DRIVER_CONSTANTS.TS_SELENIUM_PROXY_SERVER !== '') {
			options = options.addArguments('--proxy-server=' + CHROME_DRIVER_CONSTANTS.TS_SELENIUM_PROXY_SERVER);
		}

		return options;
	}

	private getDriverBuilder(options: Options): Builder {
		const disableW3copts: object = { 'goog:chromeOptions': { w3c: false } };
		let builder: Builder = new Builder().forBrowser('chrome').setChromeOptions(options);

		// if 'false' w3c protocol is disabled
		if (!CHROME_DRIVER_CONSTANTS.TS_SELENIUM_W3C_CHROME_OPTION) {
			builder.withCapabilities(disableW3copts).forBrowser('chrome').setChromeOptions(options);
		}

		// if 'true' run with remote driver
		if (CHROME_DRIVER_CONSTANTS.TS_SELENIUM_REMOTE_DRIVER_URL) {
			builder = builder.usingServer(CHROME_DRIVER_CONSTANTS.TS_SELENIUM_REMOTE_DRIVER_URL);
		}

		return builder;
	}
}
