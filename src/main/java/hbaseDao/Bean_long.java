package hbaseDao;

import java.util.Map;

public class Bean_long {
    private Long rk;

    private String family;

    private Map<String ,Long>map;

    public Long getRk() {
        return rk;
    }

    public void setRk(Long rk) {
        this.rk = rk;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public Map<String, Long> getMap() {
        return map;
    }

    public void setMap(Map<String, Long> map) {
        this.map = map;
    }
}
