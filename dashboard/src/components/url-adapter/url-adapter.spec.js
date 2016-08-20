/*
 * Copyright (c) 2015-2016 Codenvy, S.A., Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mario Loriedo - initial implementation
 */
'use strict';

/**
 * Test the 
 * @author Mario Loriedo
 */

describe('url-adapter', function () {
  var $location;
  var cheBackend;
  var httpBackend;

  beforeEach(angular.mock.module('userDashboard'));

  beforeEach(inject(function (_$location_) {
    $location = _$location_;
    spyOn($location, 'host').and.returnValue('che.eclipse.org');
  }));

  beforeEach(inject(function (cheHttpBackend) {
    cheBackend = cheHttpBackend;
    httpBackend = cheHttpBackend.getHttpBackend();
  }));

  it('should update URL with IP address hostname', function () {
    inject(function (urlAdapter) {
      cheBackend.setup();
      httpBackend.flush();

      var validUrls = [
        ['http://111.222.111.222:8080/path/', 'http://che.eclipse.org:8080/path/']
      ];

      validUrls.forEach(function (url) {
        expect(urlAdapter.fixHostName(url[0])).toEqual(url[1]);
        expect($location.host).toHaveBeenCalled();
      });
    });
  });

  it('should update URLs with non IP address hostname', function () {
    inject(function (urlAdapter) {
      cheBackend.setup();
      httpBackend.flush();

      var validUrls = [
        ['http://www.example.com:8080/path/', 'http://che.eclipse.org:8080/path/']
      ];

      validUrls.forEach(function (url) {
        expect(urlAdapter.fixHostName(url[0])).toEqual(url[1]);
        expect($location.host).toHaveBeenCalled();
      });
    });
  });

  it('should update URLs using different protocols', function () {
    inject(function (urlAdapter) {
      cheBackend.setup();
      httpBackend.flush();

      var validUrls = [
        ['https://111.222.111.222:8080/path/', 'https://che.eclipse.org:8080/path/'],
        ['ws://111.222.111.222:8080/path/', 'ws://che.eclipse.org:8080/path/'],
        ['wss://111.222.111.222:8080/path/', 'wss://che.eclipse.org:8080/path/'],
        ['https://example.com:8080/path/', 'https://che.eclipse.org:8080/path/'],
        ['ws://example:8080/path/', 'ws://che.eclipse.org:8080/path/'],
        ['wss://www.example.com/path/', 'wss://che.eclipse.org/path/']
      ];

      validUrls.forEach(function (url) {
        expect(urlAdapter.fixHostName(url[0])).toEqual(url[1]);
        expect($location.host).toHaveBeenCalled();
      });
    });
  });

  it('should update URLs without port', function () {
    inject(function (urlAdapter) {
      cheBackend.setup();
      httpBackend.flush();

      var validUrls = [
        ['wss://www.example.com/path/', 'wss://che.eclipse.org/path/']
      ];

      validUrls.forEach(function (url) {
        expect(urlAdapter.fixHostName(url[0])).toEqual(url[1]);
        expect($location.host).toHaveBeenCalled();
      });
    });
  });

});

