/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
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
      tabName: string;
    }

    export interface IRouteProvider extends ng.route.IRouteProvider {
      accessWhen: (path: string, route: any) => ng.IPromise<any>;
      accessOtherWise: (route: any) => ng.IPromise<any>;
    }
  }

  export namespace widget {

    export interface ICheListHelper {
      areAllItemsSelected: boolean;
      isNoItemSelected: boolean;
      itemsSelectionStatus: any;
      visibleItemsNumber: number;
      selectAllItems(): void;
      deselectAllItems(): void;
      changeBulkSelection(): void;
      updateBulkSelectionStatus(): void;
      getSelectedItems(): any[];
      getVisibleItems(): any[];
      setList(itemsList: any[], key: string, isSelectable?: (item: any) => boolean): void;
      applyFilter(name: string, ...filterProps: any[]);
      clearFilters(): void;
    }

    export interface ICheListHelperFactory {
      getHelper(id: string): ICheListHelper;
      removeHelper(id: string): void;
    }

  }

  export interface IRegisterService {
    app: ng.IModule;
    directive(name: string, constructorFn: Function);
    filter(name: string, constructorFn: Function): IRegisterService;
    controller(name: string, constructorFn: Function): IRegisterService;
    service(name: string, constructorFn: Function): IRegisterService;
    provider(name: string, constructorFn: ng.IServiceProvider): IRegisterService;
    factory(name: string, constructorFn: Function): IRegisterService;
  }

  export interface IWorkspaceCommand {
    name: string;
    type: string;
    commandLine: string;
    attributes?: {
      previewUrl?: string;
      [propName: string]: string;
    };
  }

  export interface IStack {
    id?: string;
    name: string;
    description?: string;
    tags?: Array<string>;
    creator?: string;
    scope?: string;
    components?: Array<any>;
    links?: Array<any>;
    source?: any;
    workspaceConfig: IWorkspaceConfig;
  }

  export interface IStackLink {
    href: string;
    method: string;
    rel: string;
    parameters: any[];
  }

  export interface IWorkspace {
    id?: string;
    name: string;
    projects?: any;
    links?: Array<any>;
    temporary?: boolean;
    status?: string;
    namespace?: string;
    attributes?: {
      updated?: number;
      created?: number;
      [propName: string]: string | number;
    };
    config: IWorkspaceConfig;
    runtime?: IWorkspaceRuntime;
    isLocked?: boolean;
    usedResources: string;
  }

  export interface IWorkspaceConfig {
    name?: string;
    defaultEnv?: string;
    environments: {
      [envName: string]: IWorkspaceEnvironment
    };
    projects?: Array <any>;
    commands?: Array <any>;
  }

  export interface IWorkspaceEnvironment {
    machines: {
      [machineName: string]: IEnvironmentMachine
    };
    recipe: IRecipe;
  }

  export interface IRecipe {
    content?: string;
    location?: string;
    contentType?: string;
    type: string;
  }

  export interface IEnvironmentMachine {
    agents?: string[];
    attributes?: {
      memoryLimitBytes?: string|number;
      [attrName: string]: string|number;
    };
    servers?: {
      [serverRef: string]: IEnvironmentMachineServer
    };
  }

  export interface IEnvironmentMachineServer {
    port: string|number;
    protocol: string;
    properties?: {
      [propName: string]: string
    };
  }

  export interface IWorkspaceRuntime {
    activeEnv: string;
    devMachine: IWorkspaceRuntimeMachine;
    links: any[];
    machines: IWorkspaceRuntimeMachine[];
    rootFolder: string;
  }

  export interface IWorkspaceRuntimeMachine {
    config: any;
    envName: string;
    id: string;
    links: any[];
    owner: string;
    runtime: {
      envVariables: { [envVarName: string]: string };
      properties: { [propName: string]: string };
      servers: { [serverName: string]: IWorkspaceRuntimeMachineServer };
    };
    status: string;
    workspaceId: string;
  }

  export interface IWorkspaceRuntimeMachineServer {
    address: string;
    properties: { [propName: string]: string; };
    protocol: string;
    port: string;
    ref: string;
    url: string;
  }

  export interface IProjectSource {
    location: string;
    parameters?: {
      [paramName: string]: string
    };
    type?: string;
  }

  export interface IProjectTemplate {
    name: string;
    displayName?: string;
    description: string;
    source?: IProjectSource;
    path?: string;
    commands?: Array<any>;
    projectType?: string;
    type?: string;
    tags?: Array<string>;
    attributes?: any;
    options?: Array<any>;
    workspaceId?: string;
    workspaceName?: string;
    projects?: IProject[];
  }

  export interface IProject {
    name: string;
    source: IProjectSource;
    workspaceId?: string;
    workspaceName?: string;
  }

  export interface IWorkspaceProjects {
    [workspaceId: string]: Array<IProject>;
  }

  export interface IImportProject {
    source: IProjectSource;
    project?: IProjectTemplate;
    projects?: IProjectTemplate[];
  }

  export interface IEditorOptions {
    mode: string;
    lineNumbers: boolean;
    lineWrapping: boolean;
    matchBrackets: boolean;
  }

  export interface IValidation {
    isValid: boolean;
    errors: Array<string>;
  }

  export interface IProfileAttributes {
      firstName?: string;
      lastName?: string;
      [propName: string]: string | number;
  }

  export interface IProfile extends ng.resource.IResourceClass<any> {
    attributes?: IProfileAttributes;
    email: string;
    links?: Array<any>;
    userId: string;
    $promise?: any;
  }

  export interface INamespace {
    id: string;
    label: string;
    location: string;
  }

  export interface IUser {
    attributes: {
      firstName?: string;
      lastName?: string;
      [propName: string]: string | number;
    };
    id: string;
    name: string;
    email: string;
    aliases: Array<string>;
  }

  export interface IFactory {
    id: string;
    name?: string;
    v: string;
    workspace: IWorkspaceConfig;
    creator: any;
    ide?: any;
    button?: any;
    policies?: any;
  }

  export interface IRegistry {
    url: string;
    username: string;
    password: string;
  }

  interface IRequestData {
    userId?: string;
    maxItems?: string;
    skipCount?: string;
    [param: string]: string;
  }

  interface IPageInfo {
    countPages: number;
    currentPageNumber: number;
  }
}
