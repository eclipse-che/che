/*******************************************************************************
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

const merge = require('webpack-merge');
const common = require('./webpack.common.js');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = merge(common, {
    devtool: 'inline-source-map',
    module:{
        rules:[
            {
                test: /\.css$/,
                use: [{
                    loader: "style-loader" // creates style nodes from JS strings
                }, {
                    loader: "css-loader" // translates CSS into CommonJS
                }]
            }
        ]
    },
    devServer: {
        contentBase: './dist',
        port: 3000,
        index: 'index.html',
        historyApiFallback: true,
        proxy: {
            '/api/websocket': {
                target: 'http://localhost:8080',
                ws: true,
            },
            '/api': "http://localhost:8080",
        }
    },
    plugins:[
        new HtmlWebpackPlugin({
            inject: false,
            title: "Che Workspace Loader",
            template:"src/index.html",
            urlPrefix:"/"
        })
    ]
});
