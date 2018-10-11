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

import {ICheEditModeOverlayConfig} from './che-edit-mode-overlay.directive';
import {CheHttpBackend} from '../../api/test/che-http-backend';

interface ITestScope extends ng.IScope {
  config: ICheEditModeOverlayConfig;
}

describe(`cheEditModeOverlay >`, () => {

  let $scope: ITestScope;

  let $compile: ng.ICompileService;

  let $timeout: ng.ITimeoutService;

  let compiledDirective;

  /**
   * Setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((_$compile_: ng.ICompileService,
                     _$timeout_: ng.ITimeoutService,
                     _$rootScope_: ng.IRootScopeService,
                     _cheHttpBackend_: CheHttpBackend) => {
    $scope = _$rootScope_.$new() as ITestScope;
    $compile = _$compile_;
    $timeout = _$timeout_;

    const $httpBackend = _cheHttpBackend_.getHttpBackend();
    // avoid tracking requests from branding controller;
    $httpBackend.whenGET(/.*/).respond(200, '');
    $httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  beforeEach(() => {
    $scope.config = {
      visible: true,
      message: {
        content: 'Test message',
        visible: false
      }
    };
  });

  afterEach(() => {
    $timeout.verifyNoPendingTasks();
  });

  function compileDirective(): void {
    const template = `<div><che-edit-mode-overlay config="config"></che-edit-mode-overlay></div>`;

    compiledDirective = $compile(angular.element(template))($scope);
    $timeout.flush();
    $scope.$digest();
  }

  it(`should be compiled >`, () => {
    compileDirective();
    expect(compiledDirective.find('.che-edit-mode-overlay').length).toEqual(1);
  });

  describe(`'visible' >`, () => {

    function getOverlayEl(): ng.IAugmentedJQuery {
      return compiledDirective.find('.che-edit-mode-overlay');
    }

    describe(`initially visible >`, () => {

      beforeEach(() => {
        $scope.config.visible = true;
        compileDirective();
      });

      it(`should be in DOM >`, () => {
        expect(getOverlayEl().length).toEqual(1);
      });

      it(`should be hidden when it's set to 'false' >`, () => {
        $scope.config.visible = false;
        $scope.$digest();

        expect(getOverlayEl().length).toEqual(0);
      });

    });

    describe(`initially hidden >`, () => {

      beforeEach(() => {
        $scope.config.visible = false;
        compileDirective();
      });

      it(`shouldn't be in DOM >`, () => {
        expect(getOverlayEl().length).toEqual(0);
      });

      it(`should be shown when it's set to 'true' >`, () => {
        $scope.config.visible = true;
        $scope.$digest();

        expect(getOverlayEl().length).toEqual(1);
      });
    });

  });

  describe(`'disabled' >`, () => {

    let saveButtonEl, applyButtonEl, cancelButtonEl;

    beforeEach(() => {
      $scope.config.saveButton = {
        action: jasmine.createSpy('saveButton.action')
      };
      $scope.config.applyButton = {
        action: jasmine.createSpy('applyButton.action')
      };
      $scope.config.cancelButton = {
        action: jasmine.createSpy('cancelButton.action')
      };
    });

    describe(`equals to 'false' >`, () => {

      beforeEach(() => {
        $scope.config.disabled = false;
        compileDirective();

        saveButtonEl = compiledDirective.find('.save-button button');
        applyButtonEl = compiledDirective.find('.apply-button button');
        cancelButtonEl = compiledDirective.find('.cancel-button button');
      });

      it(`should call a callback when 'saveButton' is clicked >`, () => {
        saveButtonEl.click();
        expect($scope.config.saveButton.action).toHaveBeenCalled();
      });

      it(`should call a callback when 'applyButton' is clicked >`, () => {
        applyButtonEl.click();
        expect($scope.config.applyButton.action).toHaveBeenCalled();
      });

      it(`should call a callback when 'cancelButton' is clicked >`, () => {
        cancelButtonEl.click();
        expect($scope.config.cancelButton.action).toHaveBeenCalled();
      });

    });

    describe(`equals to 'true' >`, () => {

      beforeEach(() => {
        $scope.config.disabled = true;
        compileDirective();

        saveButtonEl = compiledDirective.find('.save-button button');
        applyButtonEl = compiledDirective.find('.apply-button button');
        cancelButtonEl = compiledDirective.find('.cancel-button button');
      });

      it(`shouldn't call a callback when 'saveButton' is clicked >`, () => {
        saveButtonEl.click();
        expect($scope.config.saveButton.action).not.toHaveBeenCalled();
      });

      it(`shouldn't call a callback when 'applyButton' is clicked >`, () => {
        applyButtonEl.click();
        expect($scope.config.applyButton.action).not.toHaveBeenCalled();
      });

    });

  });

  describe(`'message' >`, () => {

    function getMessageElement(): ng.IAugmentedJQuery {
      return compiledDirective.find('.che-edit-mode-overlay-message span');
    }

    describe(`content isn't set up initially >`, () => {

      beforeEach(() => {
        $scope.config.message.content = '';
        $scope.config.message.visible = true;

        compileDirective();
      });

      it(`should not be shown >`, () => {
        expect(getMessageElement().length).toEqual(0);
      });

      describe(`and set later >`, () => {

        beforeEach(() => {
          $scope.config.message.content = 'Information message';
          $scope.$digest();
        });

        it(`should be shown >`, () => {
          expect(getMessageElement().length).toEqual(1);
        });

        it(`should have correct content`, () => {
          expect(getMessageElement().text().trim()).toEqual($scope.config.message.content);
        });

      });

    });

    describe(`content is set up and visible initially >`, () => {

      beforeEach(() => {
        $scope.config.message.content = 'Information message';
        $scope.config.message.visible = true;

        compileDirective();
      });

      it(`should be shown >`, () => {
        expect(getMessageElement().length).toEqual(1);
      });

      it(`should have correct content >`, () => {
        expect(getMessageElement().text().trim()).toEqual($scope.config.message.content);
      });

      it(`should be hide when visibility is changed >`, () => {
        $scope.config.message.visible = false;
        $scope.$digest();

        expect(getMessageElement().length).toEqual(0);
      });

      it(`should be hide when content is removed >`, () => {
        $scope.config.message.content = '';
        $scope.$digest();

        expect(getMessageElement().length).toEqual(0);
      });

    });

    describe(`content is set up and hidden initially >`, () => {

      beforeEach(() => {
        $scope.config.message.content = 'Information message';
        $scope.config.message.visible = false;

        compileDirective();
      });

      it(`shouldn't be shown initially > `, () => {
        expect(getMessageElement().length).toEqual(0);
      });

      it(`can be shown later >`, () => {
        $scope.config.message.visible  = true;
        $scope.$digest();

        expect(getMessageElement().length).toEqual(1);
      });

      it(`should have correct content when it's shown >`, () => {
        $scope.config.message.visible  = true;
        $scope.$digest();

        expect(getMessageElement().text()).toEqual($scope.config.message.content);
      });

    });

  });

  describe(`'saveButton' >`, () => {

    function getSaveButton(): ng.IAugmentedJQuery {
      return compiledDirective.find(`.save-button`);
    }

    describe(`not described in config >`, () => {

      beforeEach(() => {
        delete $scope.config.saveButton;
        compileDirective();
      });

      it(`shouldn't be in DOM >`, () => {
        expect(getSaveButton().length).toEqual(0);
      });

    });

    describe(`default name and title >`, () => {

      beforeEach(() => {
        $scope.config.saveButton = {
          action: jasmine.createSpy('saveButton.action')
        };
        compileDirective();
      });

      it(`should be able to found by default name >`, () => {
        expect(getSaveButton().length).toEqual(1);
      });

      it(`should have default title`, () => {
        expect(getSaveButton().text()).toEqual('Save');
      });

    });

    describe(`config is set up >`, () => {

      beforeEach(() => {
        $scope.config.saveButton = {
          name: 'my-save-button',
          title: 'My Save Button',
          action: jasmine.createSpy('saveButton.action')
        };

        compileDirective();
      });

      it(`should have BUTTON with correct name >`, () => {
        expect(getSaveButton().find('button').attr('name')).toEqual($scope.config.saveButton.name);
      });

      it(`should have correct title >`, () => {
        expect(getSaveButton().text()).toEqual($scope.config.saveButton.title);
      });

      describe(`when enabled >`, () => {

        it(`should call a callback when clicked >`, () => {
          getSaveButton().click();

          expect($scope.config.saveButton.action).toHaveBeenCalled();
        });

      });

      describe(`when disabled > `, () => {

        beforeEach(() => {
          $scope.config.saveButton.disabled = true;
          $scope.$digest();
        });

        it(`shouldn't call a callback when clicked`, () => {
          getSaveButton().find('button').click();

          expect($scope.config.saveButton.action).not.toHaveBeenCalled();
        });

      });

    });

  });

  describe(`'applyButton' >`, () => {

    function getApplyButton(): ng.IAugmentedJQuery {
      return compiledDirective.find('.apply-button');
    }

    describe(`not described in config >`, () => {

      beforeEach(() => {
        delete $scope.config.applyButton;
        compileDirective();
      });

      it(`shouldn't be in DOM >`, () => {
        expect(getApplyButton().length).toEqual(0);
      });

    });

    describe(`default name and title >`, () => {

      beforeEach(() => {
        $scope.config.applyButton = {
          action: jasmine.createSpy('applyButton.action')
        };
        compileDirective();
      });

      it(`should be able to found by default name >`, () => {
        expect(getApplyButton().length).toEqual(1);
      });

      it(`should have default title`, () => {
        expect(getApplyButton().text().trim()).toEqual('Apply');
      });

    });

    describe(`config is set up >`, () => {

      beforeEach(() => {
        $scope.config.applyButton = {
          name: 'my-apply-button',
          title: 'My Apply Button',
          action: jasmine.createSpy('applyButton.action')
        };

        compileDirective();
      });

      it(`should have BUTTON with correct name >`, () => {
        expect(getApplyButton().find('button').attr('name')).toEqual($scope.config.applyButton.name);
      });

      it(`should have correct title >`, () => {
        expect(getApplyButton().text()).toEqual($scope.config.applyButton.title);
      });

      describe(`when enabled >`, () => {

        it(`should call a callback when clicked >`, () => {
          getApplyButton().find('button').click();

          expect($scope.config.applyButton.action).toHaveBeenCalled();
        });

      });

      describe(`when disabled > `, () => {

        beforeEach(() => {
          $scope.config.applyButton.disabled = true;
          $scope.$digest();
        });

        it(`shouldn't call a callback when clicked`, () => {
          getApplyButton().find('button').click();

          expect($scope.config.applyButton.action).not.toHaveBeenCalled();
        });

      });

    });

  });

  describe(`'cancelButton' >`, () => {

    function getCancelButton(): ng.IAugmentedJQuery {
      return compiledDirective.find('.cancel-button');
    }

    describe(`not described in config >`, () => {

      beforeEach(() => {
        delete $scope.config.cancelButton;
        compileDirective();
      });

      it(`shouldn't be in DOM >`, () => {
        expect(getCancelButton().length).toEqual(0);
      });

    });

    describe(`default name and title >`, () => {

      beforeEach(() => {
        $scope.config.cancelButton = {
          action: jasmine.createSpy('cancelButton.action')
        };
        compileDirective();
      });

      it(`should be able to found by default name >`, () => {
        expect(getCancelButton().length).toEqual(1);
      });

      it(`should have default title`, () => {
        expect(getCancelButton().text().trim()).toEqual('Cancel');
      });

    });

    describe(`config is set up >`, () => {

      beforeEach(() => {
        $scope.config.cancelButton = {
          name: 'my-cancel-button',
          title: 'My Cancel Button',
          action: jasmine.createSpy('cancelButton.action')
        };

        compileDirective();
      });

      it(`should have BUTTON with correct name >`, () => {
        expect(getCancelButton().find('button').attr('name')).toEqual($scope.config.cancelButton.name);
      });

      it(`should have correct title >`, () => {
        expect(getCancelButton().text().trim()).toEqual($scope.config.cancelButton.title);
      });

      describe(`when enabled >`, () => {

        it(`should call a callback when clicked >`, () => {
          getCancelButton().find('button').click();

          expect($scope.config.cancelButton.action).toHaveBeenCalled();
        });

      });

      describe(`when disabled > `, () => {

        beforeEach(() => {
          $scope.config.cancelButton.disabled = true;
          $scope.$digest();
        });

        it(`shouldn't call a callback when clicked`, () => {
          getCancelButton().find('button').click();

          expect($scope.config.cancelButton.action).not.toHaveBeenCalled();
        });

      });

    });

  });
});
