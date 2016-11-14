/*
 * Copyright (c) 2015-2016 Codenvy, S.A.
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
 * A helper class to simplify registering Angular components and provide a consistent syntax for doing so.
 * https://raw.githubusercontent.com/michaelbromley/angular-es6/master/src/app/utils/register.js
 * Florent: Add userDashboard module and transform it to a class
 */
export class Register implements che.IRegisterService {
  app: ng.IModule;

  constructor(moduleApp: ng.IModule) {
    this.app = moduleApp;
  }

  directive(name: string, constructorFn: Function): che.IRegisterService {

    constructorFn = this._normalizeConstructor(constructorFn);

    if (!constructorFn.prototype.compile) {
      // create an empty compile function if none was defined.
      constructorFn.prototype.compile = () => {return; };
    }

    let originalCompileFn = this._cloneFunction(constructorFn.prototype.compile);

    // decorate the compile method to automatically return the link method (if it exists)
    // and bind it to the context of the constructor (so `this` works correctly).
    // this gets around the problem of a non-lexical "this" which occurs when the directive class itself
    // returns `this.link` from within the compile function.
    this._override(constructorFn.prototype, 'compile', () => {
      return function () {
        originalCompileFn.apply(this, arguments);

        if (constructorFn.prototype.link) {
          return constructorFn.prototype.link.bind(this);
        }
      };
    });

    let factoryArray = this._createFactoryArray(constructorFn);

    this.app.directive(name, factoryArray);
    return this;
  }


  filter(name: string, constructorFn: Function): che.IRegisterService {
    this.app.filter(name, constructorFn);
    return this;
  }

  controller(name: string, constructorFn: Function): che.IRegisterService {
    this.app.controller(name, constructorFn);
    return this;
  }

  service(name: string, constructorFn: Function): che.IRegisterService {
    this.app.service(name, constructorFn);
    return this;
  }

  provider(name: string, constructorFn: ng.IServiceProvider): che.IRegisterService {
    this.app.provider(name, constructorFn);
    return this;
  }

   factory(name: string, constructorFn: Function): che.IRegisterService {
    constructorFn = this._normalizeConstructor(constructorFn);
    var factoryArray = this._createFactoryArray(constructorFn);
    this.app.factory(name, factoryArray);
    return this;
  }

  /**
   * If the constructorFn is an array of type ['dep1', 'dep2', ..., constructor() {}]
   * we need to pull out the array of dependencies and add it as an $inject property of the
   * actual constructor function.
   * @param input
   * @returns {*}
   * @private
   */
  _normalizeConstructor(input: Function | any): Function {
    let constructorFn: Function;

    if (!input) {
      let stack = (new Error() as any).stack;
      console.log('Invalid constructor', stack);
    }

    if (input.constructor === Array) {
      let injected = input.slice(0, input.length - 1);
      constructorFn = input[input.length - 1];
      constructorFn.$inject = injected;
    } else {
      constructorFn = input;
    }

    return constructorFn;
  }

  /**
   * Convert a constructor function into a factory function which returns a new instance of that
   * constructor, with the correct dependencies automatically injected as arguments.
   *
   * In order to inject the dependencies, they must be attached to the constructor function with the
   * `$inject` property annotation.
   *
   * @param ConstructorFn
   * @returns {Array.<T>}
   * @private
   */
  _createFactoryArray(ConstructorFn: Function | any): Array<any> {
    // get the array of dependencies that are needed by this component (as contained in the `$inject` array)
    let args: Array<any> = ConstructorFn.$inject || [];
    let factoryArray: Array<any> = args.slice(); // create a copy of the array
    // the factoryArray uses Angular's array notation whereby each element of the array is the name of a
    // dependency, and the final item is the factory function itself.
    factoryArray.push((...args: Array<any>) => {
      return  new ConstructorFn(...args);
    });

    return factoryArray;
  }

  /**
   * Clone a function
   * @param original: Function
   * @returns {Function}
   */
  _cloneFunction(original: Function): Function {
    return function() {
      return original.apply(this, arguments);
    };
  }

  /**
   * Override an object's method with a new one specified by `callback`.
   * @param object: Object
   * @param methodName: string
   * @param callback: any
   */
  _override(object: Object, methodName: string, callback: any): void {
    object[methodName] = callback(object[methodName]);
  }

}

// export default Register;
