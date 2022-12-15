package arp.enhance;

public class ProcessInfo {
    private int id;
    private String clsName;
    private String mthName;
    private String mthDesc;

    public ProcessInfo() {
    }

    public ProcessInfo(String clsName, String mthName, String mthDesc) {
        this.clsName = clsName;
        this.mthName = mthName;
        this.mthDesc = mthDesc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    public String getMthName() {
        return mthName;
    }

    public void setMthName(String mthName) {
        this.mthName = mthName;
    }

    public String getMthDesc() {
        return mthDesc;
    }

    public void setMthDesc(String mthDesc) {
        this.mthDesc = mthDesc;
    }


}
