'use strict';

import { TestConstants } from '../constants/TestConstants';

module.exports = {
    timeout: 1200000,
    reporter: './dist/utils/CheReporter.js',
    ui: 'tdd',
    require: [
        './dist/specs/MochaHooks.js',
        'ts-node/register',
    ],
    bail: true,
    'full-trace': true,
    spec:
        // variable MOCHA_DIRECTORY uses in command "test-all-devfiles" and sets up automatically.
        // you can set it up to run files from specific directory with export environmental variable.
        process.env.MOCHA_DIRECTORY ?
            // to run one file (name without extension). uses in "test", "test-all-devfiles".
            process.env.USERSTORY ?
                `dist/specs/${process.env.MOCHA_DIRECTORY}/${process.env.USERSTORY}.spec.js`
                : `dist/specs/${process.env.MOCHA_DIRECTORY}/**.spec.js`
            : process.env.USERSTORY ?
                `dist/specs/**/${process.env.USERSTORY}.spec.js`
                : `dist/specs/**/**.spec.js`,
    retries: TestConstants.TS_SELENIUM_DEFAULT_ATTEMPTS,
};
