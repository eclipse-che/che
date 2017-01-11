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

/**
 * This class is handling the controller for the stack library filtering
 * @author Oleksii Kurinnyi
 */
export class CheStackLibraryFilterController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($scope, $mdConstant, $timeout) {
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.chip = '';
    this.suggestions = [];
    this.selectedTags = [];
    this.selectedIndex = -1;

    this.keys = [$mdConstant.KEY_CODE.ENTER, $mdConstant.KEY_CODE.COMMA];
  }

  /**
   * Transform chip into tag
   * @param chip the string to make a tag
   * @returns {*}
     */
  transformChip(chip) {
    chip = chip.toLowerCase();

    // if partial input
    if (this.selectedIndex > -1) {
        chip = this.suggestions[this.selectedIndex].toLowerCase();
    }

    // if already selected
    let selected = this.selectedTags.some(tag => tag.toLowerCase() === chip);
    if (selected) {
      return null;
    }

    // if match some tag
    let tag;
    for (var i=0; i<this.suggestions.length; i++) {
      if (this.suggestions[i].toLowerCase() === chip) {
          tag = this.suggestions[i];
          break;
      }
    }
    if (tag) {
      return tag;
    }

    // query string isn't matched any tag
    // don't add new tag
    return null;
  }

  /**
   * Callback called when tag added
   */
  onAdd() {
    this.$scope.$emit('event:updateFilter', this.selectedTags);
  }

  /**
   * Callback called when tag removed
   */
  onRemove() {
    this.$scope.$emit('event:updateFilter', this.selectedTags);
  }

  /**
   * Callback called when search query changed
   * @param query the string to compare with tags
   */
  querySearch(query) {
    query = query.trim().toLowerCase();

    let result = [];
    this.selectedIndex = -1;

    if (!query.length) {
      this.suggestions = result;
      return;
    }

    // exclude already selected tags and filter by substring
    result = this.ngModel.filter((tag) => this.selectedTags.indexOf(tag) === -1 && tag.toLowerCase().indexOf(query) === 0);

    this.suggestions = result;

    // update selected item
    if (this.suggestions.length) {
        this.selectedIndex = 0;
    }

    // set first suggestion as selected
    this.$timeout(() => {
        this.$scope.$broadcast('selectSuggestion');
    });
  }

  /**
   * Called when user clicked on suggestion to select it
   * @param tag
   */
  onSelectSuggestion(tag) {
    // add tag to selected
    if (this.transformChip(tag)){
      this.selectedTags.push(tag);
      this.onAdd(tag);
    }

    // clear suggestions and previous input
    this.chip = '';
    this.suggestions = [];
    this.selectedIndex = -1;
  }
}
