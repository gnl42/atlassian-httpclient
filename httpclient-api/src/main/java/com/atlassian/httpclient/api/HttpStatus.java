package com.atlassian.httpclient.api;

/**
 * See:
 * <ul>
 *     <li><a href="http://en.wikipedia.org/wiki/List_of_HTTP_status_codes">the wikipedia page.</a></li>
 *     <li>Hypertext Transfer Protocol -- HTTP/1.1, <a href="https://tools.ietf.org/html/rfc2616">RFC 2616</a></li>
 *     <li>HTTP Extensions for Web Distributed Authoring and Versioning (WebDAV), <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a></li>
 *     <li>Binding Extensions to Web Distributed Authoring and Versioning (WebDAV), <a href="https://tools.ietf.org/html/rfc5842">RFC 5842</a></li>
 *     <li>Delta encoding in HTTP, <a href="https://tools.ietf.org/html/rfc3229">RFC 3229</a></li>
 * </ul>
 */
enum HttpStatus
{
    /**
     * <p>This means that the server has received the request headers, and that the client should proceed to send the
     * request body (in the case of a request for which a body needs to be sent; for example, a POST request).
     * <p>If the request body is large, sending it to a server when a request has already been rejected based upon
     * inappropriate headers is inefficient. To have a server check if the request could be accepted based on the
     * request's headers alone, a client must send Expect: 100-continue as a header in its initial request and check if
     * a 100 Continue status code is received in response before continuing (or receive 417 Expectation Failed and not
     * continue).
     */
    CONTINUE(100),

    /**
     * This means the requester has asked the server to switch protocols and the server is acknowledging that it will
     * do so.
     */
    SWITCHING_PROTOCOLS(101),

    /**
     * <p>WebDAV: RFC 2518
     * <p>As a WebDAV request may contain many sub-requests involving file operations, it may take a long time to complete
     * the request. This code indicates that the server has received and is processing the request, but no response is
     * available yet. This prevents the client from timing out and assuming the request was lost.
     */
    PROCESSING(102),

    /**
     * Standard response for successful HTTP requests. The actual response will depend on the request method used. In a
     * GET request, the response will contain an entity corresponding to the requested resource. In a POST request the
     * response will contain an entity describing or containing the result of the action.
     */
    OK(200),

    /**
     * The request has been fulfilled and resulted in a new resource being created.
     */
    CREATED(201),

    /**
     * The request has been accepted for processing, but the processing has not been completed. The request might or
     * might not eventually be acted upon, as it might be disallowed when processing actually takes place.
     */
    ACCEPTED(202),

    /**
     * The server successfully processed the request, but is returning information that may be from another source.
     */
    NON_AUTHORITATIVE_INFORMATION(203),

    /**
     * The server successfully processed the request, but is not returning any content.
     */
    NO_CONTENT(204),

    /**
     * The server successfully processed the request, but is not returning any content. Unlike a 204 response, this
     * response requires that the requester reset the document view.
     */
    RESET_CONTENT(205),

    /**
     * The server is delivering only part of the resource due to a range header sent by the client. The range header is
     * used by tools like wget to enable resuming of interrupted downloads, or split a download into multiple
     * simultaneous streams.
     */
    PARTIAL_CONTENT(206),

    /**
     * <p>WebDAV; RFC 4918
     * <p>The message body that follows is an XML message and can contain a number of separate response codes, depending on
     * how many sub-requests were made.
     */
    MULTI_STATUS(207),

    /**
     * <p>WebDAV; RFC 5842
     * <p>The members of a DAV binding have already been enumerated in a previous reply to this request, and are not
     * being included again.
     */
    ALREADY_REPORTED(208),

    /**
     * <p>RFC 3229
     * <p>The server has fulfilled a GET request for the resource, and the response is a representation of the result of
     * one or more instance-manipulations applied to the current instance.
     */
    IM_USED(226),

    /**
     * Indicates multiple options for the resource that the client may follow. It, for instance, could be used to
     * present different format options for video, list files with different extensions, or word sense disambiguation.
     */
    MULTIPLE_CHOICES(300),

    /**
     * This and all future requests should be directed to the given URI.
     */
    MOVED_PERMANENTLY(301),

    /**
     * This is an example of industry practice contradicting the standard. The HTTP/1.0 specification (RFC 1945)
     * required the client to perform a temporary redirect (the original describing phrase was "Moved Temporarily"),
     * but popular browsers implemented 302 with the functionality of a 303 See Other. Therefore, HTTP/1.1 added status
     * codes 303 and 307 to distinguish between the two behaviours. However, some Web applications and frameworks use
     * the 302 status code as if it were the 303.
     */
    FOUND(302),

    /**
     * The response to the request can be found under another URI using a GET method. When received in response to a
     * POST (or PUT/DELETE), it should be assumed that the server has received the data and the redirect should be
     * issued with a separate GET message.
     */
    SEE_OTHER(303),

    /**
     * Indicates the resource has not been modified since last requested. Typically, the HTTP client provides a header
     * like the If-Modified-Since header to provide a time against which to compare. Using this saves bandwidth and
     * reprocessing on both the server and client, as only the header data must be sent and received in comparison to
     * the entirety of the page being re-processed by the server, then sent again using more bandwidth of the server
     * and client.
     */
    NOT_MODIFIED(304),


    USE_PROXY(305),
    SWITCH_PROXY(306),
    TEMPORARY_REDIRECT(307),
    PERMANENT_REDIRECT(308),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    PAYMENT_REQUIRED(402),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_URI_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    REQUEST_RANGE_NOT_SATISFIABLE(416),
    EXPECTATION_FAILED(417),
    I_M_A_TEAPOT(418),
    ENHANCE_YOUR_CALM(420),
    UNPROCESSABLE_ENTITY(422),
    LOCKED(423),
    FAILED_DEPENDENCY(424),
    UNORDERED_COLLECTION(425),
    UPGRADE_REQUIRED(426),
    PRECONDITION_REQUIRED(428),
    TOO_MANY_REQUESTS(429),
    REQUEST_HEADER_FIELDS_TOO_LARGE(431),
    /**
     * Used in Nginx logs to indicate that the server has returned no information to the client and closed the connection (useful as a deterrent for malware).
     */
    NO_RESPONSE(444),
    RETRY_WITH(449),
    BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS(450),
    UNAVAILABLE_FOR_LEGAL_REASONS(451),
    //    REDIRECT(451),
    REQUEST_HEADER_TOO_LARGE(494),
    CERT_ERROR(495),
    NO_CERT(496),
    HTTP_TO_HTTPS(497),
    CLIENT_CLOSED_REQUEST(499),
    INTERNAL_SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504),
    HTTP_VERSION_NOT_SUPPORTED(505),
    VARIANT_ALSO_NEGOTIATES(506),
    INSUFFICIENT_STORAGE(507),
    LOOP_DETECTED(508),
    BANDWIDTH_LIMIT_EXCEEDED(509),
    NOT_EXTENDED(510),
    NETWORK_AUTHENTICATION_REQUIRED(511),
    NETWORK_READ_TIMEOUT_ERROR(598),
    NETWORK_CONNECT_TIMEOUT_ERROR(599);


    public final int code;

    private HttpStatus(int code)
    {
        this.code = code;
    }

    static HttpStatus fromCode(int code)
    {
        for (HttpStatus status : values())
        {
            if (status.code == code)
            {
                return status;
            }
        }
        throw new IllegalArgumentException("No HTTP status for code " + code);
    }

    @Override
    public String toString()
    {
        return name() + "(" + code + ")";
    }
}
