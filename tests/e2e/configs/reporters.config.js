/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
const { REPORTER_CONSTANTS } = require('../constants/REPORTER_CONSTANTS');

module.exports = {
	reporterEnabled: REPORTER_CONSTANTS.REPORTERS_ENABLED(),
	allureMochaReporterOptions: {
		resultsDir: '.allure-results'
	},

	reporterOptions: {
		mochaFile: `${REPORTER_CONSTANTS.TS_SELENIUM_REPORT_FOLDER}/junit/test-results.xml`
	}
};
