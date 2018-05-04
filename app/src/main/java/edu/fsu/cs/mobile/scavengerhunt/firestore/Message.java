package edu.fsu.cs.mobile.scavengerhunt.firestore;

/*
 * https://github.com/MahmoudAlyuDeen/FirebaseIM/blob/3fafaa07495e161840afc7c547c3d1b83538e770/app/src/main/java/afterapps/com/firebaseim/beans/Message.java
 */
public class Message {

    private long timestamp;
    private String body;
    private String from;
    private String to;

    public Message(long timestamp, String body, String from, String to) {
        this.timestamp = timestamp;
        this.body = body;
        this.from = from;
        this.to = to;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public void setBody(String body) {
        this.body = body;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Message() {
    }

    public long getTimestamp() {
        return timestamp;
    }


    public String getTo() {
        return to;
    }


    public String getFrom() {
        return from;
    }

    public String getBody() {
        return body;
    }
}