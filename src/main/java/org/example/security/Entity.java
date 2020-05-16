package org.example.security;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Entity<T>
{
    @JsonSerialize(using = IntToStringSerializer2.class)
//    @JSONField(serializeUsing = IntToStringSerializer.class)
    private int code;
    private String msg;
    private T data;

    public Entity()
    {
    }

    public Entity(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    public Entity(int code, String msg, T data)
    {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }
}
