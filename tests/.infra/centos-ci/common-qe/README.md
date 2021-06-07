# How to use this functions

 - Coppy and paste this block of code inside your script. This script utomatically download and import all necessary files
 and all functions from the "common-qe" scripts may be invoked directly by function name without any additional actions.

 ```
#Download and import the "common-qe" functions
export IS_TESTS_FAILED="false"
DOWNLOADER_URL=https://raw.githubusercontent.com/eclipse/che/main/tests/.infra/centos-ci/common-qe/downloader.sh
curl $DOWNLOADER_URL -o downloader.sh
chmod u+x downloader.sh
. ./downloader.sh

```

 - For correct displaying of the tests result add next block of code to the end of your script.
 ```
 if [ "$IS_TESTS_FAILED" == "true" ]; then
  exit 1;
fi
  ```

# How to configure this functions

 - You can configure existing ```"common-qe-configuration.conf"``` which downloading automatically by "downloader.sh". 
 For configure it use next method:
 ```
setConfigProperty "<property>" "value"
```
    Here an example:
```
setConfigProperty "test.suite" "test-all-devfiles"
```

 - Or you can use your own configuration file. For this, export next variable , with path to file:
 ```
export PATH_TO_CONFIGURATION_FILE=<path_to_file>
 ```
    Here an example
```
export PATH_TO_CONFIGURATION_FILE=/full/path/to/conf/file/qe-config.conf

#Download and import the "common-qe" functions
export IS_TESTS_FAILED="false"
DOWNLOADER_URL=https://raw.githubusercontent.com/eclipse/che/main/tests/.infra/centos-ci/common-qe/downloader.sh
curl $DOWNLOADER_URL -o downloader.sh
chmod u+x downloader.sh
. ./downloader.sh
```
