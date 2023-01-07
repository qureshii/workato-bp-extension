package com.pg.opa.dto;

public class CommandArgument {
    private int index;
    private String arg;


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }
    @Override
    public String toString() {
        return "CommandArgument{" +
                "index=" + index +
                ", arg='" + arg + '\'' +
                '}';
    }
}
