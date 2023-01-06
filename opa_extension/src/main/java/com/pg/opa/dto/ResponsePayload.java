package com.pg.opa.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResponsePayload {
    private boolean success;
    private int exitCode;
    private List<String> output = Collections.EMPTY_LIST;
    private String errorMessage = "";

    public ResponsePayload(boolean success, int exitCode, List<String> output, String errorMessage) {
        this.success = success;
        this.exitCode = exitCode;
        if (StringUtils.isNotBlank(errorMessage)) {
            this.errorMessage = errorMessage;
        }
        if (output != null && !output.isEmpty()) {
            this.output = output;
        }

    }
    public ResponsePayload() {

    }
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }



    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

}
