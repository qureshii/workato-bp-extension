package com.pg.opa.dto;

public class RunProcessActionDTO {

    private String run;
    private String resource;
    private String startp;


    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getStartp() {
        return startp;
    }

    public void setStartp(String startp) {
        this.startp = startp;
    }

    @Override
    public String toString() {
        return "RunProcessActionDTO{" +
                "run='" + run + '\'' +
                ", resource='" + resource + '\'' +
                ", startp='" + startp + '\'' +
                '}';
    }
}
