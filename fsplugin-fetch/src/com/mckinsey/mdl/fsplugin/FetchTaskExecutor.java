/*************************GO-LICENSE-START*********************************
* Copyright 2014 ThoughtWorks, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*************************GO-LICENSE-END***********************************/

package com.mckinsey.mdl.fsplugin;

import com.thoughtworks.go.plugin.api.task.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FetchTaskExecutor {
	
	String TARGET_FILE = "ETL-0.1.zip";

    public Result execute(Config config, Context context, JobConsoleLogger console) {
        try {
            return runCommand(context, config, console);
        } catch (Exception e) {
            return new Result(false, "Failed to copy file: " + config.getFilePath(), e);
        }
    }

    private Result runCommand(Context taskContext, Config taskConfig, JobConsoleLogger console) throws IOException, InterruptedException {
        ProcessBuilder curl = createCurlCommandWithOptions(taskContext, taskConfig);
        console.printLine("Launching command: " + curl.command());
        curl.environment().putAll(taskContext.getEnvironmentVariables());
        console.printEnvironment(curl.environment());

        Process curlProcess = curl.start();
        console.readErrorOf(curlProcess.getErrorStream());
        console.readOutputOf(curlProcess.getInputStream());

        int exitCode = curlProcess.waitFor();
        curlProcess.destroy();

        if (exitCode != 0) {
            return new Result(false, "Failed copying file. Please check the output");
        }

        return new Result(true, "Copied file: " + TARGET_FILE);
    }

    ProcessBuilder createCurlCommandWithOptions(Context taskContext, Config taskConfig) {
        String destinationFilePath = taskContext.getWorkingDir() + "\\" + TARGET_FILE;

        List<String> command = new ArrayList<String>();
        command.add("cmd.exe");
        command.add("/c");
        command.add("copy");
        command.add(taskConfig.getFilePath());
        command.add(destinationFilePath);

        return new ProcessBuilder(command);
    }
}