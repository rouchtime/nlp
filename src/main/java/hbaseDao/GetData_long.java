package hbaseDao;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetData_long {

    public static void main(String[] args) throws IOException {
    /*    String zk=args[0];
        String htable=args[1];
        List<String> list=new ArrayList<>();
        for(int i=1;i<121;i++){
            list.add(String.valueOf(1000+i));
        }

        List<Bean> listB=getListBean(list,zk,htable);
        System.out.println(listB);*/

    }

    public static List<Bean_long> getListBean(List<Long> list,String zk,String htable) throws IOException {
        HTableInterface hTableInterface = null;
        HbaseClient hbaseClient = new HbaseClient(zk);
        HConnection hConnection = hbaseClient.getConnection();
        List<Bean_long> listBean = new ArrayList<>();
        try {
            hTableInterface = hConnection.getTable(htable);
            List<Get> listGet = new ArrayList<>();

            if (!list.isEmpty()) {
                for (Long s : list) {
                    Get get = new Get(Bytes.toBytes(s));
                    get.setCacheBlocks(false);
                    listGet.add(get);
                    if (listGet.size() > 100) {
                        Result[] results = hTableInterface.get(listGet);
                        for (Result result : results) {

                            if (!result.isEmpty()) {
                                Bean_long bean = new Bean_long();
                                Map<String, Long> map = new HashMap<>();
                                String rks = Bytes.toString(result.getRow());
                                long rk=Long.valueOf(rks);
                                bean.setRk(rk);
                                List<Cell> cellList = result.listCells();
                                String cfs = "";
                                String quas = "";
                                String vals = "";
                                long vall=0l;
                                for (Cell cell : cellList) {
                                    byte[] cf = CellUtil.cloneFamily(cell);
                                    cfs = Bytes.toString(cf);
                                    byte[] qua = CellUtil.cloneQualifier(cell);
                                    quas = Bytes.toString(qua);
                                    byte[] val = CellUtil.cloneValue(cell);
                                    vals = Bytes.toString(val);
                                    vall=Long.valueOf(vals);
                                    map.put(quas, vall);
                                    bean.setFamily(cfs);
                                    bean.setMap(map);

                                }
                                listBean.add(bean);
                            }
                            listBean.add(null);
                        }
                        listGet.clear();
                    }
                }

                if (listGet.size() > 0) {
                    System.out.println("size:"+listGet.size());
                    Result[] results = hTableInterface.get(listGet);
                    for (Result result : results) {

                        if (!result.isEmpty()) {
                            Bean_long bean = new Bean_long();
                            Map<String, Long> map = new HashMap<>();
                            String rks = Bytes.toString(result.getRow());
                            long rk=Long.valueOf(rks);
                            List<Cell> cellList = result.listCells();
                            String cfs = "";
                            String quas = "";
                            String vals = "";
                            long vall=0l;
                            for (Cell cell : cellList) {
                                byte[] cf = CellUtil.cloneFamily(cell);
                                cfs = Bytes.toString(cf);
                                byte[] qua = CellUtil.cloneQualifier(cell);
                                quas = Bytes.toString(qua);
                                byte[] val = CellUtil.cloneValue(cell);
                                vals = Bytes.toString(val);
                                vall=Long.valueOf(vals);
                                map.put(quas, vall);
                                bean.setFamily(cfs);
                                bean.setMap(map);

                            }
                            listBean.add(bean);
                        }
                        listBean.add(null);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (hTableInterface != null) {
                    hTableInterface.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (hConnection != null) {
                    hConnection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return listBean;
    }
}
