/*******************************************************************************
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

'use strict';

var path = require('path');
var gulp = require('gulp');
var conf = require('./conf');

var $ = require('gulp-load-plugins')();

gulp.task('generatesvgfonts', function() {

  var sources = [ path.join(conf.paths.src, '/assets/svg/type-blank.svg'), path.join(conf.paths.src, '/assets/svg/*.svg') ];

  return gulp.src(sources)
    .pipe($.iconfontCss({
      fontName: 'che',
      targetPath: '../styles/che-font.css',
      fontPath: '../fonts/'
    }))
    .pipe($.iconfont({
      fontName: 'che',
      appendCodepoints: false,
      normalize: true,
      svg: true,
      centerHorizontally: true,
      fontHeight: 100
    }))
    .pipe( gulp.dest('src/assets/fonts') );
});



gulp.task('svgfonts', ['generatesvgfonts'], function () {
  return gulp.src(['src/assets/styles/che-font.css'])
    .pipe($.replace('icon:before', 'chefont:before'))
    .pipe($.replace('.icon-', '.cheico-'))
    .pipe(gulp.dest('src/assets/styles'));

});
