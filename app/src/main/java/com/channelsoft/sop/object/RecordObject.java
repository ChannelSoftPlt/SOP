package com.channelsoft.sop.object;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.channelsoft.sop.BR;

import java.io.Serializable;
import java.util.Calendar;

public class RecordObject extends BaseObservable implements Serializable {
    private String date, age = "", gender = "", temperature = "", prefix = "", phone = "", ic = "", name = "", id, email = "", branch_id, branch;

    public RecordObject(String date, String age, String gender, String temperature, String prefix, String phone, String ic, String name, String id, String email, String branch_id, String branch) {
        this.date = date;
        this.age = age;
        this.gender = gender;
        this.temperature = temperature;
        this.prefix = prefix;
        this.phone = phone;
        this.ic = ic;
        this.name = name;
        this.id = id;
        this.email = email;
        this.branch_id = branch_id;
        this.branch = branch;
    }

    public RecordObject(String date, String temperature, String phone, String ic, String name, String id) {
        this.date = date;
        this.temperature = temperature;
        this.phone = phone;
        this.ic = ic;
        this.name = name;
        this.id = id;
    }

    public RecordObject(String age, String gender, String temperature, String prefix, String phone, String ic, String name) {
        this.age = age;
        this.gender = gender;
        this.temperature = temperature;
        this.prefix = prefix;
        this.phone = phone;
        this.ic = ic;
        this.name = name;
    }

    public RecordObject() {
    }

    @Bindable
    public String getDate() {
        return date;
    }

    @Bindable
    public String getAge() {
        return age;
    }

    @Bindable
    public String getGender() {
        return gender;
    }

    @Bindable
    public String getTemperature() {
        return temperature;
    }

    @Bindable
    public String getPhone() {
        return phone;
    }

    @Bindable
    public String getIc() {
        return ic;
    }

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public String getId() {
        return id;
    }

    @Bindable
    public String getPrefix() {
        return prefix;
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    @Bindable
    public String getBranch_id() {
        return branch_id;
    }

    @Bindable
    public String getBranch() {
        return branch;
    }

    public void setDate(String date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    public void setAge(String age) {
        this.age = age;
        notifyPropertyChanged(BR.age);
    }

    public void setGender(String gender) {
        this.gender = gender;
        notifyPropertyChanged(BR.gender);
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
        notifyPropertyChanged(BR.temperature);
    }

    public void setPhone(String phone) {
        this.phone = phone;
        notifyPropertyChanged(BR.phone);
    }

    public void setIc(String ic) {
        this.ic = ic;
        getGenderFromIC();
        getAgeFromIC();
        notifyPropertyChanged(BR.ic);
    }

    private void getGenderFromIC() {
        if (this.ic.length() == 12) {
            int gender = Integer.parseInt(this.ic.substring(10, 12));
            if (gender % 2 == 0)
                setGender("Female");
            else
                setGender("Male");
        } else {
            setGender("");
        }
    }

    private void getAgeFromIC() {
        try {
            if (this.ic.length() == 12) {
                int year = Integer.parseInt(this.ic.substring(0, 2));
                int month = Integer.parseInt(this.ic.substring(2, 4));
                int day = Integer.parseInt(this.ic.substring(4, 6));
                setAge(getAge(year, month, day));
            } else {
                setAge("");
            }
        } catch (Exception e) {
            setAge("Unknown");
        }
    }

    private String getAge(int year, int month, int day) {
        try {
            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            dob.set(year, month, day);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return String.valueOf(age).substring(2, 4);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        notifyPropertyChanged(BR.prefix);
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    @Override
    public String toString() {
        return name;
    }
}
