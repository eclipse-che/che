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

/**
 * This class is handling the controller for the stack library filtering
 * @author Oleksii Kurinnyi
 */
export class CheStackLibraryFilterController {

  static $inject = ['$scope', '$mdConstant', '$timeout'];

  selectSuggestion: Function;
  suggestions: Array<string>;
  selectedIndex: number;

  private $scope: ng.IScope;
  private $timeout: ng.ITimeoutService;
  private chip: string;
  private keys: Array<string>;
  private stackTags: Array<string>;
  private selectedTags: Array<string>;
  private onTagsChanges: Function;

  /**
   * Default constructor that is using resource
   */
  constructor($scope: ng.IScope, $mdConstant: any, $timeout: ng.ITimeoutService) {
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.chip = '';
    this.suggestions = [];
    this.selectedIndex = -1;

    if (!angular.isArray(this.selectedTags)) {
      this.selectedTags = [];
    }
    this.keys = [$mdConstant.KEY_CODE.ENTER, $mdConstant.KEY_CODE.COMMA];
  }

  /**
   * Transform chip into tag
   * @param chip {string} the string to make a tag
   * @returns {string}
   */
  transformChip(chip: string): string {
    chip = chip.toLowerCase();
    // if partial input
    if (this.selectedIndex > -1) {
        chip = this.suggestions[this.selectedIndex].toLowerCase();
    }
    // if already selected
    if (this.selectedTags.some((tag: string) => tag.toLowerCase() === chip)) {
      return null;
    }
    // if match some tag
    let tag = this.suggestions.find((tag: string) => tag.toLowerCase() === chip);

    return tag ? tag : null;
  }

  /**
   * Callback called when tag added
   */
  onAdd(): void {
    this.onTagsChanges({tags: this.selectedTags});
  }

  /**
   * Callback called when tag removed
   */
  onRemove(): void {
    this.onTagsChanges({tags: this.selectedTags});
  }

  /**
   * Callback called when search query changed
   * @param query {string} the string to compare with tags
   */
  querySearch(query: string): void {
    query = query.trim().toLowerCase();

    let result = [];
    this.selectedIndex = -1;

    if (!query.length) {
      this.suggestions = result;
      return;
    }

    // exclude already selected tags and filter by substring
    result = this.stackTags.filter((tag: string) => this.selectedTags.indexOf(tag) === -1 && tag.toLowerCase().indexOf(query) === 0);

    this.suggestions = result;

    // update selected item
    if (this.suggestions.length) {
        this.selectedIndex = 0;
    }

    // set first suggestion as selected
    this.$timeout(() => {
      this.selectSuggestion(0);
    });
  }

  /**
   * Called when user clicked on suggestion to select it
   * @param tag {string}
   */
  onSelectSuggestion(tag: string): void {
    // add tag to selected
    if (this.transformChip(tag)) {
      this.selectedTags.push(tag);
      this.onAdd();
    }

    // clear suggestions and previous input
    this.chip = '';
    this.suggestions = [];
    this.selectedIndex = -1;
  }
}
