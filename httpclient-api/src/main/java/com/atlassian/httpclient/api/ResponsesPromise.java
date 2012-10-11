package com.atlassian.httpclient.api;

import java.util.List;

/**
 * A specific type of BaseResponsePromise for handling a batch of response promises,
 * as supplied via a <code>when</code>-based promise aggregation
 */
public interface ResponsesPromise extends BaseResponsePromise<List<Response>>
{
}
