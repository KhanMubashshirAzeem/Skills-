package com.example.skills_plus.modal;

public class BlogModal {

    String title;
    String Description;
    String image;
    String timeStamp;
    String blogId;

    public BlogModal() {
    }

    public BlogModal(String title, String description, String image, String timeStamp, String blogId) {
        this.title = title;
        Description = description;
        this.image = image;
        this.timeStamp = timeStamp;
        this.blogId = blogId;
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

    public String getBlogId() {
        return blogId;
    }

    public void setBlogId(String blogId) {
        this.blogId = blogId;
    }
}
