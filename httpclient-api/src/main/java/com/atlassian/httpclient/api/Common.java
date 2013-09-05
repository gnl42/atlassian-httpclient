package com.atlassian.httpclient.api;

interface Common<B extends Common<B>>
{
    B setEntity(Entity entity);

    /**
     * Copies the specified map of HTTP headers into this object. It will also
     * parse any included Content-Type header into its constituent parts of IANA
     * media type and content charset, updating those properties as appropriate.
     *
     * @param headers A map of HTTP headers
     * @return This object, for builder-style chaining
     */
    B setHeaders(Headers headers);
}
