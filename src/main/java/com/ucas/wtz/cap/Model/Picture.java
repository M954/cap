package com.ucas.wtz.cap.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Picture {
    @Id
    private String id;
    private String imgPath;
    private Date dateTime;
    private String label;
    private String provider;
    private String place;
    private boolean copyright;
    private boolean published;
    private String description;

    public Picture(){};
    public Picture(String id, String imgPath, Date dateTime, String label, String provider, String place, boolean copyright, boolean published, String description) {
        this.id = id;
        this.imgPath = imgPath;
        this.dateTime = dateTime;
        this.label = label;
        this.provider = provider;
        this.place = place;
        this.copyright = copyright;
        this.published = published;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public boolean isCopyright() {
        return copyright;
    }

    public void setCopyright(boolean copyright) {
        this.copyright = copyright;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}

