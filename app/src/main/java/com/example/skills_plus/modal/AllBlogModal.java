package com.example.skills_plus.modal;

public class AllBlogModal {

    String title;
    String Description;
    String image;
    String timeStamp;

    public AllBlogModal() {
    }

    public AllBlogModal(String title, String description, String image, String timeStamp) {
        this.title = title;
        Description = description;
        this.image = image;
        this.timeStamp = timeStamp;
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
