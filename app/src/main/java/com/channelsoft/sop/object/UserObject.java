package com.channelsoft.sop.object;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.channelsoft.sop.BR;

import java.io.Serializable;

public class UserObject extends BaseObservable implements Serializable {
    private String userId, username, email = "", password = "";

    public UserObject() {
    }

    public UserObject(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    @Bindable
    public String getUserId() {
        return userId;
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    @Bindable
    public String getPassword() {
        return password;
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    public void setPassword(String password) {
        this.password = password;
        notifyPropertyChanged(BR.password);
    }

}
