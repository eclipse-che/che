const webpack = require('@cypress/webpack-preprocessor')
module.exports = on => {
  const options = {
    // send in the options from your webpack.config.js, so it works the same
    // as your app's code
    webpackOptions: require('../../webpack.config'),
    watchOptions: {}
  }

  on('file:preprocessor', webpack(options))
}
