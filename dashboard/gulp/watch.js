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

var browserSync = require('browser-sync');

function isOnlyChange(event) {
  return event.type === 'changed';
}

gulp.task('watch', ['scripts:watch', 'inject'], function () {

  gulp.watch([path.join(conf.paths.src, '/*.html')], ['inject']);

  gulp.watch([
    path.join(conf.paths.src, '/app/**/*.css'),
    path.join(conf.paths.src, '/app/**/*.styl'),
    path.join(conf.paths.src, '/components/**/*.css'),
    path.join(conf.paths.src, '/components/**/*.styl')
  ], function(event) {
    if(isOnlyChange(event)) {
      gulp.start('styles');
    } else {
      gulp.start('inject');
    }
  });


  gulp.watch([
    path.join(conf.paths.src, '/app/**/*.html'),
    path.join(conf.paths.src, '/components/**/*.html'),
  ],
    function(event) {
    browserSync.reload(event.path);
  });
});
