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

'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');

var $ = require('gulp-load-plugins')();

gulp.task('material-svgfonts', function() {
  return gulp.src('bower_components/material-design-icons/**/production/*')
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
