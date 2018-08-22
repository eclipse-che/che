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

var $ = require('gulp-load-plugins')();

gulp.task('material-svgfonts', function() {
  return gulp.src('node_modules/material-design-icons/**/production/*')
    .pipe($.iconfontCss({
      fontName: 'material-design',
      targetPath: '../styles/material-design.css',
      fontPath: '../fonts/'
    }))
    .pipe($.iconfont({
      fontName: 'material-design',
      appendCodepoints: false,
      normalize: true,
      centerHorizontally: true,
      fontHeight: 100
    }))
    .pipe( gulp.dest('src/assets/fonts') );
});
