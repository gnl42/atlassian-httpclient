package com.atlassian.httpclient.api;

/**
 * RequestContext represents execution state of an HTTP process. It is a structure
 * that can be used to map an attribute name to an attribute value.
 */
public interface RequestContext {

    /**
     * Obtains attribute with the given name.
     *
     * @param id the attribute name.
     * @return attribute value, or {@code null} if not set.
     */
    Object getAttribute(String id);

    /**
     * Obtains the cookie store of the request context.
     *
     * @return the cookie store, or {@code null} if not set.
     */
    CookieStore getCookieStore();

    /**
     * Sets value of the attribute with the given name.
     *
     * @param id the attribute name.
     * @param obj the attribute value.
     */
    void setAttribute(String id, Object obj);

    /**
     * Sets the cookie store for the request context.
     *
     * @param cookieStore the cookie store, or {@code null} to restore the default.
     */
    void setCookieStore(CookieStore cookieStore);

    /**
     * Removes attribute with the given name from the context.
     *
     * @param id the attribute name.
     * @return attribute value, or {@code null} if not set.
     */
    Object removeAttribute(String id);

}
