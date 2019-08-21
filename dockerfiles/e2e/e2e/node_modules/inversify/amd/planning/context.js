define(["require", "exports", "../utils/id"], function (require, exports, id_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var Context = (function () {
        function Context(container) {
            this.id = id_1.id();
            this.container = container;
        }
        Context.prototype.addPlan = function (plan) {
            this.plan = plan;
        };
        Context.prototype.setCurrentRequest = function (currentRequest) {
            this.currentRequest = currentRequest;
        };
        return Context;
    }());
    exports.Context = Context;
});
