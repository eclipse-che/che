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
import {CheListHelper} from './che-list-helper';
import {CheListHelperMock} from './che-list-helper.mock';
import {CheListHelperFactory} from './che-list-helper.factory';

describe('CheListHelper >', () => {
  let $scope;

  let cheListHelper: CheListHelper;

  let mock: CheListHelperMock;

  let itemsList, idKey;

  /**
   * Setup module
   */
  beforeEach(angular.mock.module('userDashboard'));

  /**
   * Inject service
   */
  beforeEach(inject((_$rootScope_: ng.IRootScopeService, cheListHelperFactory: CheListHelperFactory) => {
    $scope = _$rootScope_;
    cheListHelper = cheListHelperFactory.getHelper('test');

    mock = new CheListHelperMock();
    mock.mockData();
  }));

  describe('list contains some invalid items >', () => {

    beforeEach(() => {
      itemsList = mock.getItemsList();
      idKey = mock.getIdKey();

      const item = itemsList[0];
      delete item[idKey];
    });

    it(`should throw an exception if some item doesn't contain an unique key`, () => {
      const setList = () => {
        cheListHelper.setList(itemsList, idKey);
      };

      expect(setList).toThrowError(TypeError);
    });

  });

  describe('list contains all valid items >', () => {

    beforeEach(() => {
      itemsList = mock.getItemsList();
      idKey = mock.getIdKey();

      cheListHelper.setList(itemsList, idKey);
    });

    describe('bulk checkbox is disabled >', () => {

      it('should return correct number of visible items', () => {
        const visibleItemsList = cheListHelper.getVisibleItems();
        expect(visibleItemsList.length).toEqual(itemsList.length);
      });

      it('should have bulk checkbox disabled', () => {
        expect(cheListHelper.areAllItemsSelected).toBeFalsy();
      });

      it('should not have any item selected', () => {
        expect(cheListHelper.areAllItemsSelected).toBeFalsy();

        const selectedItemsList = cheListHelper.getSelectedItems();
        expect(selectedItemsList.length).toEqual(0);
      });

      describe('few items are checked >', () => {
        let checkedItemNumbers;

        let checkedItems;

        beforeEach(() => {
          checkedItemNumbers = [1, 3, 5, 7];
          checkedItems = itemsList.filter((item: any, i: number) => {
            return checkedItemNumbers.indexOf(i) !== -1;
          });

          checkedItems.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = true;
          });
          cheListHelper.updateBulkSelectionStatus();

        });

        it('should have bulk checkbox disabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeFalsy();
          expect(cheListHelper.isNoItemSelected).toBeFalsy();
        });

        it('should have correct number of items checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(checkedItems.length);
        });

        describe(`items are filtered by name which contains '3' >`, () => {
          let visibleItemName;

          let filter;

          let visibleItems;

          beforeEach(() => {
            // apply filter by name
            visibleItemName = '3';
            filter = mock.createFilterByName(visibleItemName); // not exact match

            cheListHelper.applyFilter('name', ...filter);

            // calculate expected number of visible items
            const re = new RegExp(visibleItemName);
            visibleItems = itemsList.filter((item: any) => {
              return re.test(item.name);
            });
            const visibleItemIds = visibleItems.map((item: any) => {
              return item[idKey];
            });

            // calculate expected number of checked items
            checkedItems = checkedItems.filter((item: any) => {
              return visibleItemIds.indexOf(item[idKey]) > -1;
            });
          });

          it('should have bulk checkbox disabled', () => {
            expect(cheListHelper.areAllItemsSelected).toBeFalsy();
            expect(cheListHelper.isNoItemSelected).toBeFalsy();
          });

          it('should have correct number of items checked', () => {
            const selectedItemsList = cheListHelper.getSelectedItems();
            expect(selectedItemsList.length).toEqual(checkedItems.length);
          });

        });

      });

      describe('all items are checked >', () => {

        beforeEach(() => {
          itemsList.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = true;
          });
          cheListHelper.updateBulkSelectionStatus();
        });

        it('should have bulk checkbox enabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeTruthy();
          expect(cheListHelper.isNoItemSelected).toBeFalsy();
        });

        it('should have correct number of items checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(itemsList.length);
        });

        describe(`items are filtered by name which contains '4' >`, () => {
          let checkedItems;

          let visibleItemName;

          let filter;

          let visibleItems;

          beforeEach(() => {
            // apply filter by name
            visibleItemName = '4';
            filter = mock.createFilterByName(visibleItemName); // not exact match

            cheListHelper.applyFilter('name', ...filter);

            // calculate expected number of visible items
            const re = new RegExp(visibleItemName);
            visibleItems = itemsList.filter((item: any) => {
              return re.test(item.name);
            });
            const visibleItemIds = visibleItems.map((item: any) => {
              return item[idKey];
            });

            // calculate expected number of checked items
            checkedItems = itemsList.filter((item: any) => {
              return visibleItemIds.indexOf(item[idKey]) > -1;
            });
          });

          it('should have bulk checkbox enabled', () => {
            expect(cheListHelper.areAllItemsSelected).toBeTruthy();
            expect(cheListHelper.isNoItemSelected).toBeFalsy();
          });

          it('should have correct number of items checked', () => {
            const selectedItemsList = cheListHelper.getSelectedItems();
            expect(selectedItemsList.length).toEqual(checkedItems.length);
          });

        });

      });

    });

    describe('bulk checkbox is enabled >', () => {

      beforeEach(() => {
        cheListHelper.changeBulkSelection();
      });

      it('should return correct number of visible items', () => {
        const visibleItemsList = cheListHelper.getVisibleItems();
        expect(visibleItemsList.length).toEqual(itemsList.length);
      });

      it('should have bulk checkbox enabled', () => {
        expect(cheListHelper.areAllItemsSelected).toBeTruthy();
        expect(cheListHelper.isNoItemSelected).toBeFalsy();
      });

      it('should have all items selected', () => {
        const selectedItemsList = cheListHelper.getSelectedItems();
        expect(selectedItemsList.length).toEqual(itemsList.length);
      });

      describe('few items are unchecked >', () => {
        let uncheckedItemNumbers;

        let uncheckedItems;

        beforeEach(() => {
          uncheckedItemNumbers = [1, 3, 5, 7];
          uncheckedItems = itemsList.filter((item: any, i: number) => {
            return uncheckedItemNumbers.indexOf(i) === -1;
          });

          uncheckedItems.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = false;
          });
          cheListHelper.updateBulkSelectionStatus();

        });

        it('should have bulk checkbox disabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeFalsy();
          expect(cheListHelper.isNoItemSelected).toBeFalsy();
        });

        it('should have correct number of items checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(itemsList.length - uncheckedItems.length);
        });

      });

      describe('all items are unchecked >', () => {

        beforeEach(() => {
          itemsList.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = false;
          });
          cheListHelper.updateBulkSelectionStatus();

        });

        it('should have bulk checkbox disabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeFalsy();
          expect(cheListHelper.isNoItemSelected).toBeTruthy();
        });

        it('should not have any item checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(0);
        });

      });

    });

  });

  describe('list contains unselectable items >', () => {
    let selectableItemNumbers: number[];

    let isItemSelectable: (item: any) => boolean;

    beforeEach(() => {
      itemsList = mock.getItemsList();
      idKey = mock.getIdKey();

      selectableItemNumbers = [1, 3, 5, 13];
      const re = new RegExp(`-(?:${selectableItemNumbers.join('|')})$`);
      isItemSelectable = (item: any) => {
        return re.test(item.id);
      };
      cheListHelper.setList(itemsList, idKey, isItemSelectable);
    });

    describe('bulk checkbox is disabled >', () => {

      it('should return correct number of visible items', () => {
        const visibleItemsList = cheListHelper.getVisibleItems();
        expect(visibleItemsList.length).toEqual(itemsList.length);
      });

      it('should have bulk checkbox disabled', () => {
        expect(cheListHelper.areAllItemsSelected).toBeFalsy();
      });

      it('should not have any item selected', () => {
        expect(cheListHelper.areAllItemsSelected).toBeFalsy();

        const selectedItemsList = cheListHelper.getSelectedItems();
        expect(selectedItemsList.length).toEqual(0);
      });

      describe('few of the selectable items are checked >', () => {
        let checkedItemNumbers;

        let checkedItems;

        beforeEach(() => {
          checkedItemNumbers = selectableItemNumbers.slice(0, 2);
          checkedItems = itemsList.filter((item: any, i: number) => {
            return checkedItemNumbers.indexOf(i) !== -1;
          });

          checkedItems.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = true;
          });
          cheListHelper.updateBulkSelectionStatus();

        });

        it('should have bulk checkbox disabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeFalsy();
          expect(cheListHelper.isNoItemSelected).toBeFalsy();
        });

        it('should have correct number of items checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(checkedItems.length);
        });

        describe(`items are filtered by name which contains '3' >`, () => {
          let visibleItemName;

          let filter;

          let visibleItems;

          beforeEach(() => {
            // apply filter by name
            visibleItemName = '3';
            filter = mock.createFilterByName(visibleItemName); // not exact match

            cheListHelper.applyFilter('name', ...filter);

            // calculate expected number of visible items
            const re = new RegExp(visibleItemName);
            visibleItems = itemsList.filter((item: any) => {
              return re.test(item.name);
            });
            const visibleItemIds = visibleItems.map((item: any) => {
              return item[idKey];
            });

            // calculate expected number of checked items
            checkedItems = checkedItems.filter((item: any) => {
              return visibleItemIds.indexOf(item[idKey]) > -1;
            });
          });

          it('should have bulk checkbox disabled', () => {
            expect(cheListHelper.areAllItemsSelected).toBeFalsy();
            expect(cheListHelper.isNoItemSelected).toBeFalsy();
          });

          it('should have correct number of items checked', () => {
            const selectedItemsList = cheListHelper.getSelectedItems();
            expect(selectedItemsList.length).toEqual(checkedItems.length);
          });

        });

      });

      describe('all the selectable items are checked >', () => {

        beforeEach(() => {
          itemsList.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = isItemSelectable(item);
          });
          cheListHelper.updateBulkSelectionStatus();
        });

        it('should have bulk checkbox enabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeTruthy();
          expect(cheListHelper.isNoItemSelected).toBeFalsy();
        });

        it('should have correct number of items checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(selectableItemNumbers.length);
        });

        describe(`items are filtered by name which contains '1' >`, () => {
          let checkedItems;

          let visibleItemName;

          let filter;

          let visibleItems;

          beforeEach(() => {
            // apply filter by name
            visibleItemName = '1';
            filter = mock.createFilterByName(visibleItemName); // not exact match

            cheListHelper.applyFilter('name', ...filter);

            // calculate expected number of visible items
            const re = new RegExp(visibleItemName);
            visibleItems = itemsList.filter((item: any) => {
              return re.test(item.name);
            });
            const visibleItemIds = visibleItems.map((item: any) => {
              return item[idKey];
            });

            // calculate expected number of checked items
            checkedItems = itemsList.filter((item: any) => {
              return visibleItemIds.indexOf(item[idKey]) > -1 && isItemSelectable(item);
            });
          });

          it('should have bulk checkbox enabled', () => {
            expect(cheListHelper.areAllItemsSelected).toBeTruthy();
            expect(cheListHelper.isNoItemSelected).toBeFalsy();
          });

          it('should have correct number of items checked', () => {
            const selectedItemsList = cheListHelper.getSelectedItems();
            expect(selectedItemsList.length).toEqual(checkedItems.length);
          });

        });

      });

    });

    describe('bulk checkbox is enabled >', () => {

      beforeEach(() => {
        cheListHelper.changeBulkSelection();
      });

      it('should return correct number of visible items', () => {
        const visibleItemsList = cheListHelper.getVisibleItems();
        expect(visibleItemsList.length).toEqual(itemsList.length);
      });

      it('should have bulk checkbox enabled', () => {
        expect(cheListHelper.areAllItemsSelected).toBeTruthy();
        expect(cheListHelper.isNoItemSelected).toBeFalsy();
      });

      it('should have correct number of items selected', () => {
        const selectedItemsList = cheListHelper.getSelectedItems();
        expect(selectedItemsList.length).toEqual(selectableItemNumbers.length);
      });

      describe('few of the selectable items are unchecked >', () => {
        let uncheckedItemNumbers;

        let uncheckedItems;

        beforeEach(() => {
          uncheckedItemNumbers = selectableItemNumbers.slice(0, 3);
          uncheckedItems = itemsList.filter((item: any, i: number) => {
            return uncheckedItemNumbers.indexOf(i) === -1;
          });

          uncheckedItems.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = false;
          });
          cheListHelper.updateBulkSelectionStatus();

        });

        it('should have bulk checkbox disabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeFalsy();
          expect(cheListHelper.isNoItemSelected).toBeFalsy();
        });

        it('should have correct number of items checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(itemsList.length - uncheckedItems.length);
        });

      });

      describe('all the selectable items are unchecked >', () => {

        beforeEach(() => {
          itemsList.forEach((item: any) => {
            cheListHelper.itemsSelectionStatus[item[idKey]] = false;
          });
          cheListHelper.updateBulkSelectionStatus();

        });

        it('should have bulk checkbox disabled', () => {
          expect(cheListHelper.areAllItemsSelected).toBeFalsy();
          expect(cheListHelper.isNoItemSelected).toBeTruthy();
        });

        it('should not have any item checked', () => {
          const selectedItemsList = cheListHelper.getSelectedItems();
          expect(selectedItemsList.length).toEqual(0);
        });

      });

    });

  });

});
