package com.atlassian.webhooks.spi.provider;

import com.atlassian.annotations.PublicSpi;

/**
 * UI item which is rendered in webhook administration page.
 */
@PublicSpi
public interface WebHookUIItem
{
    /**
     * Section of the UI item.
     */
    String section();

    /**
     * Order of this UI item within the section. UI items with lower weight are
     * rendered first.
     */
    Integer weight();

    /**
     * @return html of the section.
     */
    String getHtml();
}
