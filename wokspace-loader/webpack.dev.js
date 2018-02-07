const merge = require('webpack-merge');
const common = require('./webpack.common.js');
module.export = merge(common, {
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