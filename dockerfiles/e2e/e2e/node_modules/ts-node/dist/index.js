"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
Object.defineProperty(exports, "__esModule", { value: true });
var path_1 = require("path");
var sourceMapSupport = require("source-map-support");
var yn_1 = require("yn");
var make_error_1 = require("make-error");
var util = require("util");
/**
 * @internal
 */
exports.INSPECT_CUSTOM = util.inspect.custom || 'inspect';
/**
 * Debugging `ts-node`.
 */
var shouldDebug = yn_1.default(process.env.TS_NODE_DEBUG);
var debug = shouldDebug ? console.log.bind(console, 'ts-node') : function () { return undefined; };
var debugFn = shouldDebug ?
    function (key, fn) {
        var i = 0;
        return function (x) {
            debug(key, x, ++i);
            return fn(x);
        };
    } :
    function (_, fn) { return fn; };
/**
 * Export the current version.
 */
exports.VERSION = require('../package.json').version;
/**
 * Track the project information.
 */
var MemoryCache = /** @class */ (function () {
    function MemoryCache(rootFileNames) {
        if (rootFileNames === void 0) { rootFileNames = []; }
        this.rootFileNames = rootFileNames;
        this.fileContents = new Map();
        this.fileVersions = new Map();
        for (var _i = 0, rootFileNames_1 = rootFileNames; _i < rootFileNames_1.length; _i++) {
            var fileName = rootFileNames_1[_i];
            this.fileVersions.set(fileName, 1);
        }
    }
    return MemoryCache;
}());
/**
 * Default register options.
 */
exports.DEFAULTS = {
    files: yn_1.default(process.env['TS_NODE_FILES']),
    pretty: yn_1.default(process.env['TS_NODE_PRETTY']),
    compiler: process.env['TS_NODE_COMPILER'],
    compilerOptions: parse(process.env['TS_NODE_COMPILER_OPTIONS']),
    ignore: split(process.env['TS_NODE_IGNORE']),
    project: process.env['TS_NODE_PROJECT'],
    skipIgnore: yn_1.default(process.env['TS_NODE_SKIP_IGNORE']),
    skipProject: yn_1.default(process.env['TS_NODE_SKIP_PROJECT']),
    ignoreDiagnostics: split(process.env['TS_NODE_IGNORE_DIAGNOSTICS']),
    typeCheck: yn_1.default(process.env['TS_NODE_TYPE_CHECK']),
    transpileOnly: yn_1.default(process.env['TS_NODE_TRANSPILE_ONLY'])
};
/**
 * Default TypeScript compiler options required by `ts-node`.
 */
var TS_NODE_COMPILER_OPTIONS = {
    sourceMap: true,
    inlineSourceMap: false,
    inlineSources: true,
    declaration: false,
    noEmit: false,
    outDir: '$$ts-node$$'
};
/**
 * Split a string array of values.
 */
function split(value) {
    return typeof value === 'string' ? value.split(/ *, */g) : undefined;
}
exports.split = split;
/**
 * Parse a string as JSON.
 */
function parse(value) {
    return typeof value === 'string' ? JSON.parse(value) : undefined;
}
exports.parse = parse;
/**
 * Replace backslashes with forward slashes.
 */
function normalizeSlashes(value) {
    return value.replace(/\\/g, '/');
}
exports.normalizeSlashes = normalizeSlashes;
/**
 * TypeScript diagnostics error.
 */
var TSError = /** @class */ (function (_super) {
    __extends(TSError, _super);
    function TSError(diagnosticText, diagnosticCodes) {
        var _this = _super.call(this, "\u2A2F Unable to compile TypeScript:\n" + diagnosticText) || this;
        _this.diagnosticText = diagnosticText;
        _this.diagnosticCodes = diagnosticCodes;
        _this.name = 'TSError';
        return _this;
    }
    /**
     * @internal
     */
    TSError.prototype[exports.INSPECT_CUSTOM] = function () {
        return this.diagnosticText;
    };
    return TSError;
}(make_error_1.BaseError));
exports.TSError = TSError;
/**
 * Register TypeScript compiler.
 */
function register(opts) {
    if (opts === void 0) { opts = {}; }
    var options = Object.assign({}, exports.DEFAULTS, opts);
    var originalJsHandler = require.extensions['.js'];
    var ignoreDiagnostics = [
        6059,
        18002,
        18003
    ].concat((options.ignoreDiagnostics || [])).map(Number);
    var ignore = options.skipIgnore ? [] : (options.ignore || ['/node_modules/']).map(function (str) { return new RegExp(str); });
    // Require the TypeScript compiler and configuration.
    var cwd = process.cwd();
    var typeCheck = options.typeCheck === true || options.transpileOnly !== true;
    var compiler = require.resolve(options.compiler || 'typescript', { paths: [cwd, __dirname] });
    var ts = require(compiler);
    var transformers = options.transformers || undefined;
    var readFile = options.readFile || ts.sys.readFile;
    var fileExists = options.fileExists || ts.sys.fileExists;
    var config = readConfig(cwd, ts, fileExists, readFile, options);
    var configDiagnosticList = filterDiagnostics(config.errors, ignoreDiagnostics);
    var extensions = ['.ts'];
    var outputCache = new Map();
    var diagnosticHost = {
        getNewLine: function () { return ts.sys.newLine; },
        getCurrentDirectory: function () { return cwd; },
        getCanonicalFileName: function (path) { return path; }
    };
    // Install source map support and read from memory cache.
    sourceMapSupport.install({
        environment: 'node',
        retrieveFile: function (path) {
            return outputCache.get(path) || '';
        }
    });
    var formatDiagnostics = process.stdout.isTTY || options.pretty
        ? ts.formatDiagnosticsWithColorAndContext
        : ts.formatDiagnostics;
    function createTSError(diagnostics) {
        var diagnosticText = formatDiagnostics(diagnostics, diagnosticHost);
        var diagnosticCodes = diagnostics.map(function (x) { return x.code; });
        return new TSError(diagnosticText, diagnosticCodes);
    }
    // Render the configuration errors and exit the script.
    if (configDiagnosticList.length)
        throw createTSError(configDiagnosticList);
    // Enable additional extensions when JSX or `allowJs` is enabled.
    if (config.options.jsx)
        extensions.push('.tsx');
    if (config.options.allowJs)
        extensions.push('.js');
    if (config.options.jsx && config.options.allowJs)
        extensions.push('.jsx');
    /**
     * Get the extension for a transpiled file.
     */
    var getExtension = config.options.jsx === ts.JsxEmit.Preserve ?
        (function (path) { return /\.[tj]sx$/.test(path) ? '.jsx' : '.js'; }) :
        (function (_) { return '.js'; });
    /**
     * Create the basic required function using transpile mode.
     */
    var getOutput = function (code, fileName, lineOffset) {
        if (lineOffset === void 0) { lineOffset = 0; }
        var result = ts.transpileModule(code, {
            fileName: fileName,
            transformers: transformers,
            compilerOptions: config.options,
            reportDiagnostics: true
        });
        var diagnosticList = result.diagnostics ?
            filterDiagnostics(result.diagnostics, ignoreDiagnostics) :
            [];
        if (diagnosticList.length)
            throw createTSError(diagnosticList);
        return [result.outputText, result.sourceMapText];
    };
    var getTypeInfo = function (_code, _fileName, _position) {
        throw new TypeError("Type information is unavailable without \"--type-check\"");
    };
    // Use full language services when the fast option is disabled.
    if (typeCheck) {
        var memoryCache_1 = new MemoryCache(config.fileNames);
        // Create the compiler host for type checking.
        var serviceHost = {
            getScriptFileNames: function () { return memoryCache_1.rootFileNames; },
            getScriptVersion: function (fileName) {
                var version = memoryCache_1.fileVersions.get(fileName);
                return version === undefined ? '' : version.toString();
            },
            getScriptSnapshot: function (fileName) {
                var contents = memoryCache_1.fileContents.get(fileName);
                // Read contents into TypeScript memory cache.
                if (contents === undefined) {
                    contents = readFile(fileName);
                    if (contents === undefined)
                        return;
                    memoryCache_1.fileVersions.set(fileName, 1);
                    memoryCache_1.fileContents.set(fileName, contents);
                }
                return ts.ScriptSnapshot.fromString(contents);
            },
            fileExists: debugFn('fileExists', fileExists),
            readFile: debugFn('readFile', readFile),
            readDirectory: debugFn('readDirectory', ts.sys.readDirectory),
            getDirectories: debugFn('getDirectories', ts.sys.getDirectories),
            directoryExists: debugFn('directoryExists', ts.sys.directoryExists),
            realpath: debugFn('realpath', ts.sys.realpath),
            getNewLine: function () { return ts.sys.newLine; },
            useCaseSensitiveFileNames: function () { return ts.sys.useCaseSensitiveFileNames; },
            getCurrentDirectory: function () { return cwd; },
            getCompilationSettings: function () { return config.options; },
            getDefaultLibFileName: function () { return ts.getDefaultLibFilePath(config.options); },
            getCustomTransformers: function () { return transformers; }
        };
        var registry = ts.createDocumentRegistry(ts.sys.useCaseSensitiveFileNames, cwd);
        var service_1 = ts.createLanguageService(serviceHost, registry);
        // Set the file contents into cache manually.
        var updateMemoryCache_1 = function (contents, fileName) {
            var fileVersion = memoryCache_1.fileVersions.get(fileName) || 0;
            // Add to `rootFiles` when discovered for the first time.
            if (fileVersion === 0)
                memoryCache_1.rootFileNames.push(fileName);
            // Avoid incrementing cache when nothing has changed.
            if (memoryCache_1.fileContents.get(fileName) === contents)
                return;
            memoryCache_1.fileVersions.set(fileName, fileVersion + 1);
            memoryCache_1.fileContents.set(fileName, contents);
        };
        getOutput = function (code, fileName, lineOffset) {
            if (lineOffset === void 0) { lineOffset = 0; }
            updateMemoryCache_1(code, fileName);
            var output = service_1.getEmitOutput(fileName);
            // Get the relevant diagnostics - this is 3x faster than `getPreEmitDiagnostics`.
            var diagnostics = service_1.getSemanticDiagnostics(fileName)
                .concat(service_1.getSyntacticDiagnostics(fileName));
            var diagnosticList = filterDiagnostics(diagnostics, ignoreDiagnostics);
            if (diagnosticList.length)
                throw createTSError(diagnosticList);
            if (output.emitSkipped) {
                throw new TypeError(path_1.relative(cwd, fileName) + ": Emit skipped");
            }
            // Throw an error when requiring `.d.ts` files.
            if (output.outputFiles.length === 0) {
                throw new TypeError('Unable to require `.d.ts` file.\n' +
                    'This is usually the result of a faulty configuration or import. ' +
                    'Make sure there is a `.js`, `.json` or another executable extension and ' +
                    'loader (attached before `ts-node`) available alongside ' +
                    ("`" + path_1.basename(fileName) + "`."));
            }
            return [output.outputFiles[1].text, output.outputFiles[0].text];
        };
        getTypeInfo = function (code, fileName, position) {
            updateMemoryCache_1(code, fileName);
            var info = service_1.getQuickInfoAtPosition(fileName, position);
            var name = ts.displayPartsToString(info ? info.displayParts : []);
            var comment = ts.displayPartsToString(info ? info.documentation : []);
            return { name: name, comment: comment };
        };
    }
    // Create a simple TypeScript compiler proxy.
    function compile(code, fileName, lineOffset) {
        var _a = getOutput(code, fileName, lineOffset), value = _a[0], sourceMap = _a[1];
        var output = updateOutput(value, fileName, sourceMap, getExtension);
        outputCache.set(fileName, output);
        return output;
    }
    var register = { cwd: cwd, compile: compile, getTypeInfo: getTypeInfo, extensions: extensions, ts: ts };
    // Register the extensions.
    extensions.forEach(function (extension) {
        registerExtension(extension, ignore, register, originalJsHandler);
    });
    return register;
}
exports.register = register;
/**
 * Check if the filename should be ignored.
 */
function shouldIgnore(filename, ignore) {
    var relname = normalizeSlashes(filename);
    return ignore.some(function (x) { return x.test(relname); });
}
/**
 * Register the extension for node.
 */
function registerExtension(ext, ignore, register, originalHandler) {
    var old = require.extensions[ext] || originalHandler;
    require.extensions[ext] = function (m, filename) {
        if (shouldIgnore(filename, ignore)) {
            return old(m, filename);
        }
        var _compile = m._compile;
        m._compile = function (code, fileName) {
            debug('module._compile', fileName);
            return _compile.call(this, register.compile(code, fileName), fileName);
        };
        return old(m, filename);
    };
}
/**
 * Do post-processing on config options to support `ts-node`.
 */
function fixConfig(ts, config) {
    // Delete options that *should not* be passed through.
    delete config.options.out;
    delete config.options.outFile;
    delete config.options.composite;
    delete config.options.declarationDir;
    delete config.options.declarationMap;
    delete config.options.emitDeclarationOnly;
    // Target ES5 output by default (instead of ES3).
    if (config.options.target === undefined) {
        config.options.target = ts.ScriptTarget.ES5;
    }
    // Target CommonJS modules by default (instead of magically switching to ES6 when the target is ES6).
    if (config.options.module === undefined) {
        config.options.module = ts.ModuleKind.CommonJS;
    }
    return config;
}
/**
 * Load TypeScript configuration.
 */
function readConfig(cwd, ts, fileExists, readFile, options) {
    var config = { compilerOptions: {} };
    var basePath = normalizeSlashes(cwd);
    var configFileName = undefined;
    // Read project configuration when available.
    if (!options.skipProject) {
        configFileName = options.project
            ? normalizeSlashes(path_1.resolve(cwd, options.project))
            : ts.findConfigFile(normalizeSlashes(cwd), fileExists);
        if (configFileName) {
            var result = ts.readConfigFile(configFileName, readFile);
            // Return diagnostics.
            if (result.error) {
                return { errors: [result.error], fileNames: [], options: {} };
            }
            config = result.config;
            basePath = normalizeSlashes(path_1.dirname(configFileName));
        }
    }
    // Remove resolution of "files".
    if (!options.files) {
        config.files = [];
        config.include = [];
    }
    // Override default configuration options `ts-node` requires.
    config.compilerOptions = Object.assign({}, config.compilerOptions, options.compilerOptions, TS_NODE_COMPILER_OPTIONS);
    return fixConfig(ts, ts.parseJsonConfigFileContent(config, ts.sys, basePath, undefined, configFileName));
}
/**
 * Update the output remapping the source map.
 */
function updateOutput(outputText, fileName, sourceMap, getExtension) {
    var base64Map = Buffer.from(updateSourceMap(sourceMap, fileName), 'utf8').toString('base64');
    var sourceMapContent = "data:application/json;charset=utf-8;base64," + base64Map;
    var sourceMapLength = (path_1.basename(fileName) + ".map").length + (getExtension(fileName).length - path_1.extname(fileName).length);
    return outputText.slice(0, -sourceMapLength) + sourceMapContent;
}
/**
 * Update the source map contents for improved output.
 */
function updateSourceMap(sourceMapText, fileName) {
    var sourceMap = JSON.parse(sourceMapText);
    sourceMap.file = fileName;
    sourceMap.sources = [fileName];
    delete sourceMap.sourceRoot;
    return JSON.stringify(sourceMap);
}
/**
 * Filter diagnostics.
 */
function filterDiagnostics(diagnostics, ignore) {
    return diagnostics.filter(function (x) { return ignore.indexOf(x.code) === -1; });
}
//# sourceMappingURL=index.js.map