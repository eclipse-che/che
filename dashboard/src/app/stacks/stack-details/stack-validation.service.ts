/*
 * Copyright (c) 2015-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
'use strict';

const COMPOSE = 'compose';
const DOCKERFILE = 'dockerfile';

/**
 * This class is handling the data for stack validation
 *
 * @author Oleksii Orel
 */
export class StackValidationService {

  /**
   * Return result of recipe validation.
   * @param stack {che.IStack}
   * @returns {IValidation}
   */
  getStackValidation(stack: che.IStack | {}): che.IValidation {
    let mandatoryKeys: Array<string> = ['name', 'workspaceConfig'];
    let additionalKeys: Array<string> = ['description', 'projects', 'tags', 'creator', 'scope', 'components', 'source'];
    let validKeys: Array<string> = mandatoryKeys.concat(additionalKeys);
    let errors: Array<string> = [];
    let isValid: boolean = true;
    // stack validation
    if (!stack || stack === {}) {
      isValid = false;
      errors.push('Error. The stack is empty.');
      return {isValid: isValid, errors: errors};
    }
    let objectKeys: Array<string> = Object.keys(stack);
    mandatoryKeys.forEach((key: string) => {
      if (objectKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is mandatory in stack.');
      }
    });
    objectKeys.forEach((key: string) => {
      if (validKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is redundant in stack.');
      }
    });
    // add workspace validation
    let workspaceValidation = this.getWorkspaceConfigValidation(stack.workspaceConfig);
    if (!workspaceValidation.isValid) {
      isValid = false;
      errors = errors.concat(workspaceValidation.errors);
    }

    return {isValid: isValid, errors: errors};
  }

  /**
   * Return result of workspaceConfig validation.
   * @param workspaceConfig {che.IWorkspaceConfig}
   * @returns {IValidation}
   */
  getWorkspaceConfigValidation(workspaceConfig: che.IWorkspaceConfig): che.IValidation {
    let mandatoryKeys: Array<string> = ['name', 'environments', 'defaultEnv'];
    let additionalKeys: Array<string> = ['commands', 'projects', 'description'];
    let validKeys: Array<string> = mandatoryKeys.concat(additionalKeys);
    let errors: Array<string> = [];
    let isValid: boolean = true;
    // workspace validation
    if (!workspaceConfig || workspaceConfig === {}) {
      isValid = false;
      errors.push('Error. The workspace is empty.');
      return {isValid: isValid, errors: errors};
    }
    let objectKeys: Array<string> = Object.keys(workspaceConfig);
    mandatoryKeys.forEach((key: string) => {
      if (objectKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is mandatory in workspaceConfig.');
      }
    });
    objectKeys.forEach((key: string) => {
      if (validKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is redundant in workspaceConfig.');
      }
    });
    // add environment validation
    let workspaceEnvironments = workspaceConfig.environments;
    if (workspaceEnvironments) {
      let keys: Array<string> = Object.keys(workspaceEnvironments);
      keys.forEach((key: string) => {
        let environment: che.IWorkspaceEnvironment = workspaceConfig.environments[key];
        let environmentValidation: che.IValidation = this.getEnvironmentValidation(environment);
        if (!environmentValidation.isValid) {
          isValid = false;
          errors = errors.concat(environmentValidation.errors);
        }
      });
    }

    return {isValid: isValid, errors: errors};
  }

  /**
   * Return result of environment validation.
   * @param environment {che.IWorkspaceEnvironment}
   * @returns {IValidation}
   */
  getEnvironmentValidation(environment: che.IWorkspaceEnvironment): che.IValidation {
    let errors: Array<string> = [];
    let isValid: boolean = true;
    // environment validation
    if (!environment || environment === {}) {
      isValid = false;
      errors.push('The environment is empty.');
      return {isValid: isValid, errors: errors};
    }
    // add machines validation
    let machines = environment.machines;
    let keys: Array<string> = machines ? Object.keys(machines) : [];
    if (keys.length === 0) {
      isValid = false;
      errors.push('The machines is empty.');
    }
    keys.forEach((key: string) => {
      let machine: che.IMachine = environment.machines[key];
      let machineValidation: che.IValidation = this.getMachineValidation(machine);
      if (!machineValidation.isValid) {
        isValid = false;
        errors = errors.concat(machineValidation.errors);
      }
    });
    // add recipe validation
    let recipeValidation = this.getRecipeValidation(environment.recipe);
    if (!recipeValidation.isValid) {
      isValid = false;
      errors = errors.concat(recipeValidation.errors);
    }

    return {isValid: isValid, errors: errors};
  }

  /**
   * Return result of machine validation.
   * @param machine {che.IMachine}
   * @returns {IValidation}
   */
  getMachineValidation(machine: che.IMachine): che.IValidation {
    let mandatoryKeys: Array<string> = ['attributes'];
    let additionalKeys: Array<string> = ['agents', 'servers'];
    let validKeys: Array<string> = mandatoryKeys.concat(additionalKeys);
    let errors: Array<string> = [];
    let isValid: boolean = true;
    // machine validation
    if (!machine) {
      isValid = false;
      errors.push('The machine is empty.');
      return {isValid: isValid, errors: errors};
    }
    let objectKeys: Array<string> = Object.keys(machine);
    mandatoryKeys.forEach((key: string) => {
      if (objectKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is mandatory in machine.');
      }
    });
    objectKeys.forEach((key: string) => {
      if (validKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is redundant in machine.');
      }
    });

    return {isValid: isValid, errors: errors};
  }

  /**
   * Return result of recipe validation.
   * @param recipe {che.IRecipe}
   * @returns {IValidation}
   */
  getRecipeValidation(recipe: che.IRecipe): che.IValidation {
    let mandatoryKeys: Array<string> = ['content', 'type'];
    let additionalKeys: Array<string> = ['contentType'];
    let validKeys: Array<string> = mandatoryKeys.concat(additionalKeys);
    let errors: Array<string> = [];
    let isValid: boolean = true;
    // recipe validation
    if (!recipe || !recipe.type || !recipe.content) {
      isValid = false;
      errors.push('The recipe is empty.');
      return {isValid: isValid, errors: errors};
    }
    let objectKeys: Array<string> = Object.keys(recipe);
    mandatoryKeys.forEach((key: string) => {
      if (objectKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is mandatory in recipe.');
      }
    });
    objectKeys.forEach((key: string) => {
      if (validKeys.indexOf(key) === -1) {
        isValid = false;
        errors.push('The key "' + key + '" is redundant in recipe.');
      }
    });
    if (DOCKERFILE === recipe.type) {
      if (!/^FROM\s+\w+/m.test(recipe.content)) {
        isValid = false;
        errors.push('The dockerfile is invalid.');
      }
    } else if (COMPOSE === recipe.type) {
      if (!/^services:\n/m.test(recipe.content)) {
        isValid = false;
        errors.push('The composefile is invalid.');
      }
    }

    return {isValid: isValid, errors: errors};
  }
}
