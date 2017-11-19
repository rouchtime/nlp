package com.rouchtime.nlp.duplicate.simhash;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.rouchtime.util.RegexUtils;

import gnu.trove.map.TMap;
import hbaseDao.HbaseTbSimhashQueryDao;
import hbaseDao.SimHashBean;

public class SimHashForMassDataDuplicate {
	private Map<Long, List<Doc>> map = new HashMap<Long, List<Doc>>();
	SimHashForText sft = SimHashForText.getInstance();
	HbaseTbSimhashQueryDao tbSimHashQueryDao = HbaseTbSimhashQueryDao.getInstance();
	private String columFamliy = "d";
	private String zk = "";

	public List<String> selectFromRAM(String doc) throws Exception {
		long simvalue = sft.getFingerPrint(doc, 64);
		long[] keys = splitFingerPrint(simvalue);
		List<String> dupList = new ArrayList<String>();
		Set<String> filterSet = new HashSet<String>();
		for (long key : keys) {
			if (map.get(key) != null) {
				for (Doc d : map.get(key)) {
					if (filterSet.contains(d.getRowkey())) {
						continue;
					} else {
						if (sft.hammingDistance(d.getHash(), simvalue) <= 6) {
							dupList.add(d.getRowkey());
							filterSet.add(d.getRowkey());
						}
					}
				}
			}
		}
		return dupList;
	}

	public void addToRAM(String newskey, String doc) throws Exception {
		long simvalue = sft.getFingerPrint(doc, 64);
		long[] keys = splitFingerPrint(simvalue);
		for (long key : keys) {
			Doc d = new Doc();
			d.setHash(simvalue);
			d.setRowkey(newskey);
			if (map.get(key) == null) {
				List<Doc> listDoc = new ArrayList<Doc>();
				listDoc.add(d);
				map.put(key, listDoc);
			} else {
				map.get(key).add(d);
			}
		}
	}

	public boolean add(String newskey, String doc) {
		long simvalue = sft.getFingerPrint(doc, 64);
		long[] keys = splitFingerPrint(simvalue);
		List<Long> tmpKeyList = new ArrayList<Long>();
		for (long k : keys) {
			tmpKeyList.add(k);
		}
		try {
			List<SimHashBean> simhashBeanList = tbSimHashQueryDao.getListBean(tmpKeyList, zk);
			if (tmpKeyList.size() != simhashBeanList.size()) {
				System.err.println(String.format("Check Keys Number is Not Equal Result Number %d!=%d",
						tmpKeyList.size(), simhashBeanList.size()));
				return false;
			}
			for (int i = 0; i < simhashBeanList.size(); i++) {
				if (simhashBeanList.get(i) == null) {
					SimHashBean bean = new SimHashBean();
					bean.setFamily(columFamliy);
					bean.setRk(tmpKeyList.get(i));
					Map<String, Long> map = new HashMap<>();
					try {
						map.put(String.valueOf((Long.MAX_VALUE - Long.parseLong(newskey))), simvalue);
						bean.setMap(map);
						simhashBeanList.set(i, bean);
					} catch(NumberFormatException e) {
						continue;
					}
				} else {
					Map<String,Long> resultMap = simhashBeanList.get(i).getMap();
					try {
						resultMap.put(String.valueOf((Long.MAX_VALUE - Long.parseLong(newskey))), simvalue);
					} catch(NumberFormatException e) {
						continue;
					}
				}
			}
			tbSimHashQueryDao.putListSimhash(simhashBeanList, zk);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public List<String> select(String doc) {
		long simvalue = sft.getFingerPrint(doc, 64);
		long[] keys = splitFingerPrint(simvalue);
		List<Long> tmpKeyList = new ArrayList<Long>();
		for (long k : keys) {
			tmpKeyList.add(k);
		}
		Set<String> tmpUrlSet = new HashSet<String>();
		try {
			List<SimHashBean> simhashBeanList = tbSimHashQueryDao.getListBean(tmpKeyList, zk);
			for(SimHashBean bean : simhashBeanList) {
				if(bean == null) {
					continue;
				} 
				Map<String,Long> resultMap = bean.getMap();
				if(resultMap == null || resultMap.size() == 0) {
					continue;
				}
				for(Entry<String,Long> entry : resultMap.entrySet()) {
					if(tmpUrlSet.contains(entry.getKey())) {
						continue;
					}
					if(sft.hammingDistance(entry.getValue(), simvalue) <= 6) {
						tmpUrlSet.add(entry.getKey());
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new ArrayList<String>(tmpUrlSet);
	}
	
	
	
	public long[] splitFingerPrint(Long simhash) {
		StringBuilder sb = new StringBuilder();
		String[] splitHashValue = new String[9];

		/* 分8份，C(2,8) */
		// for (int i = 0; i < 64; i++) {
		// sb.append(simhash >> i & 1);
		// if ((i + 1) % 8 == 0) {
		// splitHashValue[(i + 1) / 8 - 1] = sb.toString();
		// sb.setLength(0);
		// }
		// }

		for (int i = 0; i < 56; i++) {
			sb.append(simhash >> i & 1);
			if ((i + 1) % 7 == 0) {
				splitHashValue[(i + 1) / 7 - 1] = sb.toString();
				sb.setLength(0);
			}
		}
		for (int i = 56; i < 64; i++) {
			sb.append(simhash >> i & 1);
			splitHashValue[8] = sb.toString();
			sb.setLength(0);
		}
		List<String[]> arrays = new ArrayList<String[]>();
		combinationSelect(splitHashValue, 2, arrays);
		long[] result = new long[arrays.size()];
		int k = 0;
		for (String[] list : arrays) {
			String _tmp = list[0] + list[1];
			BigInteger bigInt = new BigInteger(_tmp, 2);
			result[k++] = bigInt.longValue();
		}
		return result;
	}

	/**
	 * 组合选择（从列表中选择n个组合）
	 * 
	 * @param dataList
	 *            待选列表
	 * @param n
	 *            选择个数
	 */
	private void combinationSelect(String[] dataList, int n, List<String[]> arrays) {
		combinationSelect(dataList, 0, new String[n], 0, arrays);
	}

	/**
	 * 组合选择
	 * 
	 * @param dataList
	 *            待选列表
	 * @param dataIndex
	 *            待选开始索引
	 * @param resultList
	 *            前面（resultIndex-1）个的组合结果
	 * @param resultIndex
	 *            选择索引，从0开始
	 */
	private void combinationSelect(String[] dataList, int dataIndex, String[] resultList, int resultIndex,
			List<String[]> arrays) {
		int resultLen = resultList.length;
		int resultCount = resultIndex + 1;
		if (resultCount > resultLen) { // 全部选择完时，输出组合结果
			String[] list = new String[resultList.length];
			for (int i = 0; i < resultList.length; i++) {
				list[i] = resultList[i];
			}
			arrays.add(list);
			return;
		}
		// 递归选择下一个
		for (int i = dataIndex; i < dataList.length + resultCount - resultLen; i++) {
			resultList[resultIndex] = dataList[i];
			combinationSelect(dataList, i + 1, resultList, resultIndex + 1, arrays);
		}
	}

	public static SimHashForMassDataDuplicate getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static SimHashForMassDataDuplicate instance = new SimHashForMassDataDuplicate();
	}

	private SimHashForMassDataDuplicate() {
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		SimHashForMassDataDuplicate utils = SimHashForMassDataDuplicate.getInstance();
		File[] files = new File("E:\\corpus\\duplicate").listFiles();
		InputStream in = null;
		BufferedReader br = null;
		int i = 0;
		long s = System.currentTimeMillis();
		for (File file : files) {
			if (!file.getName().equals("xaa")) {
				continue;
			}
			try {
				in = new FileInputStream(file);
				br = new BufferedReader(new InputStreamReader(in));
				String line;
				line = br.readLine();
				String[] splits = null;
				while (line != null) {
					splits = line.split("\t+");
					if (splits.length != 4) {
						line = br.readLine();
						continue;
					}
					String rawKey = RegexUtils.convertURLToNewsKey(splits[1]);
					String title = splits[2];
					String raw = RegexUtils.cleanParaAndImgLabel(splits[3]);
					// long a = System.currentTimeMillis();
					utils.addToRAM(splits[1]+":" +title , title + raw);
					// System.out.println(System.currentTimeMillis() - a);
					line = br.readLine();
					if (i % 5000 == 0) {
						long e = System.currentTimeMillis();
						System.out.println(i + ":" + (e - s) * 1.0 / 5000);
						s = System.currentTimeMillis();

					}
					i++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		try {
			in = new FileInputStream(new File("E:\\corpus\\duplicate\\xaa"));
			br = new BufferedReader(new InputStreamReader(in));
			String line;
			line = br.readLine();
			String[] splits = null;
			long sum = 0;
			int c = 0;
			while (line != null) {
				splits = line.split("\t+");
				if (splits.length != 4) {
					line = br.readLine();
					continue;
				}
				String title = splits[2];
				String raw = RegexUtils.cleanParaAndImgLabel(splits[3]);
				long st = System.currentTimeMillis();
				List<String> list = utils.selectFromRAM(title + raw);
				long et = System.currentTimeMillis() - st;
				sum += et;
				if (c % 1000 == 0) {
					System.out.println(sum * 1.0 / c);
				}
				 if (list.size() == 1) {
				 line = br.readLine();
				 continue;
				 }
				 FileUtils.write(new File("D://simhashResult"), String.format(">>>>>>%s:%s\n",
				 splits[1], title),
				 "utf-8", true);
				 for (String url : list) {
				 if (url.equals(splits[1] + ":" + title)) {
				 continue;
				 }
				 FileUtils.write(new File("D://simhashResult"), String.format("%s\n", url),
				 "utf-8", true);
				 }
				 FileUtils.write(new File("D://simhashResult"), "*************************\n",
				 "utf-8", true);
				line = br.readLine();
				c++;
			}

			System.out.println(sum * 1.0 / c);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
