package com.atlassian.httpclient.api;

/**
 * HTTP Status code, for reference see:
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/List_of_HTTP_status_codes">the wikipedia page.</a></li>
 * <li>Hypertext Transfer Protocol -- HTTP/1.1, <a href="https://tools.ietf.org/html/rfc2616">RFC 2616</a></li>
 * <li>HTTP Extensions for Web Distributed Authoring and Versioning (WebDAV), <a href="https://tools.ietf.org/html/rfc4918">RFC 4918</a></li>
 * <li>Binding Extensions to Web Distributed Authoring and Versioning (WebDAV), <a href="https://tools.ietf.org/html/rfc5842">RFC 5842</a></li>
 * <li>Delta encoding in HTTP, <a href="https://tools.ietf.org/html/rfc3229">RFC 3229</a></li>
 * </ul>
 */
public enum HttpStatus {
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

    /**
     *
     */
    USE_PROXY(305),

    /**
     * No longer used. Originally meant "Subsequent requests should use the specified proxy."
     */
    SWITCH_PROXY(306),

    /**
     * In this case, the request should be repeated with another URI; however, future requests should still use the
     * original URI. In contrast to how 302 was historically implemented, the request method is not allowed to be
     * changed when reissuing the original request. For instance, a POST request repeated using another POST request.
     */
    TEMPORARY_REDIRECT(307),

    /**
     * The request, and all future requests should be repeated using another URI. 307 and 308 (as proposed) parallel the
     * behaviours of 302 and 301, but do not allow the HTTP method to change. So, for example, submitting a form to a
     * permanently redirected resource may continue smoothly.
     */
    PERMANENT_REDIRECT(308),

    /**
     * The request cannot be fulfilled due to bad syntax.
     */
    BAD_REQUEST(400),

    /**
     * Similar to 403 Forbidden, but specifically for use when authentication is required and has failed or has not yet
     * been provided. The response must include a WWW-Authenticate header field containing a challenge applicable to
     * the requested resource.
     */
    UNAUTHORIZED(401),

    /**
     * Reserved for future use. The original intention was that this code might be used as part of some form of digital
     * cash or micropayment scheme, but that has not happened, and this code is not usually used. As an example of its
     * use, however, Apple's MobileMe service generates a 402 error if the MobileMe account is delinquent.
     */
    PAYMENT_REQUIRED(402),

    /**
     * The request was a valid request, but the server is refusing to respond to it. Unlike a 401 Unauthorized response,
     * authenticating will make no difference. On servers where authentication is required, this commonly means that the
     * provided credentials were successfully authenticated but that the credentials still do not grant the client
     * permission to access the resource (e.g. a recognized user attempting to access restricted content).
     */
    FORBIDDEN(403),

    /**
     * The requested resource could not be found but may be available again in the future. Subsequent requests by the
     * client are permissible.
     */
    NOT_FOUND(404),

    /**
     * A request was made of a resource using a request method not supported by that resource; for example, using GET on
     * a form which requires data to be presented via POST, or using PUT on a read-only resource.
     */
    METHOD_NOT_ALLOWED(405),

    /**
     * The requested resource is only capable of generating content not acceptable according to the Accept headers sent
     * in the request.
     */
    NOT_ACCEPTABLE(406),

    /**
     * The client must first authenticate itself with the proxy.
     */
    PROXY_AUTHENTICATION_REQUIRED(407),

    /**
     * The server timed out waiting for the request. According to W3 HTTP specifications: "The client did not produce a
     * request within the time that the server was prepared to wait. The client MAY repeat the request without
     * modifications at any later time."
     */
    REQUEST_TIMEOUT(408),

    /**
     * Indicates that the request could not be processed because of conflict in the request, such as an edit conflict.
     */
    CONFLICT(409),

    /**
     * Indicates that the resource requested is no longer available and will not be available again. This should be used
     * when a resource has been intentionally removed and the resource should be purged. Upon receiving a 410 status code,
     * the client should not request the resource again in the future. Clients such as search engines should remove the
     * resource from their indices. Most use cases do not require clients and search engines to purge the resource, and
     * a "404 Not Found" may be used instead.
     */
    GONE(410),

    /**
     * The request did not specify the length of its content, which is required by the requested resource.
     */
    LENGTH_REQUIRED(411),

    /**
     * The server does not meet one of the preconditions that the requester put on the request.
     */
    PRECONDITION_FAILED(412),

    /**
     * The request is larger than the server is willing or able to process.
     */
    REQUEST_ENTITY_TOO_LARGE(413),

    /**
     * The URI provided was too long for the server to process.
     */
    REQUEST_URI_TOO_LONG(414),

    /**
     * The request entity has a media type which the server or resource does not support. For example, the client uploads
     * an image as image/svg+xml, but the server requires that images use a different format.
     */
    UNSUPPORTED_MEDIA_TYPE(415),

    /**
     * The client has asked for a portion of the file, but the server cannot supply that portion. For example, if the
     * client asked for a part of the file that lies beyond the end of the file.
     */
    REQUEST_RANGE_NOT_SATISFIABLE(416),

    /**
     * The server cannot meet the requirements of the Expect request-header field.
     */
    EXPECTATION_FAILED(417),

    /**
     * This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee
     * Pot Control Protocol, and is not expected to be implemented by actual HTTP servers.
     */
    I_M_A_TEAPOT(418),

    /**
     * Not part of the HTTP standard, but returned by the Twitter Search and Trends API when the client is being rate
     * limited. Other services may wish to implement the 429 Too Many Requests response code instead.
     */
    ENHANCE_YOUR_CALM(420),

    /**
     * The request was well-formed but was unable to be followed due to semantic errors.
     */
    UNPROCESSABLE_ENTITY(422),

    /**
     * The resource that is being accessed is locked.
     */
    LOCKED(423),

    /**
     * Indicates the method was not executed on a particular resource within its scope because some part of the method's
     * execution failed causing the entire method to be aborted.
     */
    METHOD_FAILURE(424),

    /**
     * Defined in drafts of "WebDAV Advanced Collections Protocol",[15] but not present in "Web Distributed Authoring
     * and Versioning (WebDAV) Ordered Collections Protocol".[16]
     */
    UNORDERED_COLLECTION(425),

    /**
     * The client should switch to a different protocol such as TLS/1.0.
     */
    UPGRADE_REQUIRED(426),

    /**
     * The origin server requires the request to be conditional. Intended to prevent "the 'lost update' problem, where
     * a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has
     * modified the state on the server, leading to a conflict."
     */
    PRECONDITION_REQUIRED(428),

    /**
     * The user has sent too many requests in a given amount of time. Intended for use with rate limiting schemes.
     */
    TOO_MANY_REQUESTS(429),

    /**
     * The server is unwilling to process the request because either an individual header field, or all the header
     * fields collectively, are too large.[18]
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431),

    /**
     * Used in Nginx logs to indicate that the server has returned no information to the client and closed the connection (useful as a deterrent for malware).
     */
    NO_RESPONSE(444),

    /**
     * A Microsoft extension. The request should be retried after performing the appropriate action.
     */
    RETRY_WITH(449),

    /**
     * A Microsoft extension. This error is given when Windows Parental Controls are turned on and are blocking access to the given webpage.
     */
    BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS(450),

    /**
     * Defined in the internet draft "A New HTTP Status Code for Legally-restricted Resources". Intended to be used when
     * resource access is denied for legal reasons, e.g. censorship or government-mandated blocked access. A reference
     * to the 1953 dystopian novel Fahrenheit 451, where books are outlawed.
     */
    UNAVAILABLE_FOR_LEGAL_REASONS(451),
    //    REDIRECT(451),

    /**
     * Nginx internal code similar to 431 but it was introduced earlier.
     */
    REQUEST_HEADER_TOO_LARGE(494),

    /**
     * Nginx internal code used when SSL client certificate error occured to distinguish it from 4XX in a log and an
     * error page redirection.
     */
    CERT_ERROR(495),

    /**
     * Nginx internal code used when client didn't provide certificate to distinguish it from 4XX in a log and an error
     * page redirection.
     */
    NO_CERT(496),

    /**
     * Nginx internal code used for the plain HTTP requests that are sent to HTTPS port to distinguish it from 4XX in a
     * log and an error page redirection.
     */
    HTTP_TO_HTTPS(497),

    /**
     * Used in Nginx logs to indicate when the connection has been closed by client while the server is still processing
     * its request, making server unable to send a status code back.
     */
    CLIENT_CLOSED_REQUEST(499),

    /**
     * A generic error message, given when no more specific message is suitable
     */
    INTERNAL_SERVER_ERROR(500),

    /**
     * The server either does not recognize the request method, or it lacks the ability to fulfill the request.
     */
    NOT_IMPLEMENTED(501),

    /**
     * The server was acting as a gateway or proxy and received an invalid response from the upstream server.
     */
    BAD_GATEWAY(502),

    /**
     * The server is currently unavailable (because it is overloaded or down for maintenance).Generally, this is a
     * temporary state.
     */
    SERVICE_UNAVAILABLE(503),

    /**
     * The server was acting as a gateway or proxy and did not receive a timely response from the upstream server.
     */
    GATEWAY_TIMEOUT(504),

    /**
     * The server does not support the HTTP protocol version used in the request.
     */
    HTTP_VERSION_NOT_SUPPORTED(505),

    /**
     * <p>RFC 2295
     * <p>Transparent content negotiation for the request results in a circular reference.
     */
    VARIANT_ALSO_NEGOTIATES(506),

    /**
     * <p>WebDAV; RFC 4918
     * <p>The server is unable to store the representation needed to complete the request.[4]
     */
    INSUFFICIENT_STORAGE(507),

    /**
     * <p>WebDAV; RFC 5842
     * <p>The server detected an infinite loop while processing the request (sent in lieu of 208).
     */
    LOOP_DETECTED(508),

    /**
     * This status code, while used by many servers, is not specified in any RFCs.
     */
    BANDWIDTH_LIMIT_EXCEEDED(509),

    /**
     * <p>RFC 2774
     * <p>Further extensions to the request are required for the server to fulfill it.
     */
    NOT_EXTENDED(510),

    /**
     * <p>RFC 6585
     * <p>The client needs to authenticate to gain network access. Intended for use by intercepting proxies used to control
     * access to the network (e.g. "captive portals" used to require agreement to Terms of Service before granting full
     * Internet access via a Wi-Fi hotspot).
     */
    NETWORK_AUTHENTICATION_REQUIRED(511),

    /**
     * This status code is not specified in any RFCs, but is used by Microsoft Corp. HTTP proxies to signal a network
     * read timeout behind the proxy to a client in front of the proxy.
     */
    NETWORK_READ_TIMEOUT_ERROR(598),

    /**
     * This status code is not specified in any RFCs, but is used by Microsoft Corp. HTTP proxies to signal a network
     * connect timeout behind the proxy to a client in front of the proxy.
     */
    NETWORK_CONNECT_TIMEOUT_ERROR(599);

    public final int code;

    private HttpStatus(int code) {
        this.code = code;
    }

    static HttpStatus fromCode(int code) {
        for (HttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("No HTTP status for code " + code);
    }

    @Override
    public String toString() {
        return name() + "(" + code + ")";
    }
}
