package com.atlassian.webhooks.plugin.web.util;

import com.atlassian.velocity.htmlsafe.HtmlSafe;

/**
 * Used to include strings that must not be escaped into templates.
 *
 */
public interface HtmlSafeContent
{
    @HtmlSafe
    CharSequence get();
}
