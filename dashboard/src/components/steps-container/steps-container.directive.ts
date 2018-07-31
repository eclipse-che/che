/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * Defines a directive for displaying steps of creating project or loading workspace
 * @author Oleksii Kurinnyi
 */
export class CheStepsContainer implements ng.IDirective {

  restrict = 'E';
  templateUrl = 'components/steps-container/steps-container.html';

  scope = {
    allSteps: '=cheAllSteps',
    currentStep: '=cheCurrentStep'
  };

}
