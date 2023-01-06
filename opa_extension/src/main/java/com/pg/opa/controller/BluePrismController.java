package com.pg.opa.controller;

import com.pg.opa.dto.CommandArgument;
import com.pg.opa.dto.ResponsePayload;
import com.pg.opa.dto.RunProcessActionDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/v1")
public class BluePrismController {
    Logger logger = LoggerFactory.getLogger(BluePrismController.class);
    @Value("${scriptpath}")
    private String scriptpath;
    @Value("${scriptname}")
    private String scriptname;
    private boolean isWindows = false;


    @Inject
    Environment environment;

    @PostConstruct
    public void init() {
        logger.info("Initializing BluePrismController .........");
        logger.info("scriptpath {}", scriptpath);
        isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        logger.info("OS type {} ", System.getProperty("os.name"));
        ping();
//        executeAction();
    }

    @GetMapping(path = "/ping")
    public ResponseEntity<ResponsePayload> ping() {
        boolean fileExist = false;
        try {

            String path = scriptpath + File.separator + scriptname;
            File file = new File(path);
            fileExist = file.exists();
            logger.info("Script {} , exist? {} ", path, fileExist);
        } catch (Exception e) {
            logger.error("Error in ping command {} ", e.getMessage());
            return errorResponse(e.getMessage(), BAD_REQUEST);
        }
        return ResponseEntity
                .status(fileExist ? OK : BAD_REQUEST)
                .body(new ResponsePayload(true, 0, null, null));
    }


    @GetMapping(path = "/status/{sessionId}")
    public ResponseEntity<ResponsePayload> getActionStatus(@PathVariable(name = "sessionId", required = true) String sessionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking status for sessionId {} ", sessionId);
        }
        ResponsePayload responsePayload = new ResponsePayload();
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {
            processBuilder.directory(new File(scriptpath));
            if (isWindows) {
                processBuilder.command("cmd.exe", "/c", scriptname,
                        "/status", String.format(" \"%s \"",
                                sessionId, "/sso"));

            } else {
                processBuilder.command(String.format("./%s", scriptname),
                        "/status",
                        String.format(" \"%s \"", sessionId),
                        "/sso");
            }
            Process process = processBuilder.start();
            responsePayload.setSuccess(process.waitFor() == 0);
            responsePayload.setOutput(readOutput(process.getInputStream()));
        } catch (Exception e) {
            logger.error("Error in executing script {} ", e.getMessage());
            logger.error("Error ", e);
            return errorResponse(e.getMessage(), BAD_REQUEST);
        }
        return ResponseEntity
                .ok()
                .body(responsePayload);
    }

    @PostMapping(path = "/run-process")
    public ResponseEntity<ResponsePayload> runProcessActions(@RequestBody RunProcessActionDTO payload) {
        if (logger.isDebugEnabled()) {
            logger.debug("RunProcessAction {}", payload.toString());
        }

        ResponsePayload responsePayload = new ResponsePayload();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(scriptpath));
            List<String> initialCommand = new ArrayList<>();
            if (isWindows) {
                initialCommand.add("cmd.exe");
                initialCommand.add("/c");
                initialCommand.add(scriptname);
            } else {
                initialCommand.add(String.format("./%s", scriptname));
            }
            if (isWindows) {
                processBuilder.command("cmd.exe", "/c", scriptname,
                        "/run", String.format("\"%s\"", payload.getRun()),
                        "/resource", String.format("\"%s\"", payload.getResource()),
                        "/startp", String.format("\"%s\"", payload.getStartp()),
                        "/sso");
            } else {
                processBuilder.command(String.format("./%s", scriptname),
                        "/run", String.format("\"%s\"", payload.getRun()),
                        "/resource", String.format("\"%s\"", payload.getResource()),
                        "/startp", String.format("\"%s\"", payload.getStartp()),
                        "/sso");
            }
            Process process = processBuilder.start();
            responsePayload.setSuccess(process.waitFor() == 0);
            responsePayload.setOutput(readOutput(process.getInputStream()));

        } catch (Exception e) {
            logger.error("Error in executing script {} ", e.getMessage());
            logger.error("Error ", e);
            return errorResponse(e.getMessage(), BAD_REQUEST);

        }
        return ResponseEntity
                .ok()
                .body(responsePayload);
    }

    @PostMapping(path = "/generic-actions")
    public ResponseEntity<ResponsePayload> genericAction(@RequestBody List<CommandArgument> payload) {
        if (logger.isDebugEnabled()) {
            logger.debug("RunProcessAction {}", payload.toString());
        }

        List<String> sortedCommands = sortCommandArguments(payload);
        ResponsePayload responsePayload = new ResponsePayload();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.directory(new File(scriptpath));
            List<String> initialCommand = new ArrayList<>();
            if (isWindows) {
                initialCommand.add("cmd.exe");
                initialCommand.add("/c");
                initialCommand.add(scriptname);
            } else {
                initialCommand.add(String.format("./%s", scriptname));
            }
            processBuilder.command(Stream.concat(initialCommand.stream(), sortedCommands.stream()).collect(Collectors.toList()));
            Process process = processBuilder.start();
            responsePayload.setSuccess(process.waitFor() == 0);
            responsePayload.setOutput(readOutput(process.getInputStream()));

        } catch (Exception e) {
            logger.error("Error in executing script {} ", e.getMessage());
            logger.error("Error ", e);
            return errorResponse(e.getMessage(), BAD_REQUEST);

        }
        return ResponseEntity
                .ok()
                .body(responsePayload);
    }

    private List<String> sortCommandArguments(List<CommandArgument> arguments) {
        int count = 0;
        List<String> sortedCommands = arguments.stream()
                .sorted((o1, o2) -> o1.getIndex())
                .map((item) -> {
                    return StringUtils.containsWhitespace(item.getArg()) ? String.format("\"%s\"", item.getArg()) : item.getArg();
                })
                .collect(Collectors.toList());

        if (logger.isDebugEnabled()) {
            logger.debug("String command {} ", String.join(" ", sortedCommands));
        }
        return sortedCommands;


    }

    private ResponseEntity<ResponsePayload> errorResponse(String errorMessage, HttpStatus status) {
        ResponsePayload errorResponse = new ResponsePayload(false, -1, null, errorMessage);
        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    private List<String> readOutput(InputStream stream) throws IOException {
        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Line {}", line);
            }
            output.add(line);
        }
        return output;

    }

}
