package com.delvepartners.scheduler;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 4/6/13
 * Time: 3:07 PM
 */
public class TalendJobInfo implements Serializable {
    private final Class<?> jobClass;
    private final String[] arguments;

    TalendJobInfo(Class<?> jobClass, String[] arguments) {
        this.arguments = (arguments == null ? new String[0] : arguments);
        this.jobClass = jobClass;
    }

    public Class<?> getJobClass() {
        return jobClass;
    }

    public String[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TalendJobInfo: {");
        sb.append("jobClass=").append(jobClass);
        sb.append(", arguments=").append(arguments == null ? "null" : Arrays.asList(arguments).toString());
        sb.append('}');
        return sb.toString();
    }
}
