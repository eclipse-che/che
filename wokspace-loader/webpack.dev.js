/*******************************************************************************
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

const merge = require('webpack-merge');
const common = require('./webpack.common.js');
module.exports = merge(common, {
    devtool: 'inline-source-map',
    devServer: {
        contentBase: './dist',
        port: 3050,
        index: 'index.html',
        historyApiFallback: true,
        proxy: {
            '/api/websocket': {
                target: 'http://localhost:8080',
                ws: true,
            },
            '/api/workspace': "http://localhost:8080",
        }
    }
});