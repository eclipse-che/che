'use strict';

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
        process.env.MOCHA_DIRECTORY ?
            process.env.USERSTORY ?
                `dist/specs/${process.env.MOCHA_DIRECTORY}/${process.env.USERSTORY}.spec.js`
                : `dist/specs/${process.env.MOCHA_DIRECTORY}/**.spec.js`
            : `dist/specs/**/**.spec.js`,
    grep: process.env.MOCHA_SUITE ? process.env.MOCHA_SUITE : '',
    retries: process.env.MOCHA_RETRIES ? process.env.MOCHA_RETRIES : 3,
};
