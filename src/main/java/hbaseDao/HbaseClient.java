package hbaseDao;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;

import java.io.IOException;

public class HbaseClient {
    private HConnection connection;
    public HConnection getConnection() {
        return connection;
    }
    public HbaseClient(String zookeeper) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zookeeper);// zookeeper地址
        connection = HConnectionManager.createConnection(conf);
    }
}
