package hbaseDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseTbUrlKwDao {
	private HbaseTbUrlKwDao() {
	}

	public static HbaseTbUrlKwDao getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static HbaseTbUrlKwDao instance = new HbaseTbUrlKwDao();
	}

	public List<UrlKwBean> getListBean(List<String> list, String zk) throws IOException {
		HTableInterface hTableInterface = null;
		HbaseClient hbaseClient = new HbaseClient(zk);
		HConnection hConnection = hbaseClient.getConnection();
		List<UrlKwBean> listBean = new ArrayList<>();
		try {
			hTableInterface = hConnection.getTable("tb_kw_query");
			List<Get> listGet = new ArrayList<>();
			if (!list.isEmpty()) {
				for (String s : list) {
					Get get = new Get(Bytes.toBytes(s));
					get.setCacheBlocks(false);
					listGet.add(get);
					if (listGet.size() > 100) {
						Result[] results = hTableInterface.get(listGet);
						for (Result result : results) {
							if (!result.isEmpty()) {
								UrlKwBean bean = new UrlKwBean();
								Map<String, String> map = new HashMap<>();
								String rk = Bytes.toString(result.getRow());
								bean.setRk(rk);
								List<Cell> cellList = result.listCells();
								String cfs = "";
								String quas = "";
								String vals = "";
								for (Cell cell : cellList) {
									byte[] cf = CellUtil.cloneFamily(cell);
									cfs = Bytes.toString(cf);
									byte[] qua = CellUtil.cloneQualifier(cell);
									quas = Bytes.toString(qua);
									byte[] val = CellUtil.cloneValue(cell);
									vals = Bytes.toString(val);
									map.put(quas, vals);
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
					System.out.println("size:" + listGet.size());
					Result[] results = hTableInterface.get(listGet);
					for (Result result : results) {

						if (!result.isEmpty()) {
							UrlKwBean bean = new UrlKwBean();
							Map<String, String> map = new HashMap<>();
							String rk = Bytes.toString(result.getRow());
							bean.setRk(rk);
							List<Cell> cellList = result.listCells();
							String cfs = "";
							String quas = "";
							String vals = "";
							for (Cell cell : cellList) {
								byte[] cf = CellUtil.cloneFamily(cell);
								cfs = Bytes.toString(cf);
								byte[] qua = CellUtil.cloneQualifier(cell);
								quas = Bytes.toString(qua);
								byte[] val = CellUtil.cloneValue(cell);
								vals = Bytes.toString(val);
								map.put(quas, vals);
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

	public void putListBean(List<UrlKwBean> list, String zk) throws IOException {
		HTableInterface hTableInterface = null;
		HbaseClient hbaseClient = new HbaseClient(zk);
		HConnection hConnection = hbaseClient.getConnection();
		try {
			hTableInterface = hConnection.getTable("tb_kw_query");
			List<Put> listPut = new ArrayList<>();

			if (list.size() > 0) {
				for (UrlKwBean bean : list) {
					System.out.println(bean);
					Put put = new Put(Bytes.toBytes(bean.getRk()));
					if (bean.getRk().isEmpty())
						continue;
					byte[] family = Bytes.toBytes(bean.getFamily());
					byte[] column;
					byte[] value;
					for (Map.Entry<String, String> entry : bean.getMap().entrySet()) {
						column = Bytes.toBytes(entry.getKey());
						value = Bytes.toBytes(entry.getValue());
						put.add(family, column, value);
					}
					listPut.add(put);
					if (listPut.size() > 100) {
						hTableInterface.put(listPut);
						listPut.clear();
					}
				}
				if (listPut.size() > 0) {
					System.out.println("size" + listPut.size());
					hTableInterface.put(listPut);
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
	}
}
