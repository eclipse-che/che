define(["require", "exports", "../constants/error_msgs"], function (require, exports, ERROR_MSGS) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function isStackOverflowExeption(error) {
        return (error instanceof RangeError ||
            error.message === ERROR_MSGS.STACK_OVERFLOW);
    }
    exports.isStackOverflowExeption = isStackOverflowExeption;
});
