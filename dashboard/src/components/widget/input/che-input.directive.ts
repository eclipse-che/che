/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for creating input that are working either on desktop or on mobile devices.
 * It will change upon width of the screen
 * @author Florent Benoit
 */
export class CheInput {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.replace = true;
    this.transclude = true;

    // we require ngModel as we want to use it inside our directive
    this.require = ['ngModel'];

    // scope values
    this.scope = {
      valueModel: '=ngModel',
      inputName: '@cheName',
      labelName: '@?cheLabelName',
      placeHolder: '@chePlaceHolder',
      pattern: '@chePattern',
      myForm: '=cheForm',
      isChanged: '&ngChange'
    };

  }


  /**
   * Template for the current toolbar
   * @param element
   * @param attrs
   * @returns {string} the template
   */
  template(element, attrs) {

    var inputName = attrs.cheName;
    var labelName = attrs.cheLabelName || '';
    var placeHolder = attrs.chePlaceHolder;
    var pattern = attrs.chePattern;

    var template = '<div class="che-input">'
      + '<md-input-container hide-gt-xs ng-class="{\'che-input-mobile-no-label\': !labelName}">'
      + '<label ng-if="labelName">' + labelName + '</label>'
      + '<input type="text" name="' + inputName + '"';
    if (attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }

    template = template + ' ng-trim="false" data-ng-model="valueModel" >'
      + '<md-icon class="fa fa-pencil che-input-icon che-input-icon-xs"></md-icon>'
      + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.' + inputName + '.$error"></div>'
      + '</md-input-container>'
      + ''
      + '<div class="che-input-desktop" hide-xs layout="column">'
      + '<div layout="row" layout-align="start start">'
      + '<label flex="15" class="che-input-desktop-label" ng-if="labelName">' + labelName + ': </label>'
      + ''
      + '<div layout="column" class="che-input-desktop-value-column" flex="{{labelName ? 85 : \'none\'}}">'
      + '<input type="text" placeholder="' + placeHolder + '" ng-trim="false" name="desk' + inputName + '" style="{{labelName ? \'width: 100%\' : \'\'}}"';
    if (attrs.chePattern) {
      template = template + ' pattern="' + pattern + '"';
    }
    template = template + ' data-ng-model="valueModel">'
      + '<md-icon class="fa fa-pencil che-input-icon"></md-icon>';

    if (attrs.cheWidth === 'auto') {
      template = template + '<div class="che-input-desktop-hidden-text">{{valueModel ? valueModel : placeHolder}}</div>';
    }

    template = template + '<!-- display error messages for the form -->'
      + '<div ng-messages="myForm.desk' + inputName + '.$error" ng-transclude></div>'
      + '</div>'
      + '</div>'
      + '</div>'
      + '</div>';

    return template;
  }


  compile(element, attrs) {

    var keys = Object.keys(attrs);

    // search the input field
    var inputElement = element.find('input');

    var tabIndex;

    keys.forEach((key) => {

      // don't reapply internal properties
      if (key.indexOf('$') === 0) {
        return;
      }
      // don't reapply internal element properties
      if (key.indexOf('che') === 0) {
        return;
      }
      // avoid model
      if ('ngModel' === key) {
        return;
      }
      // don't reapply ngChange
      if ('ngChange' === key) {
        return;
      }
      var value = attrs[key];

      // remember tabindex
      if (key === 'tabindex') {
        tabIndex = value;
      }

      // handle empty values as boolean
      if (value === '') {
        value = 'true';
      }

      // set the value of the attribute
      inputElement.attr(attrs.$attr[key], value);


      //add also the material version of max length (only one the first input which is the md-input)
      if ('ngMaxlength' === key) {
        inputElement.eq(0).attr('md-maxlength', value);
      }

      element.removeAttr(attrs.$attr[key]);

    });


    // The focusable element is the input, remove tabIndex from top-level element
    element.attr('tabindex', -1);
    // The default value for tabindex on the input is 0 (meaning: set 0 if no value was set)
    if (!tabIndex) {
      inputElement.attr('tabindex', 0);
    }
  }

  /**
   * Keep reference to the model controller
   */
  link($scope, element, attr) {
    $scope.$watch(function () {
      return element.is(':visible');
    }, function () {
      //Since there are two inputs (for mobile and desktop versions) - add id attr only for visible one:
      if (attr.id) {
        element.find('input:hidden').removeAttr('id');
        element.find('input:visible').attr('id', attr.id);
      }
    });

    $scope.$watch('myForm.desk' + $scope.inputName + '.$pristine', (isPristine) => {
      if (isPristine) {
        element.addClass('desktop-pristine');
      } else {
        element.removeClass('desktop-pristine');
      }
    });

    if (!attr.ngChange) {
      return;
    }
    //for ngChange attribute only
    $scope.$watch('valueModel', () => {
      $scope.isChanged();
    });

  }
}
