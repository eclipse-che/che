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

type Item = {
  id: string;
  name: string;
}

/**
 * This class creates mock data sets.
 *
 * @author Oleksii Kurinnyi
 */
export class CheListHelperMock {
  private idKey: string = 'id';
  private items: Item[];

  mockData(): void {
    const itemsNumber = 15;

    this.items = Array.from(Array(itemsNumber)).map((x: any, i: number) => {
      return <Item>{
        [this.idKey]: `item-${this.idKey}-${i}`,
        name: `item-name-${i}`
      };
    });

  }

  getIdKey(): string {
    return this.idKey;
  }

  getItemsList(): Item[] {
    return this.items;
  }

  createFilterByName(name: string): any[] {
    return [{name: name}];
  }
}
