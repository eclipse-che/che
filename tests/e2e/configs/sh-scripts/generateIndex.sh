#!/usr/bin/env bash

echo "Generating index.ts file..."
echo "import * as inversifyConfig from './configs/inversify.config';
export { inversifyConfig };
export * from './configs/inversify.types';
export * from './configs/mocharc';" > ./index.ts

listOfDirs="driver utils pageobjects tests-library constants"
listOfExcludes="./utils/CheReporter.ts"
for dir in $listOfDirs
do
  files=$(find ./$dir -type f | sort)
  for file in $files
  do  
    case $file in *ts)
      for excludedFile in $listOfExcludes
      do
        if [ $excludedFile == $file ]; then
          continue
        else
          file_without_ending=${file::-3}
          echo "export * from '$file_without_ending';" >> ./index.ts
        fi
      done
      ;;
      *)
      echo "Excluding file $file - not a typescript file"
      ;;
    esac
  done
done
