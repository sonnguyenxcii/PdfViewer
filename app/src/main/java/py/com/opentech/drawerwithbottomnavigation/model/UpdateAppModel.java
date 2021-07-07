package py.com.opentech.drawerwithbottomnavigation.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateAppModel {

    @SerializedName("version_code")
    @Expose
    private Integer versionCode;
    @SerializedName("version_name")
    @Expose
    private String versionName;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("Status")
    @Expose
    private Boolean status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("version_code_required")
    @Expose
    private List<Integer> versionCodeRequired = null;
    @SerializedName("required")
    @Expose
    private Boolean required;
    @SerializedName("new_package")
    @Expose
    private String newPackage;

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Integer> getVersionCodeRequired() {
        return versionCodeRequired;
    }

    public void setVersionCodeRequired(List<Integer> versionCodeRequired) {
        this.versionCodeRequired = versionCodeRequired;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getNewPackage() {
        return newPackage;
    }

    public void setNewPackage(String newPackage) {
        this.newPackage = newPackage;
    }

}