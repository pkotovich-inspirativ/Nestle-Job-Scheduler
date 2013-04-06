package com.delvepartners.scheduler;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 4/6/13
 * Time: 1:52 PM
 */
public class Util {
    public static final String MQ_URL_ENVVAR="CLOUDAMQP_URL";
    public static final String DEFAULT_ENCODING="UTF-8";
    public static final String JOB_QUEUE_NAME="work-queue-1";


    public static String getEnvOrThrow(String name) {
        String val = System.getenv(name);
        if(null == val || val.isEmpty()) {
            throw new IllegalStateException("env var not found for key: "+name);
        }
        return val;
    }
}
