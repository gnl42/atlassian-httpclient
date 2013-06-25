package com.atlassian.webhooks.spi.provider;

import java.io.IOException;

public interface WebHookUIItem
{
    String section();

    Integer weight();

    String getHtml() throws IOException;
}
