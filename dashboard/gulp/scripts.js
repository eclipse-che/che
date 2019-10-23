/*******************************************************************************
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');
var bootstrap = require('bootstrap-styl');

var browserSync = require('browser-sync');
var webpackstream = require('webpack-stream');
var webpack = require('webpack');
var glob = require("glob")

var $ = require('gulp-load-plugins')();

function webpackWrapper(watch, test, callback) {
  var webpackOptions = {
    context: __dirname,
    resolve: {extensions: ['', '.ts', '.js', '.styl']},
    watch: watch,
    module: {
      noParse: [/jsonlint/],
      loaders: [
        {
          test: /node_modules[\\\\|\/](vscode-languageserver-types|vscode-uri|jsonc-parser|vscode-json-languageservice)/,
          loader: 'umd-compat-loader'
        },
        {
          test: /\.min\.js\.map$/,
          include: /node_modules\/angular-websocket\/dist/,
          loader: 'file-loader'
        },
        {
          test: /\.ts$/,
          exclude: /node_modules/,
          loaders: ['babel-loader', 'awesome-typescript-loader']
        },
        {
          test: /\.js$/,
          include: /node_modules\/(monaco-editor-core|monaco-languages|vscode-languageserver|vscode-languageserver-types|yaml-language-server)/,
          loaders: ['babel-loader']
        },
        {
          test: /\.css$/,
          loaders: ['style-loader', 'css-loader']
        },
        {
          test: /\.styl$/,
          loaders: [
            'style-loader',
            'css-loader',
            {
              loader: 'stylus-loader?paths=node_modules/bootstrap-styl',
              options: {
                preferPathResolver: 'webpack',
                use: [bootstrap()]
              }
            }
          ]
        },
        {
          test: /\.(svg|woff|woff2|ttf|eot|ico)$/,
          loader: 'file-loader'
        }, {
          test: /\.html$/,
          loaders: [
            {
              loader: 'ngtemplate-loader',
              options: {
                angular: true
              }
            }, 'html-loader']
        }
      ]
    },
    output: {filename: '[name].module.js', globalObject: 'self'},
    target: 'web',
    node: {
      fs: 'empty',
      net: 'empty',
      module: 'empty'
    },
    entry: {
      index: [path.resolve(__dirname, '..', 'src', 'index.ts')],
      "editor.worker": 'monaco-editor-core/esm/vs/editor/editor.worker.js'
    },
    plugins: [
      new webpack.IgnorePlugin(/prettier/)
    ]
  };

  if (watch) {
    webpackOptions.devtool = 'inline-source-map';
  }

  var callbackCalled = false;

  var webpackChangeHandler = function (err, stats) {
    $.util.log(stats.toString({
      colors: $.util.colors.supportsColor,
      chunks: false,
      hash: false,
      version: false
    }));

    if (!watch && stats.hasErrors()) {
      $.util.log($.util.colors.red('[Webpack task failed with errors]'));
      process.exit(1);
    }

    if (watch && !callbackCalled) {
        callbackCalled = true;
        callback();
    }
  };

  var sources = [path.join(conf.paths.src, '/index.ts')];
  if (test) {
    sources.push(path.join(conf.paths.src, '/{app,components}/**/*.spec.ts'));

    const appTestGlob = glob.sync(path.resolve(__dirname, '..', path.join(conf.paths.src, '/app/**/*.spec.ts')));
    webpackOptions.entry.index.push(...appTestGlob);

    const componentsTestGlob = glob.sync(path.resolve(__dirname, '..', path.join(conf.paths.src, '/components/**/*.spec.ts')));
    webpackOptions.entry.index.push(...componentsTestGlob);
  }

  return gulp.src(sources)
    .pipe(webpackstream(webpackOptions, null, webpackChangeHandler))
    .pipe(gulp.dest(path.join(conf.paths.tmp, '/serve/app')));
}

gulp.task('scripts', ['colors', 'proxySettings'], function () {
  return webpackWrapper(false, false);
});

gulp.task('scripts:watch', ['colors', 'proxySettings'], function (callback) {
  return webpackWrapper(true, false, callback);
});

gulp.task('scripts:test', ['colors', 'outputcolors', 'proxySettings'], function () {
  return webpackWrapper(false, true);
});

gulp.task('scripts:test-watch', ['scripts'], function (callback) {
  return webpackWrapper(true, true, callback);
});
