package com.example.skills_plus.modal;

public class CardModal {

    String username;
    String title;
    String Description;
    String image;
    String timeStamp;

    public CardModal() {
    }

    public CardModal(String username, String title, String description, String image, String timeStamp) {
        this.username = username;
        this.title = title;
        Description = description;
        this.image = image;
        this.timeStamp = timeStamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
