#!/usr/bin/env node
"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var _a;
var path_1 = require("path");
var repl_1 = require("repl");
var util_1 = require("util");
var Module = require("module");
var arg = require("arg");
var diff_1 = require("diff");
var vm_1 = require("vm");
var fs_1 = require("fs");
var index_1 = require("./index");
var args = arg({
    // Node.js-like options.
    '--eval': String,
    '--print': Boolean,
    '--require': [String],
    // CLI options.
    '--files': Boolean,
    '--help': Boolean,
    '--version': arg.COUNT,
    // Project options.
    '--compiler': String,
    '--compiler-options': index_1.parse,
    '--project': String,
    '--ignore-diagnostics': [String],
    '--ignore': [String],
    '--transpile-only': Boolean,
    '--type-check': Boolean,
    '--pretty': Boolean,
    '--skip-project': Boolean,
    '--skip-ignore': Boolean,
    // Aliases.
    '-e': '--eval',
    '-p': '--print',
    '-r': '--require',
    '-h': '--help',
    '-v': '--version',
    '-T': '--transpile-only',
    '-I': '--ignore',
    '-P': '--project',
    '-C': '--compiler',
    '-D': '--ignore-diagnostics',
    '-O': '--compiler-options'
}, {
    stopAtPositional: true
});
var _b = args["--help"], help = _b === void 0 ? false : _b, _c = args["--version"], version = _c === void 0 ? 0 : _c, _d = args["--files"], files = _d === void 0 ? index_1.DEFAULTS.files : _d, _e = args["--compiler"], compiler = _e === void 0 ? index_1.DEFAULTS.compiler : _e, _f = args["--compiler-options"], compilerOptions = _f === void 0 ? index_1.DEFAULTS.compilerOptions : _f, _g = args["--project"], project = _g === void 0 ? index_1.DEFAULTS.project : _g, _h = args["--ignore-diagnostics"], ignoreDiagnostics = _h === void 0 ? index_1.DEFAULTS.ignoreDiagnostics : _h, _j = args["--ignore"], ignore = _j === void 0 ? index_1.DEFAULTS.ignore : _j, _k = args["--transpile-only"], transpileOnly = _k === void 0 ? index_1.DEFAULTS.transpileOnly : _k, _l = args["--type-check"], typeCheck = _l === void 0 ? index_1.DEFAULTS.typeCheck : _l, _m = args["--pretty"], pretty = _m === void 0 ? index_1.DEFAULTS.pretty : _m, _o = args["--skip-project"], skipProject = _o === void 0 ? index_1.DEFAULTS.skipProject : _o, _p = args["--skip-ignore"], skipIgnore = _p === void 0 ? index_1.DEFAULTS.skipIgnore : _p;
if (help) {
    console.log("\nUsage: ts-node [options] [ -e script | script.ts ] [arguments]\n\nOptions:\n\n  -e, --eval [code]              Evaluate code\n  -p, --print                    Print result of `--eval`\n  -r, --require [path]           Require a node module before execution\n\n  -h, --help                     Print CLI usage\n  -v, --version                  Print module version information\n\n  -T, --transpile-only           Use TypeScript's faster `transpileModule`\n  -I, --ignore [pattern]         Override the path patterns to skip compilation\n  -P, --project [path]           Path to TypeScript JSON project file\n  -C, --compiler [name]          Specify a custom TypeScript compiler\n  -D, --ignore-diagnostics [code] Ignore TypeScript warnings by diagnostic code\n  -O, --compiler-options [opts]   JSON object to merge with compiler options\n\n  --files                        Load files from `tsconfig.json` on startup\n  --pretty                       Use pretty diagnostic formatter\n  --skip-project                 Skip reading `tsconfig.json`\n  --skip-ignore                  Skip `--ignore` checks\n");
    process.exit(0);
}
// Output project information.
if (version === 1) {
    console.log("v" + index_1.VERSION);
    process.exit(0);
}
var cwd = process.cwd();
var code = args['--eval'];
var isPrinted = args['--print'] !== undefined;
// Register the TypeScript compiler instance.
var service = index_1.register({
    files: files,
    pretty: pretty,
    typeCheck: typeCheck,
    transpileOnly: transpileOnly,
    ignore: ignore,
    project: project,
    skipIgnore: skipIgnore,
    skipProject: skipProject,
    compiler: compiler,
    ignoreDiagnostics: ignoreDiagnostics,
    compilerOptions: compilerOptions,
    readFile: code ? readFileEval : undefined,
    fileExists: code ? fileExistsEval : undefined
});
// Output project information.
if (version >= 2) {
    console.log("ts-node v" + index_1.VERSION);
    console.log("node " + process.version);
    console.log("compiler v" + service.ts.version);
    process.exit(0);
}
// Require specified modules before start-up.
if (args['--require'])
    Module._preloadModules(args['--require']);
/**
 * Eval helpers.
 */
var EVAL_FILENAME = "[eval].ts";
var EVAL_PATH = path_1.join(cwd, EVAL_FILENAME);
var EVAL_INSTANCE = { input: '', output: '', version: 0, lines: 0 };
// Prepend `ts-node` arguments to CLI for child processes.
(_a = process.execArgv).unshift.apply(_a, [__filename].concat(process.argv.slice(2, process.argv.length - args._.length)));
process.argv = [process.argv[1]].concat(args._.length ? path_1.resolve(cwd, args._[0]) : []).concat(args._.slice(1));
// Execute the main contents (either eval, script or piped).
if (code) {
    evalAndExit(code, isPrinted);
}
else {
    if (args._.length) {
        Module.runMain();
    }
    else {
        // Piping of execution _only_ occurs when no other script is specified.
        if (process.stdin.isTTY) {
            startRepl();
        }
        else {
            var code_1 = '';
            process.stdin.on('data', function (chunk) { return code_1 += chunk; });
            process.stdin.on('end', function () { return evalAndExit(code_1, isPrinted); });
        }
    }
}
/**
 * Evaluate a script.
 */
function evalAndExit(code, isPrinted) {
    var module = new Module(EVAL_FILENAME);
    module.filename = EVAL_FILENAME;
    module.paths = Module._nodeModulePaths(cwd);
    global.__filename = EVAL_FILENAME;
    global.__dirname = cwd;
    global.exports = module.exports;
    global.module = module;
    global.require = module.require.bind(module);
    var result;
    try {
        result = _eval(code);
    }
    catch (error) {
        if (error instanceof index_1.TSError) {
            console.error(error.diagnosticText);
            process.exit(1);
        }
        throw error;
    }
    if (isPrinted) {
        console.log(typeof result === 'string' ? result : util_1.inspect(result));
    }
}
/**
 * Evaluate the code snippet.
 */
function _eval(input) {
    var lines = EVAL_INSTANCE.lines;
    var isCompletion = !/\n$/.test(input);
    var undo = appendEval(input);
    var output;
    try {
        output = service.compile(EVAL_INSTANCE.input, EVAL_PATH, -lines);
    }
    catch (err) {
        undo();
        throw err;
    }
    // Use `diff` to check for new JavaScript to execute.
    var changes = diff_1.diffLines(EVAL_INSTANCE.output, output);
    if (isCompletion) {
        undo();
    }
    else {
        EVAL_INSTANCE.output = output;
    }
    return changes.reduce(function (result, change) {
        return change.added ? exec(change.value, EVAL_FILENAME) : result;
    }, undefined);
}
/**
 * Execute some code.
 */
function exec(code, filename) {
    var script = new vm_1.Script(code, { filename: filename });
    return script.runInThisContext();
}
/**
 * Start a CLI REPL.
 */
function startRepl() {
    var repl = repl_1.start({
        prompt: '> ',
        input: process.stdin,
        output: process.stdout,
        terminal: process.stdout.isTTY,
        eval: replEval,
        useGlobal: true
    });
    // Bookmark the point where we should reset the REPL state.
    var resetEval = appendEval('');
    function reset() {
        resetEval();
        // Hard fix for TypeScript forcing `Object.defineProperty(exports, ...)`.
        exec('exports = module.exports', EVAL_FILENAME);
    }
    reset();
    repl.on('reset', reset);
    repl.defineCommand('type', {
        help: 'Check the type of a TypeScript identifier',
        action: function (identifier) {
            if (!identifier) {
                repl.displayPrompt();
                return;
            }
            var undo = appendEval(identifier);
            var _a = service.getTypeInfo(EVAL_INSTANCE.input, EVAL_PATH, EVAL_INSTANCE.input.length), name = _a.name, comment = _a.comment;
            undo();
            repl.outputStream.write(name + "\n" + (comment ? comment + "\n" : ''));
            repl.displayPrompt();
        }
    });
}
/**
 * Eval code from the REPL.
 */
function replEval(code, _context, _filename, callback) {
    var err = null;
    var result;
    // TODO: Figure out how to handle completion here.
    if (code === '.scope') {
        callback(err);
        return;
    }
    try {
        result = _eval(code);
    }
    catch (error) {
        if (error instanceof index_1.TSError) {
            // Support recoverable compilations using >= node 6.
            if (repl_1.Recoverable && isRecoverable(error)) {
                err = new repl_1.Recoverable(error);
            }
            else {
                console.error(error.diagnosticText);
            }
        }
        else {
            err = error;
        }
    }
    callback(err, result);
}
/**
 * Append to the eval instance and return an undo function.
 */
function appendEval(input) {
    var undoInput = EVAL_INSTANCE.input;
    var undoVersion = EVAL_INSTANCE.version;
    var undoOutput = EVAL_INSTANCE.output;
    var undoLines = EVAL_INSTANCE.lines;
    // Handle ASI issues with TypeScript re-evaluation.
    if (undoInput.charAt(undoInput.length - 1) === '\n' && /^\s*[\[\(\`]/.test(input) && !/;\s*$/.test(undoInput)) {
        EVAL_INSTANCE.input = EVAL_INSTANCE.input.slice(0, -1) + ";\n";
    }
    EVAL_INSTANCE.input += input;
    EVAL_INSTANCE.lines += lineCount(input);
    EVAL_INSTANCE.version++;
    return function () {
        EVAL_INSTANCE.input = undoInput;
        EVAL_INSTANCE.output = undoOutput;
        EVAL_INSTANCE.version = undoVersion;
        EVAL_INSTANCE.lines = undoLines;
    };
}
/**
 * Count the number of lines.
 */
function lineCount(value) {
    var count = 0;
    for (var _i = 0, value_1 = value; _i < value_1.length; _i++) {
        var char = value_1[_i];
        if (char === '\n') {
            count++;
        }
    }
    return count;
}
/**
 * Get the file text, checking for eval first.
 */
function readFileEval(path) {
    if (path === EVAL_PATH)
        return EVAL_INSTANCE.input;
    try {
        return fs_1.readFileSync(path, 'utf8');
    }
    catch (err) { /* Ignore. */ }
}
/**
 * Get whether the file exists.
 */
function fileExistsEval(path) {
    if (path === EVAL_PATH)
        return true;
    try {
        var stats = fs_1.statSync(path);
        return stats.isFile() || stats.isFIFO();
    }
    catch (err) {
        return false;
    }
}
var RECOVERY_CODES = new Set([
    1003,
    1005,
    1109,
    1126,
    1160,
    1161,
    2355 // "A function whose declared type is neither 'void' nor 'any' must return a value."
]);
/**
 * Check if a function can recover gracefully.
 */
function isRecoverable(error) {
    return error.diagnosticCodes.every(function (code) { return RECOVERY_CODES.has(code); });
}
//# sourceMappingURL=bin.js.map