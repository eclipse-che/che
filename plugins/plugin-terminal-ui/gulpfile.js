// include gulp
var gulp = require('gulp');
// Clean up lib and build folders
var rimraf = require('gulp-rimraf');
// Compile TypeScript
var ts = require('gulp-typescript');
// Include browserify plugin
var browserify = require('browserify');
//Generate source map to debug source files.
var sourcemaps = require('gulp-sourcemaps');
var source = require("vinyl-source-stream");
var buffer = require('vinyl-buffer');
//Browserify TypeScript file.
var tsify = require('tsify');
//Uglify js file.
var uglify = require('gulp-uglify');

var tsProject = ts.createProject('tsconfig.json');
var tsConfigOutDir = tsProject.config.compilerOptions.outDir;
var buildDir = 'build';

/**
 * Clean up folders before build.
 */
gulp.task('clean', function () {
  return gulp.src([tsConfigOutDir, buildDir], {read: false})
    .pipe(rimraf());
});

/**
 * Browserify and uglify xterm.js script with addons to the build folder, generate source map
 */
gulp.task('browserify', function() {
  // Single entry point to browserify
  return browserifyTask = browserify({
    debug : true,
    standalone: "Terminal",
    basedir: buildDir,
    entries: ['../src/xterm.js', '../src/addons/fit/fit.js']
  })
    .plugin(tsify)
    .bundle()
    .pipe(source('xterm.js'))
    .pipe(buffer())
    .pipe(sourcemaps.init({loadMaps: true}))
    .pipe(uglify())
    .pipe(sourcemaps.write('./', {sourceRoot: '.'}))
    .pipe(gulp.dest(buildDir))
});

/**
 * Main build task
 */
gulp.task('build', ['clean', 'browserify']);

/**
 *  Default task clean temporaries directories and launch the main build task
 */
gulp.task('default', function () {
  return gulp.start('build');
});
