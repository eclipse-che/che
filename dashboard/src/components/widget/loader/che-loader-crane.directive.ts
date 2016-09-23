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
 * Defines a directive for animating iteration process
 * @author Oleksii Kurinnyi
 */
export class CheLoaderCrane {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($timeout, $window) {
    this.$timeout = $timeout;
    this.$window = $window;
    this.restrict = 'E';
    this.replace = true;
    this.templateUrl = 'components/widget/loader/che-loader-crane.html';

    // scope values
    this.scope = {
      step: '@cheStep',
      allSteps: '=cheAllSteps',
      excludeSteps: '=cheExcludeSteps',
      switchOnIteration: '=?cheSwitchOnIteration'
    };
  }

  link($scope, element) {
    let jqCrane = element.find('.che-loader-crane'),
      craneHeight = jqCrane.height(),
      craneWidth = jqCrane.width(),
      jqCraneLoad = element.find('#che-loader-crane-load'),
      jqCraneScaleWrap = element.find('.che-loader-crane-scale-wrapper'),
      jqCreateProjectContentPage = angular.element('#create-project-content-page'),
      jqBody = angular.element(document).find('body'),
      scaleStep = 0.05,
      scaleMin = 0.6,

      newStep,
      animationStopping = false,
      animationRunning = false;


    $scope.$watch(() => {
      return $scope.step;
    }, (newVal) => {
      newVal = parseInt(newVal, 10);

      // try to stop animation on last step
      if (newVal === $scope.allSteps.length - 1) {
        animationStopping = true;

        if (!$scope.switchOnIteration) {
          // stop animation immediately if it shouldn't wait until next iteration
          setNoAnimation();
        }
      }

      // skip steps excluded
      if ($scope.excludeSteps.indexOf(newVal) !== -1) {
        return;
      }

      newStep = newVal;

      // go to next step
      // if animation hasn't run yet or it shouldn't wait until next iteration
      if (!animationRunning || !$scope.switchOnIteration) {
        setAnimation();
        setCurrentStep();
      }
    });

    let destroyResizeEvent;
    $scope.$watch(() => {
      return element.find('.che-loader-crane:visible').length;
    }, (craneIsVisible) => {

      if (angular.isFunction(destroyResizeEvent)) {
        return;
      }

      jqCrane = element.find('.che-loader-crane');
      jqCraneLoad = element.find('#che-loader-crane-load');
      jqCraneScaleWrap = element.find('.che-loader-crane-scale-wrapper');
      jqCreateProjectContentPage = angular.element('#create-project-content-page');
      jqBody = angular.element(document).find('body');

      // initial resize
      this.$timeout(() => {
        setCraneSize();
      },0);

      let timeoutPromise;
      destroyResizeEvent = angular.element(this.$window).bind('resize', () => {
        if (timeoutPromise) {
          this.$timeout.cancel(timeoutPromise);
        }
        timeoutPromise = this.$timeout(() => {
          setCraneSize();
        }, 50);
      });
    });

    if ($scope.switchOnIteration) {
      element.find('.che-loader-animation.trolley-block').bind('animationstart', () => {
        animationRunning = true;
      });
      element.find('.che-loader-animation.trolley-block').bind('animationiteration', () => {
        setCurrentStep();

        if (animationStopping) {
          setNoAnimation();
        }
      });
    }

    let applyScale = (jqElement, scale) => {
        if (jqElement.nodeType) {
          jqElement = angular.element(jqElement);
        }
        jqElement.css('transform', 'scale('+scale+')');
        jqElement.css('height', craneHeight * scale);
        jqElement.css('width', craneWidth * scale);
      },
      hasScrollMoreThan = (domElement,diff) => {
        if (!domElement.nodeType) {
          domElement = domElement[0];
        }
        if (!domElement) {
          return;
        }
        return domElement.scrollHeight - domElement.offsetHeight > diff;
      },
      isVisibilityPartial = (domElement) => {
        if (!domElement.nodeType) {
          domElement = domElement[0];
        }
        if (!domElement) {
          return;
        }
        let rect = domElement.getBoundingClientRect();
        return rect.top < 0;
      },
      setCraneSize = () => {
        let scale = scaleMin;

        applyScale(jqCraneScaleWrap, scale);
        jqCraneScaleWrap.css('display','block');

        // do nothing if loader is hidden by hide-sm directive
        if (element.find('.che-loader-crane-scale-wrapper:visible').length === 0) {
          return;
        }

        // hide loader if there is scroll on minimal scale
        if (
          // check loader visibility on ide loading or factory loading
          (isVisibilityPartial(jqCrane)
          // check whether scroll is present on project creating page
          || hasScrollMoreThan(jqBody, 0) || hasScrollMoreThan(jqCreateProjectContentPage, 0))
          && scale === scaleMin) {
          jqCraneScaleWrap.css('display','none');
          return;
        }

        while (scale < 1) {
          applyScale(jqCraneScaleWrap, scale + scaleStep);

          // check for scroll appearance
          if (
            // check loader visibility on ide loading or factory loading
            isVisibilityPartial(jqCrane)
            // check whether scroll is present on project creating page
            || hasScrollMoreThan(jqBody, 0) || hasScrollMoreThan(jqCreateProjectContentPage, 0)) {
            applyScale(jqCraneScaleWrap, scale);
            break;
          }

          scale = scale + scaleStep;
        }
      },
      setAnimation = () => {
        jqCrane.removeClass('che-loader-no-animation');
      },
      setNoAnimation = () => {
        animationRunning = false;
        jqCrane.addClass('che-loader-no-animation');
      },
      setCurrentStep = () => {
        // clear all previously added 'step-#' and 'layer-#' classes
        for (let i = 0; i < $scope.allSteps.length; i++) {
          jqCrane.removeClass('step-' + i);
          jqCraneLoad.removeClass('layer-' + i);
        }

        // avoid next layer blinking
        let currentLayer = element.find('.layers-in-box').find('.layer-'+newStep);
        currentLayer.css('visibility','hidden');
        this.$timeout(() => {
          currentLayer.removeAttr('style');
        },500);

        jqCrane.addClass('step-' + newStep);
        jqCraneLoad.addClass('layer-' + newStep);
      };
  }
}
