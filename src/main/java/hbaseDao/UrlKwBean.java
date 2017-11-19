package hbaseDao;

import java.util.Map;

public class UrlKwBean {
    private String rk;

    private String family;

    private Map<String ,String>map;


    public String getRk() {
        return rk;
    }

    public void setRk(String rk) {
        this.rk = rk;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "rk='" + rk + '\'' +
                ", family='" + family + '\'' +
                ", map=" + map +
                '}';
    }
}
