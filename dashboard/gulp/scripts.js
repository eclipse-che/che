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
var webpack = require('webpack-stream');

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
          test: /\.ts$/,
          exclude: /node_modules/,
          loaders: ['babel-loader', 'awesome-typescript-loader']
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
    output: {filename: 'index.module.js'}
  };

  if (watch) {
    webpackOptions.devtool = 'inline-source-map';
  }

  var webpackChangeHandler = function (err, stats) {
    if (err) {
      conf.errorHandler('Webpack')(err);
    }
    $.util.log(stats.toString({
      colors: $.util.colors.supportsColor,
      chunks: false,
      hash: false,
      version: false
    }));
    browserSync.reload();
    if (watch) {
      watch = false;
      callback();
    }
  };

  var sources = [path.join(conf.paths.src, '/index.ts')];
  if (test) {
    sources.push(path.join(conf.paths.src, '/{app,components}/**/*.spec.ts'));
  }

  return gulp.src(sources)
    .pipe(webpack(webpackOptions, null, webpackChangeHandler))
    .pipe(gulp.dest(path.join(conf.paths.tmp, '/serve/app')));
}

gulp.task('scripts', ['colors', 'proxySettings'], function () {
  return webpackWrapper(false, false);
});

gulp.task('scripts:watch', ['scripts'], function (callback) {
  return webpackWrapper(true, false, callback);
});

gulp.task('scripts:test', ['colors', 'outputcolors', 'proxySettings'], function () {
  return webpackWrapper(false, true);
});

gulp.task('scripts:test-watch', ['scripts'], function (callback) {
  return webpackWrapper(true, true, callback);
});
