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

import {IParser} from './parser';

export type ISupportedListItem = IPodItem | IDeploymentItem | IConfigMapItem | ISecretItem;

export function isSupportedItem(item: any): item is ISupportedListItem {
  return isDeploymentItem(item) || isPodItem(item) || isConfigMapItem(item) || isSecretItem(item);
}

export function isDeploymentItem(item: ISupportedListItem): item is IDeploymentItem {
  return (item.kind && item.kind.toLowerCase() === 'deployment');
}

export function isPodItem(item: ISupportedListItem): item is IPodItem {
  return (item.kind && item.kind.toLowerCase() === 'pod');
}

export function isConfigMapItem(item: ISupportedListItem): item is IConfigMapItem {
  return (item.kind && item.kind.toLowerCase() === 'configmap');
}

export function isSecretItem(item: ISupportedListItem): item is ISecretItem {
  return (item.kind && item.kind.toLowerCase() === 'secret');
}

export function getPodItemOrNull(item: ISupportedListItem): IPodItem {
  if (isDeploymentItem(item)) {
    return item.spec.template;
  } else if (isPodItem(item)) {
    return item;
  } else {
    return null;
  }
}

export interface IObjectMetadata {
  name?: string;
  generateName?: string;
  annotations?: { [propName: string]: string };
  labels?: { [propName: string ]: string };
  [propName: string]: string | Object;
}

export interface IDeploymentItem {
  apiVersion: string;
  kind: string;
  metadata: IObjectMetadata;
  spec: {
    replicas: number;
    selector: {
      matchLabels: { [propName: string]: string | Object };
    }
    template: IPodItem
  };
}

export interface IPodItem {
  apiVersion: string;
  kind: string;
  metadata: IObjectMetadata;
  spec: { containers: Array<IPodItemContainer> };
  [propName: string]: string | Object;
}

export interface IPodItemContainer {
  name: string;
  image: string;
  resources ?: {
    limits?: {
      memory?: string;
    }
  };
  [propName: string]: string | Object;
}

export interface IConfigMapItem {
  apiVersion: string;
  kind: string;
  metadata: IObjectMetadata;
  data: { [propName: string]: string | Object };
}

export interface ISecretItem {
  apiVersion: string;
  kind: string;
  metadata: IObjectMetadata;
  data?: { [propName: string]: string | Object };
  stringData?: { [propName: string]: string | Object};
}

/**
 * Wrapper for jsyaml and simple validator for kubernetes machine recipe.
 *
 *  @author Oleksii Orel
 */
export class KubernetesMachineRecipeParser implements IParser {
  private recipeByContent: Map<string, ISupportedListItem> = new Map();
  private recipeKeys: Array<string> = [];

  /**
   * Parses recipe content
   *
   * @param content {string} recipe content
   * @returns {ISupportedListItem} recipe object
   */
  parse(content: string): ISupportedListItem {
    let recipe: ISupportedListItem;
    if (this.recipeByContent.has(content)) {
      recipe = angular.copy(this.recipeByContent.get(content));
      this.validate(recipe);
      return recipe;
    }
    recipe = jsyaml.safeLoad(content);
    // add to buffer
    this.recipeByContent.set(content, angular.copy(recipe));
    this.recipeKeys.push(content);
    if (this.recipeKeys.length > 10) {
      this.recipeByContent.delete(this.recipeKeys.shift());
    }
    this.validate(recipe);

    return recipe;
  }

  /**
   * Dumps recipe object.
   *
   * @param recipe {IPodItem | IDeploymentItem | IConfigMapItem} recipe object
   * @returns {string} recipe content
   */
  dump(recipe: ISupportedListItem): string {
    return jsyaml.safeDump(recipe, {'indent': 1});
  }

  validate(recipe: ISupportedListItem ): void {
    if (!recipe || !recipe.kind) {
      throw new TypeError(`Recipe item should contain a 'kind' section.`);
    }
    if (!recipe.apiVersion) {
      throw new TypeError(`Recipe item should contain 'apiVersion' section`);
    }
    if (isDeploymentItem(recipe)) {
      this.validateDeployment(<IDeploymentItem>recipe);
    } else if (isPodItem(recipe)) {
      this.validatePod(<IPodItem>recipe);
    } else if (isConfigMapItem(recipe)) {
      this.validateConfigMap(<IConfigMapItem>recipe);
    } else if (isSecretItem(recipe)) {
      this.validateSecret(<ISecretItem> recipe);
    }
  }

  /**
   * Simple validation of Deployment recipe.
   *
   * @param deployment
   */
  validateDeployment(deployment: IDeploymentItem): void {
    this.validateMetadata(deployment.metadata);
    if (!deployment.spec) {
      throw new TypeError(`Recipe deployment item should contain a 'spec' section.`);
    }
    const spec = deployment.spec;
    if (!spec.replicas || spec.replicas !== 1) {
      throw new TypeError(`Recipe deployment spec should contain replicas value equal to 1.`);
    }
    if (!spec.template) {
      throw new TypeError(`Recipe deployment spec should contain template section.`);
    }
    if (!spec.selector || !spec.selector.matchLabels) {
      throw new TypeError(`Recipe deployment spec should contain selector section.`);
    }
    this.validatePod(spec.template);
    // for the deployment to work, the matchlabels section needs to match the labels
    // applied to the Pod.
    const matchLabels = spec.selector.matchLabels;
    const podLabels = spec.template.metadata.labels;
    if (!podLabels) {
      throw new TypeError(`Recipe deployment spec matchLabels must match pod labels.`);
    }
    for (const key of Object.keys(matchLabels)) {
      if (matchLabels[key] !== podLabels[key]) {
        throw new TypeError(`Recipe deployment matchLabels must match pod labels.`);
      }
    }
    // since match labels are ANDed, we also need the same labels
    for (const key of Object.keys(podLabels)) {
      if (podLabels[key] !== matchLabels[key]) {
        throw new TypeError(`Recipe deployment matchLabels must match pod labels.`);
      }
    }
  }

  /**
   * Simple validation of Pod recipe.
   *
   * @param pod {IPodItem}
   */
  validatePod(pod: IPodItem): void {
    this.validateMetadata(pod.metadata);
    if (!pod.spec) {
      throw new TypeError(`Recipe pod item should contain 'spec' section.`);
    }
    if (!pod.spec.containers) {
      throw new TypeError(`Recipe pod item spec should contain 'containers' section.`);
    }
    if (!angular.isArray(pod.spec.containers) || pod.spec.containers.length === 0) {
      throw new TypeError(`Recipe pod item spec containers should contain at least one 'container'.`);
    }
    pod.spec.containers.forEach((podItemContainer: IPodItemContainer) => {
      if (!podItemContainer) {
        return;
      }
      if (!podItemContainer.name) {
        throw new TypeError(`Recipe pod item container should contain 'name' section.`);
      }
      if (podItemContainer.name && !this.testName(podItemContainer.name)) {
        throw new TypeError(`Recipe pod item container name should not contain special characters like dollar, etc.`);
      }
      if (!podItemContainer.image) {
        throw new TypeError(`Recipe pod item container should contain 'image' section.`);
      }
    });
  }

  validateConfigMap(configMap: IConfigMapItem) {
    this.validateMetadata(configMap.metadata);
    if (!configMap.data) {
      throw new TypeError(`Recipe config map item should contain data section.`);
    }
  }

  validateSecret(secret: ISecretItem) {
    this.validateMetadata(secret.metadata);
    if (!secret.data && !secret.stringData) {
      throw new TypeError(`Recipe secret item should contain either data or stringData section.`);
    }
    // secret.data values must also be base64 encoded but nodejs doesn't allow an easy way to check
    // if the encoding is valid (ignores errors silently).
  }

  validateMetadata(metadata: IObjectMetadata) {
    if (!metadata) {
      throw new TypeError(`Recipe item should contain 'metadata' section.`);
    }
    if (!metadata.name && !metadata.generateName) {
      throw new TypeError(`Recipe item metadata should contain 'name' section.`);
    }
    if (metadata.name && !this.testName(metadata.name)) {
      throw new TypeError(`Recipe item container name should not contain special characters like dollar, etc.`);
    }
  }

  /**
   * Returns true if the name is valid.
   * @param name {string}
   * @returns {boolean}
   */
  private testName(name: string): boolean {
    return /^[a-z0-9]([-a-z0-9]*[a-z0-9])?$/.test(name);
  }
}
