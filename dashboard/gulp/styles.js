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
var concat = require('gulp-concat');
var merge = require('merge-stream');

var browserSync = require('browser-sync');

var $ = require('gulp-load-plugins')();

gulp.task('styles', function () {

  var injectFiles = gulp.src([
    path.join(conf.paths.src, '/{app,components}/**/*.styl'),
    path.join('!' + conf.paths.src, '/app/index.styl')
  ], { read: false });

  var injectOptions = {
    transform: function(filePath) {
      filePath = filePath.replace(conf.paths.src + '/app/', '');
      filePath = filePath.replace(conf.paths.src + '/components/', '../components/');
      return '@import "' + filePath + '";';
    },
    starttag: '// injector',
    endtag: '// endinjector',
    addRootSlash: false
  };


  var stylCss =  gulp.src([
    path.join(conf.paths.src, '/app/index.styl')
  ])
    .pipe($.inject(injectFiles, injectOptions))
    .pipe($.sourcemaps.init())
    .pipe($.stylus()).on('error', conf.errorHandler('Stylus'))
    .pipe($.autoprefixer()).on('error', conf.errorHandler('Autoprefixer'))
    .pipe($.sourcemaps.write())
    .pipe(gulp.dest(path.join(conf.paths.tmp, '/serve/app/')))
    .pipe(browserSync.reload({ stream: true }));

  var css = gulp.src([
    path.join(conf.paths.modules, '/font-awesome/css/font-awesome.css'),
    path.join(conf.paths.modules, '/angular-material/**/*.css'),
    path.join(conf.paths.modules, '/codemirror/lib/codemirror.css'),
    path.join(conf.paths.modules, '/codemirror/addon/lint/lint.css'),
    path.join(conf.paths.modules, '/codemirror/addon/fold/foldgutter.css')
  ])
    .pipe(concat('css-files.css'));

  return merge(stylCss, css)
    .pipe(gulp.dest(path.join(conf.paths.tmp, '/serve/app/')))
    .pipe(browserSync.reload({stream: true}));
});
