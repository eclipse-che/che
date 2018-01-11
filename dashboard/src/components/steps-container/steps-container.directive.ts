/*
 * Copyright (c) 2015-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
