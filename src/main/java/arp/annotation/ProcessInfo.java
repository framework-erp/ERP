package arp.annotation;

public class ProcessInfo {
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
