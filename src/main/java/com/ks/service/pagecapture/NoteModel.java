package com.ks.service.pagecapture;

/**
 * Created by Admin on 2017/4/6 0006.
 */
public class NoteModel {
    private String note_ls_id;
    private String note_id;
    private String url;
    private String user_id;
    private String group_id;

    public String getNote_ls_id() {
        return note_ls_id;
    }

    public void setNote_ls_id(String note_ls_id) {
        this.note_ls_id = note_ls_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getNote_id() {
        return note_id;
    }

    public void setNote_id(String note_id) {
        this.note_id = note_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
