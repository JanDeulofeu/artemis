package com.amelco.artemis.model;

import java.io.Serializable;
import java.util.Date;

public class MessageDto implements Serializable{

    private String content;
    private Date time;

    public MessageDto(final String content, final Date time) {
        this.content = content;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public Date getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "MessageDto{" +
                "content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
