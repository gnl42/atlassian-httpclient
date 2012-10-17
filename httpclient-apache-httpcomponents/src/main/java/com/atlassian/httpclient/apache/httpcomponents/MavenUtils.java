package com.atlassian.httpclient.apache.httpcomponents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class MavenUtils
{
    private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);

    private static final String UNKNOWN_VERSION = "unknown";

    static String getVersion(String groupId, String artifactId)
    {
        final Properties props = new Properties();
        InputStream resourceAsStream = null;
        try
        {
            resourceAsStream = MavenUtils.class.getResourceAsStream(String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));
            props.load(resourceAsStream);
            return props.getProperty("version", UNKNOWN_VERSION);
        }
        catch (Exception e)
        {
            logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId);
            logger.debug("Got the following exception", e);
            return UNKNOWN_VERSION;
        }
        finally
        {
            if (resourceAsStream != null)
            {
                try
                {
                    resourceAsStream.close();
                }
                catch (IOException ioe)
                {
                    // ignore
                }
            }
        }
    }
}
