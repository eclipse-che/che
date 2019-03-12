This module contains tests based on using "Cypress" framework.


### Prerequisites
Launching requires valid ```"baseUrl"``` which may be configured by next way:
 - by **"cypress.json"** - open ```"cypress.json"``` file placed in the ```"cypress-tests"``` folder,
   change the "baseUrl" value to the valid URL.

 - by **CLI** - perform ```"export CYPRESS_baseUrl=<your_url>"``` in the terminal


### Launching

- Launch testrunner with dashboard:
  - go to the ```"cypress-tests"``` folder and perform ```"npm run cypress:open"``` in the terminal

- Launch tests by "CLI" in the hidden mode:
  - go to the ```"cypress-tests"``` folder and perform ```"npm run cypress:run-hidden <test-name>"``` (particular test)
    or ```"npm run cypress:run-hidden"``` (all tests) in the terminal

- Launch tests by "CLI" in the browser mode:
  - go to the ```"cypress-tests"``` folder and perform ```"npm run cypress:run <test-name>"``` (particular test)
    or ```"npm run cypress:run"``` (all tests) in the terminal
