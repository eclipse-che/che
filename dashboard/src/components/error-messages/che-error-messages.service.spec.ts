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
import {CheErrorMessagesService} from './che-error-messages.service';
import {CheHttpBackend} from '../api/test/che-http-backend';

/**
 * Test of the CheErrorMessagesService
 *
 * @author Oleksii Kurinnyi
 */
describe('CheErrorMessagesService', () => {

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject((cheHttpBackend: CheHttpBackend) => {
    httpBackend = cheHttpBackend.getHttpBackend();
    // avoid tracking requests from branding controller
    httpBackend.whenGET(/.*/).respond(200, '');
    httpBackend.when('OPTIONS', '/api/').respond({});
  }));

  describe('for each namespace', () => {

    let errorMessagesService;

    let namespace1 = 'namespace1',
        errorName1 = 'errorName1',
        errorText1_1 = 'error text #1_1',
        errorText1_2 = 'error text #1_2';

    let namespace2 = 'namespace2',
        errorName2 = 'errorName2',
        errorText2_1 = 'error text #2_1';

    beforeEach(() => {
      errorMessagesService = new CheErrorMessagesService();

      errorMessagesService.addMessage(namespace1, errorName1, errorText1_1);
      errorMessagesService.addMessage(namespace1, errorName1, errorText1_2);

      errorMessagesService.addMessage(namespace2, errorName2, errorText2_1);
    });

    it('should return correct number of messages', () => {
      let messages1 = errorMessagesService.getMessages(namespace1);
      expect(messages1.length).toEqual(2);

      let messages2 = errorMessagesService.getMessages(namespace2);
      expect(messages2.length).toEqual(1);
    });

    it('should prevent to add duplicates', () => {
      errorMessagesService.addMessage(namespace1, errorName1, errorText1_1);

      let messages1 = errorMessagesService.getMessages(namespace1);
      expect(messages1.length).toEqual(2);
    });

    it('should return correct messages', () => {
      let messages1 = errorMessagesService.getMessages(namespace1);
      expect(messages1[0]).toContain(errorName1);
      expect(messages1[0]).toContain(errorText1_1);
      expect(messages1[0]).toContain(errorName1);
      expect(messages1[0]).toContain(errorText1_1);

      let messages2 = errorMessagesService.getMessages(namespace2);
      expect(messages2[0]).toContain(errorName2);
      expect(messages2[0]).toContain(errorText2_1);
      expect(messages2[0]).toContain(errorName2);
      expect(messages2[0]).toContain(errorText2_1);
    });

    it('should remove messages for specified namespace', () => {
      errorMessagesService.removeMessages(namespace1, errorName1);

      // no messages for namespace #1
      let messages1 = errorMessagesService.getMessages(namespace1);
      expect(messages1.length).toEqual(0);

      // namespace #2 still contains messages
      let messages2 = errorMessagesService.getMessages(namespace2);
      expect(messages2.length).toEqual(1);
    });

    it('should notify observers', () => {
      let callback1 = jasmine.createSpy('callback1'),
          callback2 = jasmine.createSpy('callback2');

      errorMessagesService.registerCallback(namespace1, callback1);
      errorMessagesService.registerCallback(namespace2, callback2);

      errorMessagesService.addMessage(namespace1, 'customValidation', 'customValidation failed!');
      let messages1 = errorMessagesService.getMessages(namespace1);

      expect(callback1).toHaveBeenCalled();
      expect(callback1).toHaveBeenCalledWith(jasmine.objectContaining(messages1));

      expect(callback2).not.toHaveBeenCalled();
    });

  });

});
