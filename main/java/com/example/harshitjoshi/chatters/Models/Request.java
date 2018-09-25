package com.example.harshitjoshi.chatters.Models;
public class Request {

    String request_type;
    public  String UserID;

    public Request(){

    }
    public Request(String requesttype) {
        request_type = requesttype;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public Request WithId(String UserID){
        this.UserID = UserID;
        return this;
    }
}