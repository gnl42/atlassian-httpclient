package com.atlassian.httpclient.api;

/**
 * Represents the result of an HTTP request.
 */
public interface Response extends Message
{
    /**
     * Gets the status code of the response.
     *
     * @return The status code
     */
    int statusCode();

    /**
     * Gets the status text of the response.
     *
     * @return The status text
     */
    String statusText();

    /**
     * Indicates whether or not this response's status code is categorized as "Informational" (1xx).
     *
     * @return True if status code gte 100 and lt 200
     */
    boolean isInformational();

    /**
     * Indicates whether or not this response's status code is categorized as "Successful" (2xx).
     *
     * @return True if status code gte 200 and lt 300
     */
    boolean isSuccessful();

    /**
     * Indicates whether or not this response's status code is "OK".
     *
     * @return True if status code is 200
     */
    boolean isOk();

    /**
     * Indicates whether or not this response's status code is "Created".
     *
     * @return True if status code is 201
     */
    boolean isCreated();

    /**
     * Indicates whether or not this response's status code is "No Content".
     *
     * @return True if status code is 204
     */
    boolean isNoContent();

    /**
     * Indicates whether or not this response's status code is categorized as "Redirection" (3xx).
     *
     * @return True if status code gte 300 and lt 400
     */
    boolean isRedirection();

    /**
     * Indicates whether or not this response's status code is "See Other".
     *
     * @return True if status code is 303
     */
    boolean isSeeOther();

    /**
     * Indicates whether or not this response's status code is "Not Modified".
     *
     * @return True if status code is 304
     */
    boolean isNotModified();

    /**
     * Indicates whether or not this response's status code is categorized as "Client Error" (4xx).
     *
     * @return True if status code gte 400 and lt 500
     */
    boolean isClientError();

    /**
     * Indicates whether or not this response's status code is "Bad Request".
     *
     * @return True if status code is 400
     */
    boolean isBadRequest();

    /**
     * Indicates whether or not this response's status code is "Unauthorized".
     *
     * @return True if status code is 401
     */
    boolean isUnauthorized();

    /**
     * Indicates whether or not this response's status code is "Forbidden".
     *
     * @return True if status code is 403
     */
    boolean isForbidden();

    /**
     * Indicates whether or not this response's status code is "Not Found".
     *
     * @return True if status code is 404
     */
    boolean isNotFound();

    /**
     * Indicates whether or not this response's status code is "Conflict".
     *
     * @return True if status code is 409
     */
    boolean isConflict();

    /**
     * Indicates whether or not this response's status code is categorized as "Server Error" (5xx).
     *
     * @return True if status code gte 500 and lt 600
     */
    boolean isServerError();

    /**
     * Indicates whether or not this response's status code is "Internal Server Error".
     *
     * @return True if status code is 500
     */
    boolean isInternalServerError();

    /**
     * Indicates whether or not this response's status code is "Service Unavailable".
     *
     * @return True if status code is 503
     */
    boolean isServiceUnavailable();

    /**
     * Indicates whether or not this response's status code is categorized as either "Client Error" or "Server Error".
     *
     * @return True if either of isClientError() or isServerError() is true
     */
    boolean isError();

    /**
     * Indicates whether or not this response's status code is categorized as one of "Informational", "Redirection",
     * "Client Error" or "Server Error".
     *
     * @return True if one of isInformational(), isRedirection() or isError() is true
     */
    boolean isNotSuccessful();

    interface Builder extends Common<Builder>, Buildable<Response>
    {
        /**
         * Sets the status code of the response.
         */
        Builder setStatusCode(int statusCode);

        /**
         * Sets the status text of the response.
         */
        Builder setStatusText(String statusText);
    }
}
