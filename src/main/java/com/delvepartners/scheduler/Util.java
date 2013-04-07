package com.delvepartners.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: ebridges
 * Date: 4/6/13
 * Time: 1:52 PM
 */
public class Util {
    private static Logger LOG = LoggerFactory.getLogger(Util.class);

    public static final String MQ_URL_ENVVAR="CLOUDAMQP_URL";
    public static final String JOB_QUEUE_NAME="work-queue-1";
    public static final Map<String, Object> QUEUE_CONFIG = new HashMap<String, Object>();

    static {
        QUEUE_CONFIG.put("x-ha-policy", "all");
    }

    public static Class<?> getTalendJobClass(String projectName, String jobName, String version) {
        String versionName = version.replace('.','_');
        String className = String.format("%s.%s_%s.%s",
                projectName.toLowerCase(),
                jobName.toLowerCase(),
                versionName,
                jobName);

        if(LOG.isInfoEnabled()) {
            LOG.info(String.format("Talend job info: %s/%s/%s::%s", projectName, jobName, version, className));
        }

        try {
            return Class.forName( className );
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("unable to locate %s on classpath", className), e);
        }
    }

    public static String getEnvOrThrow(String name) {
        String val = System.getenv(name);
        if(null == val || val.isEmpty()) {
            throw new IllegalStateException("env var not found for key: "+name);
        }
        return val;
    }

    public static String[] asArray(String s) {
        if(null == s || s.isEmpty()) {
            return new String[0];
        } else {
            return s.split(",");
        }
    }
}
