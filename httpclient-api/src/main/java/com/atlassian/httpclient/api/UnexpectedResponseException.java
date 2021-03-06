package com.atlassian.httpclient.api;

/**
 * Thrown to indicate that a response completed normally but that produced an
 * unexpected status code.
 */
public class UnexpectedResponseException extends RuntimeException {
    private Response response;

    /**
     * Creates a new exception for the given response.
     *
     * @param response The unexpected response
     */
    public UnexpectedResponseException(Response response) {
        this.response = response;
    }

    /**
     * Returns the unexpected response
     *
     * @return The response
     */
    public Response getResponse() {
        return response;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Unexpected response '").append(response.getStatusCode()).append("' with message '");
        sb.append(response.getStatusText()).append("'");
        return sb.toString();
    }
}
