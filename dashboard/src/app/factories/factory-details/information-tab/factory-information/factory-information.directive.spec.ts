/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CheHttpBackend} from '../../../../../components/api/test/che-http-backend';
import {CheAPIBuilder} from '../../../../../components/api/builder/che-api-builder.factory';
import {FactoryInformationController} from './factory-information.controller';

interface ITestScope extends ng.IScope {
  model: any;
}

/**
 * Test of the FactoryInformation directive
 * @author Oleksii Kurinnyi
 */
describe('FactoryInformation >', () => {

  let $scope: ITestScope;

  let $compile: ng.ICompileService;

  let compiledDirective;

  let cheAPIBuilder: CheAPIBuilder;

  let factory: che.IFactory;

  let controller: FactoryInformationController;

  let cheHttpBackend: CheHttpBackend;

  /**
   * Backend for handling http operations
   */
  let $httpBackend: ng.IHttpBackendService;

  let $timeout: ng.ITimeoutService;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$rootScope_: ng.IRootScopeService,
                     _$timeout_: ng.ITimeoutService,
                     _cheAPIBuilder_: CheAPIBuilder,
                     _cheHttpBackend_: CheHttpBackend) => {
    $scope = _$rootScope_.$new() as ITestScope;
    $timeout = _$timeout_;
    $compile = _$compile_;

    cheAPIBuilder = _cheAPIBuilder_;
    cheHttpBackend = _cheHttpBackend_;

    $httpBackend = _cheHttpBackend_.getHttpBackend();
    // avoid tracking requests from branding controller
    $httpBackend.whenGET(/.*/).respond(200, '');
    $httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  /**
   * Build a factory.
   * Set up default user, factory.
   */
  const commandName = 'build',
    commandLine = 'mvn clean install';
  beforeEach(() => {
    // setup default user
    const userId = 'userId',
      user = cheAPIBuilder.getUserBuilder().withId(userId).build();
    cheHttpBackend.setDefaultUser(user);
    cheHttpBackend.usersBackendSetup();

    // setup factory
    const workspaceId = 'workspaceId',
      workspaceName = 'workspaceName',
      defaultEnvironmentName = 'default',
      environments: {[envName: string]: che.IWorkspaceEnvironment} = {
        [defaultEnvironmentName]: {
          machines: {
            machine1: {
              attributes: {
                memoryLimitBytes: 2147483648
              }
            }
          },
          recipe: {
            content: 'FROM test/image',
            type: 'dockerfile'
          }
        }
      };
    const workspace = cheAPIBuilder.getWorkspaceBuilder()
      .withId(workspaceId)
      .withName(workspaceName)
      .withDefaultEnvironment(defaultEnvironmentName)
      .withEnvironments(environments)
      .build();

    const factoryId = 'factoryId',
      factoryName = 'factoryName';

    factory = cheAPIBuilder.getFactoryBuilder()
      .withId(factoryId)
      .withName(factoryName)
      .withWorkspace(workspace.config)
      .withCommand(commandName, commandLine)
      .build();

    cheHttpBackend.addUserFactory(factory);
    cheHttpBackend.factoriesBackendSetup();

    cheHttpBackend.setup();

    $scope.model = {
      factory: factory
    };
  });

  /**
   * Compile the directive, grab the controller.
   */
  beforeEach(() => {
    compiledDirective = getCompiledElement();
    controller = compiledDirective.controller('cdvyFactoryInformation');
  });

  /**
   * Returns compiled directive.
   * @returns {angular.IAugmentedJQuery}
   */
  function getCompiledElement(): ng.IAugmentedJQuery {
    const element = $compile(angular.element(`<cdvy-factory-information cdvy-factory="model.factory"></cdvy-factory-information>`))($scope);
    $scope.$digest();
    return element;
  }

  describe('factory name >', () => {
    let factoryNameInput;
    let modelCtrl;

    beforeEach(() => {
      factoryNameInput = compiledDirective.find('input[name="factoryName"]');
      modelCtrl = factoryNameInput.controller('ngModel');

      $timeout.flush();
    });

    it('should have correct value', () => {
      expect(modelCtrl.$viewValue).toEqual(factory.name);
    });

    describe('updating >', () => {

      it(`should call 'updateFactoryName' method`, () => {
        spyOn(controller, 'updateFactoryName');

        const newFactoryName = 'newFactoryName';
        modelCtrl.$setViewValue(newFactoryName);

        expect(controller.updateFactoryName).toHaveBeenCalledWith(newFactoryName);
      });

      it(`should call 'updateFactory' method`, () => {
        spyOn(controller, 'updateFactory');

        const newFactoryName = 'newFactoryName';
        modelCtrl.$setViewValue(newFactoryName);

        expect(controller.updateFactory).toHaveBeenCalled();
      });

    });

  });

  describe('workspace name >', () => {
    let workspaceNameInput;
    let modelCtrl;

    beforeEach(() => {
      workspaceNameInput = compiledDirective.find('input[name="workspaceName"]');
      modelCtrl = workspaceNameInput.controller('ngModel');

      $timeout.flush();
    });

    it('should have correct value', () => {
      expect(modelCtrl.$viewValue).toEqual(factory.workspace.name);
    });

    describe('updating >', () => {

      it(`should call 'updateWorkspaceName' method`, () => {
        spyOn(controller, 'updateWorkspaceName');

        const newWorkspaceName = 'newWorkspaceName';
        modelCtrl.$setViewValue(newWorkspaceName);

        expect(controller.updateWorkspaceName).toHaveBeenCalledWith(newWorkspaceName);
      });

      it(`should call 'updateFactory' method`, () => {
        spyOn(controller, 'updateFactory');

        const newWorkspaceName = 'newWorkspaceName';
        modelCtrl.$setViewValue(newWorkspaceName);

        expect(controller.updateFactory).toHaveBeenCalled();
      });

    });

  });

  describe('workspace RAM >', () => {
    let workspaceRamInput;
    let modelCtrl;

    beforeEach(() => {
      workspaceRamInput = compiledDirective.find('.che-ram-allocation-slider input[type="number"]');
      modelCtrl = workspaceRamInput.controller('ngModel');

      $timeout.flush();
    });

    it('should have correct value', () => {
      expect(modelCtrl.$viewValue).toEqual('2');
    });

    describe('updating >', () => {

      it(`should call 'updateFactory' method`, () => {
        spyOn(controller, 'updateFactory');

        const newWorkspaceRam = 3;
        modelCtrl.$setViewValue(newWorkspaceRam);

        $timeout.flush();

        expect(controller.updateFactory).toHaveBeenCalled();
      });

    });

  });

  describe('configure commands >', () => {
    let commandsWidget;

    beforeEach(() => {
      commandsWidget = compiledDirective.find('cdvy-factory-command');

      $timeout.flush();
    });

    it('should contain predefined command', () => {
      const commandRow = commandsWidget.find('.che-list-item'),
        cmdName = commandRow.find('.factory-commands-row-command-name').text().trim(),
        cmdLine = commandRow.find('.factory-commands-row-command').text().trim();

      expect(cmdName).toEqual(commandName);
      expect(cmdLine).toEqual(commandLine);
    });

    describe('adding a new command >', () => {
      let commandNameInput,
        commandNameCtrl,
        commandLineInput,
        commandLineCtrl,
        commandAddButton;

      beforeEach(() => {
        commandNameInput = angular.element(commandsWidget.find('.factory-commands-input .che-input').get(0)).find('input');
        commandNameCtrl = commandNameInput.controller('ngModel');
        commandLineInput = angular.element(commandsWidget.find('.factory-commands-input .che-input').get(1)).find('input');
        commandLineCtrl = commandLineInput.controller('ngModel');
        commandAddButton = commandsWidget.find('button');
      });

      it(`should add a new command to the list`, () => {
        const newCommandName = 'new command name',
          newCommandLine = 'new command line';

        commandNameCtrl.$setViewValue(newCommandName);
        commandLineCtrl.$setViewValue(newCommandLine);
        commandAddButton.click();

        $timeout.flush();

        const commandRows = compiledDirective.find('cdvy-factory-command').find('.che-list-content');
        expect(commandRows.html()).toContain(newCommandName);
        expect(commandRows.html()).toContain(newCommandLine);
      });

      it(`should call 'updateFactory' method`, () => {
        spyOn(controller, 'updateFactory');

        const newCommandName = 'new command name',
          newCommandLine = 'new command line';

        commandNameCtrl.$setViewValue(newCommandName);
        commandLineCtrl.$setViewValue(newCommandLine);
        commandAddButton.click();

        expect(controller.updateFactory).toHaveBeenCalled();
      });

    });

  });

});

