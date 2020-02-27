package lk.developments.microlion.fmmahanamalive;

public class UpdateItem {
    private String title, desc;

    public UpdateItem() {
    }

    public UpdateItem(String title, String genre) {
        this.title = title;
        this.desc = genre;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getDesc() {
        return desc;
    }

    public void getDesc(String desc) {
        this.desc = desc;
    }
}
