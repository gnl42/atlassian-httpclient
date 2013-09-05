package com.atlassian.httpclient.apache.httpcomponents;

import com.atlassian.httpclient.api.Entity;
import com.atlassian.httpclient.api.Entity.Builder;

import java.io.InputStream;

class EntityBuilder implements Entity.Builder
{

    static Builder builder()
    {
        return new EntityBuilder();
    }

    private EntityBuilder() {}

    @Override
    public Entity build()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder setStream(InputStream entityStream)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder setStream(InputStream entityStream, String charset)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder setString(String entity)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public Builder setMaxEntitySize(long maxEntitySize)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
