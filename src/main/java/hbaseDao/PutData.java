package hbaseDao;

import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutData {

    public static void main(String[] args) throws IOException {
        String zk=args[0];
        String htable=args[1];
        List<Bean> list=new ArrayList<>();
        for(int i=0;i<150;i++){
            Bean bean=new Bean();
            Map<String,String> map=new HashMap();
            for(int y=0;y<3;y++){
                map.put("col"+y,String.valueOf(y));
            }

            bean.setRk(String.valueOf(i));
            bean.setFamily("d");
            bean.setMap(map);
            list.add(bean);
        }
        putListBean(list,zk,htable);
    }

    public static void putListBean(List<Bean> list,String zk,String htable) throws IOException {
        HTableInterface hTableInterface=null;
        HbaseClient hbaseClient=new HbaseClient(zk);
        HConnection hConnection= hbaseClient.getConnection();
        try {
            hTableInterface=hConnection.getTable(htable);
            List<Put> listPut=new ArrayList<>();

            if(list.size()>0) {
                for (Bean bean : list) {
                    System.out.println(bean);
                    Put put = new Put(Bytes.toBytes(bean.getRk()));
                    if(bean.getRk().isEmpty())continue;
                    byte[] family=Bytes.toBytes(bean.getFamily());
                    byte [] column;
                    byte [] value;
                    for(Map.Entry<String,String> entry:bean.getMap().entrySet()){
                        column=Bytes.toBytes(entry.getKey());
                        value=Bytes.toBytes(entry.getValue());
                        put.add(family,column,value);
                    }
                    listPut.add(put);
                    if(listPut.size()>100)
                    {
                        hTableInterface.put(listPut);
                        listPut.clear();
                    }
                }
                if(listPut.size()>0)
                {
                    System.out.println("size"+listPut.size());
                    hTableInterface.put(listPut);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            try {
                if(hTableInterface!=null){
                    hTableInterface.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            try {
                if(hConnection!=null){
                    hConnection.close();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
}
