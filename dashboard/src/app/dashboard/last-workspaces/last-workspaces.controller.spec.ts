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
import { DashboardLastWorkspacesController } from "./last-workspaces.controller";
import { CheWorkspace } from "../../../components/api/workspace/che-workspace.factory";
import { CheNotification } from "../../../components/notification/che-notification.factory";

declare const expect: (param: any) => {
  toHaveBeenCalledTimes: any;
  toBeTruthy: () => any;
  toBeFalsy: () => any;
  toBe: any;
  not: any;
};

/**
 * @author Lucia Jelinkova
 */
describe(`Last workspaces controller >`, () => {

  let controller: DashboardLastWorkspacesController;
  let cheWorkspace: CheWorkspace;
  let cheNotification: CheNotification;

  let createGeneralError = () => Promise.reject('This is some error');
  let createHTTPError = (message: string) => Promise.reject({
    'status': status,
    'data': {
      'message': message
    }
  });

  beforeEach(() => {
    // tell angular to mock the module
    angular.mock.module('userDashboard');

    // retrieve all necessary services
    inject((
      _$controller_: ng.IControllerService,
      _cheWorkspace_: CheWorkspace,
      _cheNotification_: CheNotification) => {

      // get the tested controller from ng.IControllerService
      controller = _$controller_('DashboardLastWorkspacesController');
      cheWorkspace = _cheWorkspace_;
      cheNotification = _cheNotification_
    })
  });

  it('loadData - workspaces pre-loaded', async () => {
    spyOn(cheWorkspace, 'getWorkspaces').and.returnValue([jasmine.createSpy('IWorkspace')]);
    spyOn(cheWorkspace, 'fetchWorkspaces');
    spyOn(cheNotification, 'showError');

    expect(controller.isLoading).toBeTruthy();

    await controller.loadData();

    expect(controller.isLoading).toBeFalsy();
    expect(cheWorkspace.getWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheWorkspace.fetchWorkspaces).not.toHaveBeenCalled();
    expect(cheNotification.showError).not.toHaveBeenCalled();
    expect(controller.workspaces.length).toBe(1);
  });

  it('loadData - fetch workspaces - no workspaces', async () => {
    spyOn(cheWorkspace, 'getWorkspaces').and.returnValue([]);
    spyOn(cheWorkspace, 'fetchWorkspaces').and.returnValue(Promise.resolve([]));
    spyOn(cheNotification, 'showError');

    expect(controller.isLoading).toBeTruthy();

    await controller.loadData();

    expect(controller.isLoading).toBeFalsy();
    expect(cheWorkspace.getWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheWorkspace.fetchWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheNotification.showError).not.toHaveBeenCalled();
    expect(controller.workspaces.length).toBe(0);
  });

  it('loadData - fetch workspaces', async () => {
    spyOn(cheWorkspace, 'getWorkspaces').and.returnValue([]);
    spyOn(cheWorkspace, 'fetchWorkspaces').and.returnValue(Promise.resolve([jasmine.createSpy('IWorkspace')]));
    spyOn(cheNotification, 'showError');

    expect(controller.isLoading).toBeTruthy();

    await controller.loadData();

    expect(controller.isLoading).toBeFalsy();
    expect(cheWorkspace.getWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheWorkspace.fetchWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheNotification.showError).not.toHaveBeenCalled();
    expect(controller.workspaces.length).toBe(1);
  });

  it('loadData - general error handling', async () => {
    spyOn(cheWorkspace, 'getWorkspaces').and.returnValue([]);
    spyOn(cheWorkspace, 'fetchWorkspaces').and.returnValue(createGeneralError());
    spyOn(cheNotification, 'showError').and.callFake((args) => {
      expect(args).toBe('Update workspaces failed.');
    });

    expect(controller.isLoading).toBeTruthy();

    await controller.loadData();

    expect(controller.isLoading).toBeFalsy();
    expect(cheWorkspace.getWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheWorkspace.fetchWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheNotification.showError).toHaveBeenCalledTimes(1);
  });

  it('loadData - http error handling', async () => {
    spyOn(cheWorkspace, 'getWorkspaces').and.returnValue([]);
    spyOn(cheWorkspace, 'fetchWorkspaces').and.returnValue(createHTTPError('Error message'));
    spyOn(cheNotification, 'showError').and.callFake((args) => {
      expect(args).toBe('Error message');
    });
    expect(controller.isLoading).toBeTruthy();

    await controller.loadData();

    expect(controller.isLoading).toBeFalsy();
    expect(cheWorkspace.getWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheWorkspace.fetchWorkspaces).toHaveBeenCalledTimes(1);
    expect(cheNotification.showError).toHaveBeenCalledTimes(1);
  });
});

