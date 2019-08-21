"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var ERROR_MSGS = require("../constants/error_msgs");
function isStackOverflowExeption(error) {
    return (error instanceof RangeError ||
        error.message === ERROR_MSGS.STACK_OVERFLOW);
}
exports.isStackOverflowExeption = isStackOverflowExeption;
