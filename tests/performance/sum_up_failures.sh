#!/bin/bash
#
# Copyright (c) 2020-2023 Red Hat, Inc.
# This program and the accompanying materials are made
# available under the terms of the Eclipse Public License 2.0
# which is available at https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# See: https://sipb.mit.edu/doc/safe-shell/

# This script goes through results of load tests and sums up well known issues. Also reports non recognized failures for manual investigation.
# To run this script, set:
#   TIMESTAMP to the name of a folder, where load tests results are stored.
#   filedFolderNames to the array of a users, where test failed. You can easily copy that from the load test sum up.

TIMESTAMP=
failedFolderNames=(  )

staleErrors=0
explorerErrs=0
nosuchwindowErr=0
stopErr=0
removeErr=0
loadErr=0
tokenErr=0
startErr=0
ETIMEDOUTerr=0

sum=0
staleElementUser=""
explorerUser=""
nosuchwindUser=""
stopErrUser=""
removeUser=""
loadErrUser=""
tokenErrUser=""
startErrUser=""
ETIMEDOUTUser=""
notclasified=0

#aditional info
usr409=""
usr503=""
usr504=""

for i in "${failedFolderNames[@]}" 
do
	#################### Additional info gathering #############################################
	er409=$(cat report/$TIMESTAMP/$i/console-log.txt | grep '409')
	if [[ ! -z $er409 ]]; then
		usr409="$usr409 $i"
	fi
	er503=$(cat report/$TIMESTAMP/$i/console-log.txt | grep '503')
	if [[ ! -z $er503 ]]; then
		usr503="$usr503 $i"
	fi
	er504=$(cat report/$TIMESTAMP/$i/console-log.txt | grep '504')
	if [[ ! -z $er504 ]]; then
		usr504="$usr504 $i"
	fi
	############################################################################################

	sum=$((sum+1))
	staleerror=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'StaleElementReferenceError')
	if [[ ! -z $staleerror ]]; then
		staleErrors=$((staleErrors+1))
		staleElementUser="$staleElementUser $i"
		continue;
	fi

	explorer=$(cat report/$TIMESTAMP/$i/console-log.txt | grep "TimeoutError: Waiting for element to be located.*Explorer")
	if [[ ! -z $explorer ]]; then
		explorerErrs=$((explorerErrs+1))
		explorerUser="$explorerUser $i"
		continue;
	fi

	nosuchwindow=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'NoSuchWindowError: no such window')
	if [[ ! -z $nosuchwindow ]]; then
		nosuchwindowErr=$((nosuchwindowErr+1))
		nosuchwindUser="$nosuchwindUser $i"
		continue;
	fi

	loading=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'cannot determine loading status')
	if [[ ! -z $loading ]]; then
		loadErr=$((loadErr+1))
		loadErrUser="$loadErrUser $i"
		continue;
	fi

	starterror=$(cat report/$TIMESTAMP/$i/console-log.txt | grep ') Wait loading workspace and get time')
	if [[ ! -z $starterror ]]; then
		startErr=$((startErr+1))
		startErrUser="$startErrUser $i"
		continue;
	fi

	stoperror=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'Stopping workspace failed')
	if [[ ! -z $stoperror ]]; then
		stopErr=$((stopErr+1))
		stopErrUser="$stopErrUser $i"
		continue;
	fi

	removingerr=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'Removing of workspace failed')
	if [[ ! -z $removingerr ]]; then
		removeErr=$((removeErr+1))
		removeUser="$removeUser $i"
		continue;
	fi

	token=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'Can not get bearer token')
	if [[ ! -z $token ]]; then
		tokenErr=$((tokenErr+1))
		tokenErrUser="$tokenErrUser $i"
		continue;
	fi

	etimed=$(cat report/$TIMESTAMP/$i/console-log.txt | grep 'ETIMEDOUT')
	if [[ ! -z $etimed ]]; then
		ETIMEDOUTerr=$((ETIMEDOUTerr+1))
		ETIMEDOUTUser="$ETIMEDOUTUser $i"
		continue;
	fi
	

	echo " ========================================================= vv $i vv  ================================================================================"
    cat report/$TIMESTAMP/$i/console-log.txt | grep ') "after all"'
	notclasified=$((notclasified+1))

done

echo "StaleElementReferenceError           seen: $staleErrors times"
echo $staleElementUser
echo
echo "Waiting for IDE - Explorer           seen: $explorerErrs times"
echo $explorerUser
echo
echo "NoSuchWindowError: no such window    seen: $nosuchwindowErr times"
echo $nosuchwindUser
echo 
echo "Failed to start workspace            seen: $startErr times"
echo $startErrUser
echo
echo "Can not get bearer token             seen: $tokenErr times"
echo $tokenErrUser
echo
echo "Stopping workspace failed            seen: $stopErr times"
echo $stopErrUser
echo
echo "Removing of workspace failed         seen: $removeErr times"
echo $removeUser
echo
echo "cannot determine loading status      seen: $loadErr times"
echo $loadErrUser
echo
echo "afer suited - ETIMEDOUT              seen: $ETIMEDOUTerr times"
echo $ETIMEDOUTUser
echo
echo "Remaining for manual verifictaion $notclasified/$sum"
echo 
echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "info about error codes found:"
echo "409: $usr409"
echo "503: $usr503"
echo "504: $usr504"
echo "Note: error 409 happens when you try to remove workspace which is not stopped"
