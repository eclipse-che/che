/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

#include <string>
#include <cstdio>
#include <iostream>
#include <sstream>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>


#include "DeveloperStudioProcess.h"
#include "include/cef_browser.h"
#include "include/cef_command_line.h"
#include "include/wrapper/cef_helpers.h"
#include "SystemUtils.h"
#include "Messages.h"

int failure = -1;
int success = 0;


int DeveloperStudioProcess::GetServerPort() {

  std::string sever_port;
  std::string portpath = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::BIN_PORT;
  sever_port = SystemUtils::GetFileContents(portpath.c_str());
  return atoi(sever_port.c_str());
}


void *ExecuteCheServerInBackground(void *args_ptr) {

    std::string server_launch_cmd = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::WSO2STUDIO_CHE_SH_AND;
    int server_startup_status = system(server_launch_cmd.c_str());
        if (server_startup_status == 0) {
            std::cout << Messages::SERVER_STARTED << std::endl;
        } else {
            std::cerr << Messages::SERVER_STARTUP_ERROR << server_startup_status << std::endl;
        }
    return NULL;
}


void *ExecuteCheServerStopInBackground(void *args_ptr) {

    std::string che_launch_cmd = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::WSO2STUDIO_CHE_SH_STOP_AND;


    int server_startup_status = system(che_launch_cmd.c_str());
    if (server_startup_status == 0) {
        std::cout << Messages::SERVER_SHUTDOWN_SUCESSFULL << std::endl;
    } else {
        std::cerr << Messages::SERVER_SHUTDOWN_ERROR << server_startup_status << std::endl;
    }
    return NULL;
}

int DeveloperStudioProcess::StartWorksSpaceSelector() {

    // starting the workspace selector and splash screen
    std::string workspace_selector_cmd = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::WSO2STUDIO_WORKSPACE;
    std::string bash_s = SystemUtils::BIN_BASH;

    //char workspace_selector_cmd_arr[1024];
    //strncpy(workspace_selector_cmd_arr, workspace_selector_cmd.c_str(), sizeof (workspace_selector_cmd_arr));

    char bash[1024];
    strncpy(bash, bash_s.c_str(), sizeof (bash));
    std::string command_s = "-c";

    //char command[1024];
    //strncpy(command, command_s.c_str(), sizeof (command));
    char *name[] = {bash,  command_s.c_str(),  workspace_selector_cmd.c_str(), NULL};

    int pid = fork();
    if (pid == 0) {
        int workspace_selector_status = execvp(bash_s.c_str(), name);
        if (workspace_selector_status == 0) {
            std::cout << Messages::WORKSPACE_SELECTOR_STARTED << std::endl;
        } else {
            std::cerr << Messages::SERVER_STARTUP_ERROR << workspace_selector_status << std::endl;

            return failure;
        }
    }
    return success;
}


int DeveloperStudioProcess::StartProcess() {

  char *actualpath;
  actualpath = realpath("../", NULL);

  if (actualpath != NULL) {

        std::string path(actualpath);
        SystemUtils::APPLICATION_BASE_PATH = path;

        if (DeveloperStudioProcess::StartWorksSpaceSelector() == success) {
            // waiting the sever startup
            std::string urlpath = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::BIN_URL_TXT;
            std::string portpath = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::BIN_PORT;

            //Introduce timeout and exit, or check weatherany othr option to see port is thr
            //use 'good' method in streaming to check file exists
            //wait for the port file, accommodate for the close button in workspace selector
                while (true) {
                    std::FILE *portFile = std::fopen(portpath.c_str(), "rb");
                    if (portFile) {
                        std::fclose(portFile);
                        break;
                    } else {
                        sleep(2); //in seconds
                    }
                }
            int serverPORT = DeveloperStudioProcess::GetServerPort();
            int port_file_remove_status = std::remove(portpath.c_str());
            if (port_file_remove_status != 0) {
                std::cerr << Messages::ERROR_IN_FILE_DELETE << port_file_remove_status << std::endl;
            }

            if (serverPORT > 0 ) {
                pthread_t che_thread;
                int server_args = 0;

                if (pthread_create(&che_thread, NULL, ExecuteCheServerInBackground, (void *)&server_args)) {
                    std::cout << Messages::ERROR_CREATING_SERVER_THREAD << stderr << std::endl;
                }
            //check for the url file
                while (true) {
                    std::FILE *url_file = std::fopen(urlpath.c_str(), "rb");
                    if (url_file) {
                        std::fclose(url_file);
                        break;
                    } else {
                        std::cout << Messages::WAITING_FOR_URL;
                        sleep(2);
                    }
                }
            } else {
               return failure;
            }
        }
   }
   return success;
}

int DeveloperStudioProcess::StopProcess() {
    int server_args = 0;
    pthread_t che_thread;

     if (pthread_create(&che_thread, NULL, ExecuteCheServerStopInBackground, (void *)&server_args)) {
            std::cout << Messages::ERROR_CREATING_SERVER_THREAD << stderr << std::endl;
            return failure;
     }

     return success;
}

std::string DeveloperStudioProcess::GetURLFromFile(){
    std::string urlpath = SystemUtils::APPLICATION_BASE_PATH + SystemUtils::BIN_URL_TXT;
    std::string url = SystemUtils::GetFileContents(urlpath.c_str());

    //delete the url file for this session startup
    int url_file_remove_status = std::remove(urlpath.c_str());
    if (url_file_remove_status != 0) {
         std::cerr << Messages::ERROR_IN_FILE_DELETE << url_file_remove_status << std::endl;
     }
   return url;
}
