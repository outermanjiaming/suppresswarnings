package com.suppresswarnings.corpus.service.shop;

import java.util.Arrays;

public class Reply {
    int state;
    String msg;
    Object data;
    public Reply(){}
    public Reply(int state, String msg) {
        this.state = state;
        this.msg   = msg;
    }
    static Reply FAIL = new Reply(-1, "fail");
    static Reply SUCCESS = new Reply(200, "success");
    public static Reply fail(){
        return FAIL;
    }
    public static Reply fail(String reason){
        return new Reply(-1, reason);
    }
    public static Reply success(){
        return SUCCESS;
    }
    public static Reply success(Object data){
        Reply reply = new Reply(200, "success");
        reply.setData(data);
        return reply;
    }
    public static Reply of(int state, String msg, Object data){
        Reply reply = new Reply(state, msg);
        reply.setData(data);
        return reply;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Reply{" +
                "state=" + state +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
    
    public static void main(String[] args) {
    	String s = "s$b$p";
    	String[] ss = s.split("\\$");
    	System.out.println(Arrays.toString(ss));
    }
}
