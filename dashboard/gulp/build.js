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

var $ = require('gulp-load-plugins')({
  pattern: ['gulp-*', 'uglify-save-license', 'del']
});

var minimist = require('minimist');

var serverOptions = {
  string: 'server',
  default: {server: 'http://localhost:8080'}
};

var options = minimist(process.argv.slice(2), serverOptions);

gulp.task('partials', function () {
  return gulp.src([
      path.join(conf.paths.src, '/{app,components}/**/*.html'),
      path.join(conf.paths.tmp, '/serve/{app,components}/**/*.html')
    ])
    .pipe($.minifyHtml({
      empty: true,
      spare: true,
      quotes: true
    }))
    .pipe($.angularTemplatecache('templateCacheHtml.js', {
      module: 'userDashboard'
    }))
    .pipe(gulp.dest(conf.paths.tmp + '/partials/'));
});

gulp.task('html', ['inject', 'partials'], function () {
  var partialsInjectFile = gulp.src(path.join(conf.paths.tmp, '/partials/templateCacheHtml.js'), { read: false });
  var partialsInjectOptions = {
    starttag: '<!-- inject:partials -->',
    ignorePath: path.join(conf.paths.tmp, '/partials'),
    addRootSlash: false
  };
  var htmlFilter = $.filter('*.html', { restore: true });
  var jsFilter = $.filter('**/*.js', { restore: true });
  var cssFilter = $.filter('**/*.css', { restore: true });
  var assets;

  return gulp.src(path.join(conf.paths.tmp, '/serve/*.html'))
    .pipe($.inject(partialsInjectFile, partialsInjectOptions))
    .pipe(assets = $.useref.assets())
    .pipe($.rev())
    .pipe(jsFilter)
    .pipe($.sourcemaps.init())
    .pipe($.uglify({ preserveComments: $.uglifySaveLicense })).on('error', conf.errorHandler('Uglify'))
    .pipe($.sourcemaps.write('maps'))
    .pipe(jsFilter.restore)
    .pipe(cssFilter)
    .pipe($.sourcemaps.init())
    .pipe($.minifyCss({ processImport: false }))
    .pipe($.sourcemaps.write('maps'))
    .pipe(cssFilter.restore)
    .pipe(assets.restore())
    .pipe($.useref())
    .pipe($.revReplace())
    .pipe(htmlFilter)
    .pipe($.minifyHtml({
      empty: true,
      spare: true,
      quotes: true,
      conditionals: true
    }))
    .pipe(htmlFilter.restore)
    .pipe(gulp.dest(path.join(conf.paths.dist, '/')))
    .pipe($.size({ title: path.join(conf.paths.dist, '/'), showFiles: true }));
});

gulp.task('images', function () {
  return gulp.src(conf.paths.src + '/assets/images/**/*')
    .pipe(gulp.dest(conf.paths.dist + '/assets/images/'));
});


gulp.task('htmlassets', function () {
  return gulp.src(conf.paths.src + '/assets/html/**/*')
    .pipe(gulp.dest(conf.paths.dist + '/assets/html/'));
});

gulp.task('brandingassets', function () {
  return gulp.src(conf.paths.src + '/assets/branding/**/*')
    .pipe(gulp.dest(conf.paths.dist + '/assets/branding/'));
});

gulp.task('existingfonts', function () {
  return gulp.src(conf.paths.src + '/assets/fonts/*')
    .pipe($.filter('**/*.{eot,svg,ttf,otf,woff,woff2}'))
    .pipe($.flatten())
    .pipe(gulp.dest(conf.paths.dist + '/fonts/'));
});

gulp.task('fonts', ['colors', 'outputcolors', 'proxySettings', 'existingfonts'], function () {
  return gulp.src([
    'node_modules/material-design-iconfont/iconfont/*',
    'node_modules/font-awesome/fonts/*'
  ])
    .pipe($.filter('**/*.{eot,svg,ttf,woff,woff2}'))
    .pipe($.flatten())
    .pipe(gulp.dest(path.join(conf.paths.dist, '/fonts/')));
});

var fs = require('fs');
gulp.task('colorstemplate', function () {
  return gulp.src('src/app/colors/che-color.constant.ts.template')
    .pipe($.replace('%CONTENT%', fs.readFileSync('src/app/colors/che-colors.json')))
    .pipe($.replace('\"', '\''))
    .pipe(gulp.dest('src/app/colors/template'));
});

gulp.task('colors', ['colorstemplate'], function () {
  return gulp.src("src/app/colors/template/che-color.constant.ts.template")
    .pipe($.rename("che-color.constant.ts"))
    .pipe(gulp.dest("src/app/colors"));
});

gulp.task('outputcolorstemplate', function () {
  return gulp.src('src/app/colors/che-output-colors.constant.ts.template')
    .pipe($.replace('%CONTENT%', fs.readFileSync('src/app/colors/che-output-colors.json')))
    .pipe($.replace('\"', '\''))
    .pipe(gulp.dest('src/app/colors/template'));
});

gulp.task('outputcolors', ['outputcolorstemplate'], function () {
  return gulp.src("src/app/colors/template/che-output-colors.constant.ts.template")
    .pipe($.rename("che-output-colors.constant.ts"))
    .pipe(gulp.dest("src/app/colors"));
});

gulp.task('proxySettingsTemplate', function () {
  return gulp.src("src/app/proxy/proxy-settings.constant.ts.template")
    .pipe($.replace('%CONTENT%', options.server))
    .pipe(gulp.dest('src/app/proxy/template'));
});

gulp.task('proxySettings', ['proxySettingsTemplate'], function () {
  return gulp.src("src/app/proxy/template/proxy-settings.constant.ts.template")
    .pipe($.rename("proxy-settings.constant.ts"))
    .pipe(gulp.dest("src/app/proxy"));
});


gulp.task('other', function () {
  var fileFilter = $.filter(function (file) {
    return file.stat.isFile();
  });

  return gulp.src([
      path.join(conf.paths.src, '/**/*'),
      path.join('!' + conf.paths.src, '/**/*.{html,css,js,styl}')
    ])
    .pipe(fileFilter)
    .pipe(gulp.dest(path.join(conf.paths.dist, '/')));
});

gulp.task('clean', function () {
  return $.del([path.join(conf.paths.dist, '/'), path.join(conf.paths.tmp, '/')]);
});


gulp.task('build', ['html', 'images', 'htmlassets', 'brandingassets', 'fonts', 'other']);
