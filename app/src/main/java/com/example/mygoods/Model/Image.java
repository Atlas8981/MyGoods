package com.example.mygoods.Model;

import java.io.Serializable;
import java.util.Objects;

public class Image implements Serializable {
    private String imageURL;
    private String imageName;

    public Image(){

    }
    public Image(String imageURL) {
        this.imageURL = imageURL;
        this.imageName = "ImageName";
    }
    public Image(String imageURL, String imageName) {
        this.imageURL = imageURL;
        this.imageName = imageName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public String toString() {
        return "Image{" +
                "imageURL='" + imageURL + '\'' +
                ", imageName='" + imageName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Image image = (Image) o;
        return Objects.equals(imageURL, image.imageURL) &&
                Objects.equals(imageName, image.imageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageURL, imageName);
    }
}
