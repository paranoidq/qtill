package me.qtill.zookeeper.practices.leaderElection;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class RunningData {

    private static final long serialVersionUID = 4260577459043203630L;


    private long cid;
    private String name;

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
