import { id } from "../utils/id";
var Context = (function () {
    function Context(container) {
        this.id = id();
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
export { Context };
