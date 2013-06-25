package com.atlassian.webhooks.plugin.web;

import com.atlassian.osgi.tracker.WaitableServiceTrackerCustomizer;
import com.atlassian.osgi.tracker.WaitableServiceTrackerFactory;
import com.atlassian.velocity.htmlsafe.HtmlSafe;
import com.atlassian.webhooks.spi.provider.WebHookUIItem;
import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import static com.google.common.base.Preconditions.checkNotNull;

public class WebHookUIRegistry
{
    private final Multimap<String, WebHookUIItem> uiBySections = ArrayListMultimap.create();

    public WebHookUIRegistry(WaitableServiceTrackerFactory factory)
    {
        checkNotNull(factory).create(WebHookUIItem.class, new WebHookUIWaitableServiceTrackerCustomizer());
    }

    @HtmlSafe
    public Iterable<String> getSections()
    {
        return uiBySections.keys();
    }

    @HtmlSafe
    public Iterable<WebHookUIItem> getItems(final String section)
    {
        return Ordering.natural().reverse().onResultOf(new Function<WebHookUIItem, Comparable>()
        {
            @Override
            public Comparable apply(final WebHookUIItem webHookUIItem)
            {
                return webHookUIItem.weight();
            }
        }).sortedCopy(uiBySections.get(section));
    }

    private final class WebHookUIWaitableServiceTrackerCustomizer implements WaitableServiceTrackerCustomizer<WebHookUIItem>
    {
        @Override
        public WebHookUIItem adding(final WebHookUIItem webHookUIItem)
        {
            uiBySections.put(webHookUIItem.section(), webHookUIItem);
            return webHookUIItem;
        }

        @Override
        public void removed(WebHookUIItem webHookUIItem)
        {
            uiBySections.remove(webHookUIItem.section(), webHookUIItem);
        }
    }
}
