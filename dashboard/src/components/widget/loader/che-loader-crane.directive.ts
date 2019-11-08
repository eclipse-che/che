import { IAugmentedJQuery } from 'angular';
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

interface ICheLoaderCraneScope extends ng.IScope {
  step: string;
  allSteps: string[];
  excludeSteps: string[];
  switchOnIteration: boolean;
}

/**
 * Defines a directive for animating iteration process
 * @author Oleksii Kurinnyi
 */
export class CheLoaderCrane implements ng.IDirective {

  static $inject = ['$q', '$timeout', '$window'];

  $q: ng.IQService;
  $timeout: ng.ITimeoutService;
  $window: ng.IWindowService;

  restrict = 'E';
  replace = true;
  templateUrl = 'components/widget/loader/che-loader-crane.html';

  // scope values
  scope = {
    step: '@cheStep',
    allSteps: '=cheAllSteps',
    excludeSteps: '=cheExcludeSteps',
    switchOnIteration: '=?cheSwitchOnIteration'
  };

  /**
   * Default constructor that is using resource
   */
  constructor($q: ng.IQService, $timeout: ng.ITimeoutService, $window: ng.IWindowService) {
    this.$q = $q;
    this.$timeout = $timeout;
    this.$window = $window;
  }

  link($scope: ICheLoaderCraneScope, $element: ng.IAugmentedJQuery): void {
    const jqCraneScaleWrap = $element.find('.che-loader-crane-scale-wrapper'),
      jqCreateProjectContentPage = angular.element('#create-project-content-page'),
      jqBody = $element.closest('body');

    const stepsNumber = $scope.allSteps.length - $scope.excludeSteps.length;
    const loader = new Loader(this.$q, this.$timeout, $element, stepsNumber, $scope.switchOnIteration);

    const scaleStep = 0.05;
    const scaleMin = 0.6;
    let setCraneSize = () => {
      let scale = scaleMin;

      this.applyScale(scale, jqCraneScaleWrap, loader.height, loader.width);
      jqCraneScaleWrap.css('display', 'block');

      // do nothing if loader is hidden by hide-sm directive
      if ($element.find('.che-loader-crane-scale-wrapper:visible').length === 0) {
        return;
      }

      const loaderPartiallyHidden = this.elementPartiallyHidden(loader.element);
      const bodyHasScroll = this.elementHasScroll(jqBody);
      const createProjectContentPageHasScroll = this.elementHasScroll(jqCreateProjectContentPage);

      // hide loader if there is scroll on minimal scale
      if (
        // check loader visibility on ide loading or factory loading
        (loaderPartiallyHidden
          // check whether scroll is present on project creating page
          || bodyHasScroll || createProjectContentPageHasScroll)
        && scale === scaleMin) {
        jqCraneScaleWrap.css('display', 'none');
        return;
      }

      while (scale < 1) {
        this.applyScale(scale + scaleStep, jqCraneScaleWrap, loader.height, loader.width);

        // check for scroll appearance
        if (
          // check loader visibility on ide loading or factory loading
          loaderPartiallyHidden
          // check whether scroll is present on project creating page
          || bodyHasScroll || createProjectContentPageHasScroll) {
          this.applyScale(scale, jqCraneScaleWrap, loader.height, loader.width);
          break;
        }

        scale = scale + scaleStep;
      }
    };

    $scope.$watch(() => {
      return $scope.step;
    }, (nextStepStr: string) => {
      const nextStep = parseInt(nextStepStr, 10);

      // skip excluded step
      if ($scope.excludeSteps.indexOf(nextStepStr) !== -1) {
        return;
      }

      loader.setStep(nextStep);
      if (nextStep === $scope.allSteps.length) {
        loader.stopAnimation();
      }
    });

    let destroyResizeEvent;
    $scope.$watch(() => {
      return $element.find('.che-loader-crane:visible').length;
    }, () => {
      if (angular.isFunction(destroyResizeEvent)) {
        return;
      }

      // initial resize
      this.$timeout(() => {
        setCraneSize();
      }, 0);

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

  }

  applyScale(scale: number, element: ng.IAugmentedJQuery, elementHeight: number, elementWidth: number) {
    let jqElement = angular.element(element);
    jqElement.css('transform', 'scale(' + scale + ')');
    jqElement.css('height', elementHeight * scale);
    jqElement.css('width', elementWidth * scale);
  }

  // hasScroll
  elementHasScroll(element: ng.IAugmentedJQuery) {
    let domElement = element[0];
    if (!domElement) {
      return;
    }
    return domElement.scrollHeight - domElement.offsetHeight > 0;
  }

  elementPartiallyHidden(element: ng.IAugmentedJQuery): boolean {
    let domElement = element[0];
    if (!domElement) {
      return false;
    }
    let rect = domElement.getBoundingClientRect();
    return rect.top < 0;
  }

}

class Loader {
  private $q: ng.IQService;
  private $timeout: ng.ITimeoutService;
  private $element: ng.IAugmentedJQuery;

  private loader: ng.IAugmentedJQuery;
  // TODO: try to remove this
  private load: ng.IAugmentedJQuery;

  // if `true` then wait when current iteration ends and then render next step
  private changeAnimationOnIteration: boolean = true;
  private animationStopping: boolean = false;
  private animationRunning: boolean = false;
  private animationIterationDeferred: ng.IDeferred<void>;

  currentStep: number = 0;
  stepsNumber: number = 0;
  maxStepsNumber: number = 4;
  height: number;
  width: number;

  constructor(
    $q: ng.IQService,
    $timeout: ng.ITimeoutService,
    $element: ng.IAugmentedJQuery,
    stepsNumber: number,
    changeAnimationOnIteration: boolean
  ) {
    this.$q = $q;
    this.$timeout = $timeout;
    this.$element = $element;

    this.stepsNumber = stepsNumber;

    this.changeAnimationOnIteration = changeAnimationOnIteration;

    this.initialize();
  }

  private initialize(): void {
    this.loader = this.$element.find('.che-loader-crane');
    this.height = this.loader.height();
    this.width = this.loader.width();

    this.load = this.$element.find('#che-loader-crane-load');

    this.loader.find('.che-loader-animation.trolley-block .layer')
      .bind('animationstart', () => {
        this.animationRunning = true;
      })
      .bind('animationiteration', () => {
        this.animationIterationDeferred.resolve();
        this.animationIterationDeferred = this.$q.defer<void>();

        if (this.animationStopping) {
          this.animationRunning = false;
          this.loader.addClass('che-loader-no-animation');
          return;
        }
      });
  }

  get element(): ng.IAugmentedJQuery {
    return this.loader;
  }

  setStep(step: number): void {
    this.currentStep = step > this.maxStepsNumber ? this.maxStepsNumber : step;

    // stop animation on last step
    if (step === this.stepsNumber) {
      this.lastStep();
      return;
    }

    if (!this.animationIterationDeferred) {
      this.animationIterationDeferred = this.$q.defer<void>();
    }

    this.animationIterationDeferred.promise.then(() => {
      this.drawStep();
      this.loader.removeClass('che-loader-no-animation');
    });

    if (
      // if animation is not running at the moment
      !this.animationRunning
      // or it can be changed before an iteration ends
      || !this.changeAnimationOnIteration
    ) {
      this.animationIterationDeferred.resolve();
      this.animationIterationDeferred = this.$q.defer<void>();
    }
  }

  private lastStep(): void {
    if (!this.changeAnimationOnIteration) {
      // stop animation immediately
      this.stopAnimation();
    } else {
      // or wait until an iteration ends
      this.animationStopping = true;
    }
  }

  stopAnimation(): void {
    if (this.changeAnimationOnIteration === false) {
      this.animationRunning = false;
      this.loader.addClass('che-loader-no-animation');
    }
  }

  private drawStep(): void {
    // clear all previously added 'step-#' and 'layer-#' classes
    let steps = '';
    let layers = '';
    for (let i = 0; i < this.maxStepsNumber; i++) {
      steps += `step-${i}`;
      layers += `layers-${i}`;
    }
    this.loader.removeClass(steps);
    this.load.removeClass(layers);

    // avoid next layer blinking
    let currentLayer = this.loader.find('.layers-in-box').find('.layer-' + this.currentStep);
    currentLayer.css('visibility', 'hidden');

    this.$timeout(() => {
      currentLayer.removeAttr('style');
    }, 500);

    this.loader.addClass('step-' + this.currentStep);
    this.load.addClass('layer-' + this.currentStep);
  }

}
