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
 * This class is handling items selection and filtration.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHelper implements che.widget.ICheListHelper {
  /**
   * Angular filter service
   */
  private $filter: ng.IFilterService;

  /**
   * Name of unique key each item should have.
   */
  private idKey: string;
  /**
   * List of all items.
   */
  private allItemsList: any[];
  /**
   * List of filtered items.
   */
  private itemsList: any[];
  /**
   * Object which contains selection statuses of items.
   */
  private _itemsSelectionStatus: {
    [key: string]: boolean;
  };
  /**
   * <code>true</code> if all visible items are checked and checkbox for bulk operations is checked.
   */
  private isAllSelected: boolean;
  /**
   * <code>true</code> if no one item is checked.
   */
  private isNoSelected: boolean;
  /**
   * Filters.
   */
  private filters: {
    [name: string]: any[];
  };
  /**
   * Callback to define if item can be selected or not.
   */
  private isItemSelectable: (item: any) => boolean;

  constructor($filter: ng.IFilterService) {
    this.$filter = $filter;

    this.allItemsList = [];
    this.itemsList = [];
    this._itemsSelectionStatus = {};
    this.filters = {};

    this.isAllSelected = false;
    this.isNoSelected = true;
  }

  /**
   * Returns an object which contains selection statuses of items.
   *
   * @return {{[key: string]: boolean}}
   */
  get itemsSelectionStatus(): {[key: string]: boolean} {
    return this._itemsSelectionStatus;
  }

  /**
   * Returns <code>true</code> if all visible items are selected.
   *
   * @return {boolean}
   */
  get areAllItemsSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if no one item is selected.
   *
   * @return {boolean}
   */
  get isNoItemSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Select all visible items.
   */
  selectAllItems(): void {
    this.itemsList.forEach((item: any) => {
      const id = item[this.idKey];
      this._itemsSelectionStatus[id] = this.isItemSelectable(item);
    });
  }

  /**
   * Deselect all visible items.
   */
  deselectAllItems(): void {
    Object.keys(this._itemsSelectionStatus).forEach((key: string) => {
      this._itemsSelectionStatus[key] = false;
    });
  }

  /**
   * Inverts bulk selection.
   */
  changeBulkSelection(): void {
    if (this.isAllSelected) {
      this.deselectAllItems();
    } else {
      this.selectAllItems();
    }
    this.updateBulkSelectionStatus();
  }

  /**
   * Updates status of bulk checkbox.
   */
  updateBulkSelectionStatus(): void {
    const keysList = this.itemsList.filter((item: any) => {
      return this.isItemSelectable(item);
    }).map((item: any) => {
      return item[this.idKey];
    });

    if (keysList.length === 0) {
      this.isNoSelected = true;
      this.isAllSelected = false;
      return;
    }

    this.isNoSelected = true;
    this.isAllSelected = true;
    keysList.forEach((key: string) => {
      if (this._itemsSelectionStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });
  }

  /**
   * Returns list of selected items.
   *
   * @return {any[]}
   */
  getSelectedItems(): any[] {
    return this.itemsList.filter((item: any) => {
      const id = item[this.idKey];
      return this._itemsSelectionStatus[id];
    });
  }

  /**
   * Returns list of visible items.
   *
   * @return {any[]}
   */
  getVisibleItems(): any[] {
    return this.itemsList;
  }

  /**
   * Returns number of visible items.
   *
   * @return {number}
   */
  get visibleItemsNumber(): number {
    return this.itemsList.length;
  }

  /**
   * Store list of items.
   * Check if every item contains an unique key.
   *
   * @param {any[]} itemsList list of items
   * @param {string} key an unique key name
   * @param {Function=} isSelectable callback to define whether item is selectable or not
   */
  setList(itemsList: any[], key: string, isSelectable?: (item: any) => boolean): void {
    this.allItemsList = [];
    this.itemsList = [];

    if (angular.isFunction(isSelectable)) {
      this.isItemSelectable = isSelectable;
    } else {
      this.isItemSelectable = () => { return true; };
    }

    this.idKey = key;

    itemsList.forEach((item: any) => {
      if (!item[this.idKey]) {
        throw TypeError(`Cannot find key "${this.idKey}" in ${angular.toJson(item)}`);
      }

      this.allItemsList.push(item);
    });

    this.doFilterList();
    this.refreshSelected();
  }

  /**
   * Apply a filter.
   *
   * @param {string} name a filter name
   * @param {any[]} filterProps a filter properties (expression, comparator, anyPropertyKey)
   */
  applyFilter(name: string, ...filterProps: any[]): void {
    this.filters[name] = filterProps;

    this.doFilterList();
  }

  /**
   * Removes all filters.
   */
  clearFilters(): void {
    this.filters = {};

    this.doFilterList();
  }

  /**
   * Performs list filtering using filters.
   */
  private doFilterList(): void {
    this.itemsList = angular.copy(this.allItemsList);

    const filterNames = Object.keys(this.filters);

    if (filterNames.length === 0) {
      return;
    }

    filterNames.forEach((name: string) => {
      this.itemsList = this.$filter('filter')(this.itemsList, ...this.filters[name]);
    });

    const visibleItemIds = this.itemsList.map((item: any) => {
      return item[this.idKey];
    });

    // deselect hidden items
    Object.keys(this._itemsSelectionStatus).forEach((key: string) => {
      this._itemsSelectionStatus[key] = visibleItemIds.indexOf(key) === -1 ? false : this._itemsSelectionStatus[key];
    });
    this.updateBulkSelectionStatus();
  }

  /**
   * Updates statuses of selected items.
   */
  private refreshSelected(): void {
    const removeFromSelectedKeys = Object.keys(this._itemsSelectionStatus);

    this.allItemsList.forEach((item: any) => {
      const index = removeFromSelectedKeys.indexOf(item[this.idKey]);
      if (index !== -1) {
        removeFromSelectedKeys.splice(index, 1);
      }
    });

    removeFromSelectedKeys.forEach((key: string) => {
      delete this._itemsSelectionStatus[key];
    });

    this.updateBulkSelectionStatus();
  }

}
