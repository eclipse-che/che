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
#include "Messages.h"

std::string	Messages::SERVER_STARTED  = "Server started successfully.";
std::string	Messages::SERVER_STARTUP_ERROR  = "Error during the server startup: please refer to log for more details.\n StatusCode: ";
std::string	Messages::ERROR_CREATING_SERVER_THREAD  = "Error creating server thread.\n";
std::string	Messages::WORKSPACE_SELECTOR_STARTED  = "Workspace selector started successfully";
std::string	Messages::ERROR_IN_FILE_DELETE = "Error during file deletion: StatusCode: ";
std::string	Messages::WAITING_FOR_URL = " Waiting for URL |";

std::string	Messages::ERROR_GETTING_DEFAULT_DISPLAY = "Failed to open default display.\n";
std::string	Messages::ERROR_GETTING_DEFAULT_SCREEN = "Failed to obtain the default screen of given display.\n";

std::string	Messages::SERVER_SHUTDOWN_SUCESSFULL = "Server shutting down is successful. \n";
std::string	Messages::SERVER_SHUTDOWN_ERROR = "Error during the server shutdown: please refer to log for more details.\n StatusCode: ";


