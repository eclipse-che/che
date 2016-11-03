import che = _che;

declare namespace _che {

  export interface IRootScopeService extends ng.IRootScopeService {
    hideLoader: boolean;
    showIDE: boolean;
    wantTokeepLoader: boolean;
  }

  export namespace route {

    export interface IRouteParamsService extends ng.route.IRouteParamsService {
      action: string;
      ideParams: string | string[];
      namespace: string;
      showLogs: string;
      workspaceName: string;
    }

  }

}
