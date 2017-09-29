package task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class BprGD {

	public static void main(String[] args) {
		int k = 100;
		double lr = 0.002, r = 0.06;
		System.out.println(lr);
		// String trainPath = args[0];
		// String storePath = args[1];
		// int allIter=Integer.parseInt(args[2]);
		// String urlTypePath = args[3];
		String trainPath = "E:/bpr_prior/dataset_ios";
		String storePath = "E:/bpr_prior";
		int allIter = 18;
		// String urlTypePath = "E:/bpr_prior/show_active_url_type_20170905";
		// 全局参数设计 ---特征矩阵和梯度矩阵参数
		HashMap<String, double[]> userMap = new HashMap<String, double[]>();
		HashMap<String, double[]> urlMap = new HashMap<String, double[]>();

		// 参数初始化
		initialMatrixFeature(userMap, urlMap, trainPath, k); // 初始化userMap, urlMap

		// 开始训练参数
		int iter = 0;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 璁剧疆鏃ユ湡鏍煎紡
		System.out.println(df.format(new Date()) + " begain, train: ");
		while (iter <= allIter) {
			System.out.println(df.format(new Date()) + " iter= " + iter);
			double train_recall = testMatrixFeatureRecall(userMap, urlMap, trainPath, k);
			System.out.println(df.format(new Date()) + " iter= " + iter + " train_recall= " + train_recall);
			// 更新偏序参数
			updateBprWeight(userMap, urlMap, trainPath, k, lr, r);
			lr *= 0.98;
			iter += 1;
		}
		// 存储参数结果
		storeFeatureMatrix(userMap, urlMap, storePath);
		System.out.println("process end!");
	}

	public static double testMatrixFeatureRecall(HashMap<String, double[]> userMap, HashMap<String, double[]> urlMap,
			String testPath, int k) {
		File file = new File(testPath);
		int count = 0, rightCount = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String[] words = tempString.split("\001");
				if (userMap.containsKey(words[0]) && urlMap.containsKey(words[1]) && urlMap.containsKey(words[2])) {
					if (1.0 / heplerDotVec(userMap.get(words[0]), urlMap.get(words[1]), urlMap.get(words[2]), k) < 0.5)
						rightCount++;
					;
					count++;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return 1.0 * rightCount / count;
	}

	public static void updateBprWeight(HashMap<String, double[]> userMap, HashMap<String, double[]> urlMap,
			String trainPath, int k, double lr, double r) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 璁剧疆鏃ユ湡鏍煎紡
		System.out.println(df.format(new Date()) + " begain, updateBprWeight: ");
		HashMap<String, double[]> userGradientMap = new HashMap<String, double[]>();
		HashMap<String, double[]> urlGradientMap = new HashMap<String, double[]>();

		File file = new File(trainPath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String[] words = tempString.split("\001");
				double[] userGradient, urlGradient1, urlGradient2;
				if (userGradientMap.containsKey(words[0])) {
					userGradient = userGradientMap.get(words[0]);
				} else {
					userGradient = new double[k];
					userGradientMap.put(words[0], userGradient);
				}
				if (urlGradientMap.containsKey(words[1])) {
					urlGradient1 = urlGradientMap.get(words[1]);
				} else {
					urlGradient1 = new double[k];
					urlGradientMap.put(words[1], urlGradient1);
				}
				if (urlGradientMap.containsKey(words[2])) {
					urlGradient2 = urlGradientMap.get(words[2]);
				} else {
					urlGradient2 = new double[k];
					urlGradientMap.put(words[2], urlGradient2);
				}
				double[] user = userMap.get(words[0]);
				double[] url1 = urlMap.get(words[1]);
				double[] url2 = urlMap.get(words[2]);
				double item = heplerDotVec(user, url1, url2, k);
				for (int i = 0; i < k; ++i) {
					userGradient[i] += (url2[i] - url1[i]) / item;
					urlGradient1[i] -= user[i] / item;
					urlGradient2[i] += user[i] / item;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		// 根据 梯度更新参数
		for (String key : userMap.keySet()) {
			double[] user = userMap.get(key);
			double[] userGradient = userGradientMap.get(key);
			for (int i = 0; i < k; ++i) {
				// if(key.equals("03A866D1-D8D7-45BD-8E46-CC288568EBB9")) {
				// System.out.print(String.valueOf(i) + " " + String.valueOf(user[i]) + " " +
				// String.valueOf(userGradient[i]) + " ");
				// }
				user[i] = (1 - lr * r) * user[i] - lr * userGradient[i];
				// if(key.equals("03A866D1-D8D7-45BD-8E46-CC288568EBB9")) {
				// System.out.println(String.valueOf(user[i]) );
				// }
			}
		}
		for (String key : urlMap.keySet()) {
			double[] url = urlMap.get(key);
			double[] urlGradient = urlGradientMap.get(key);
			for (int i = 0; i < k; ++i) {
				// if(key.equals("170903173227743")) {
				// System.out.print(String.valueOf(i) + " " + String.valueOf(url[i]) + " " +
				// String.valueOf(urlGradient[i]) + " ");
				// }
				url[i] = (1 - lr * r) * url[i] - lr * urlGradient[i];
				// if(key.equals("170903173227743")) {
				// System.out.println(String.valueOf(url[i])+"; ");
				// }
			}
		}
		System.out.println(df.format(new Date()) + " end, updateBprWeight: ");
	}

	public static void initialMatrixFeature(HashMap<String, double[]> userMap, HashMap<String, double[]> urlMap,
			String trainPath, int k) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 璁剧疆鏃ユ湡鏍煎紡
		System.out.println(df.format(new Date()) + " begain, initalMatrixFeature: ");
		File file = new File(trainPath);
		BufferedReader reader = null;
		try {
			java.util.Random r = new java.util.Random();
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String[] words = tempString.split("\001");
				if (!userMap.containsKey(words[0])) {
					double[] vec = new double[k];
					for (int i = 0; i < k; ++i) {
						vec[i] = r.nextInt(1000) * 1.0 / 5000 - 0.1; // (-0.1,0.1)
					}
					userMap.put(words[0], vec);
				}
				if (!urlMap.containsKey(words[1])) {
					double[] vec1 = new double[k];
					for (int i = 0; i < k; ++i) {
						vec1[i] = r.nextInt(1000) * 1.0 / 5000 - 0.1;
					}
					urlMap.put(words[1], vec1);
				}
				if (!urlMap.containsKey(words[2])) {
					double[] vec2 = new double[k];
					for (int i = 0; i < k; ++i) {
						vec2[i] = r.nextInt(1000) * 1.0 / 5000 - 0.1;
					}
					urlMap.put(words[2], vec2);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		System.out.println(df.format(new Date()) + " end, initalMatrixFeature: ");
	}

	public static double heplerDotVec(double[] user, double[] url1, double[] url2, int k) {
		double sum1 = 0.0;
		double sum2 = 0.0;
		for (int i = 0; i < k; ++i) {
			sum1 += user[i] * url1[i];
			sum2 += user[i] * url2[i];
		}
		double item = 1.0 + Math.exp(sum1 - sum2);
		return item;
	}

	public static void storeFeatureMatrix(HashMap<String, double[]> userMap, HashMap<String, double[]> urlMap,
			String storePath) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 璁剧疆鏃ユ湡鏍煎紡
		System.out.println(df.format(new Date()) + " begain, storeFeatureMatrix: ");

		try {
			File file_user = new File(storePath + "/userFeature");
			if (file_user.exists()) {
				file_user.delete();
			}
			file_user.createNewFile();
			FileWriter fw = new FileWriter(file_user.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (String key : userMap.keySet()) {
				for (double item : userMap.get(key)) {
					key = key.concat('	' + String.valueOf(item));
				}
				key = key.concat(String.valueOf('\n'));
				bw.write(key);
			}
			bw.close();

			File file_url = new File(storePath + "/urlFeature");
			if (file_url.exists()) {
				file_url.delete();
			}
			file_url.createNewFile();
			fw = new FileWriter(file_url.getAbsoluteFile());
			bw = new BufferedWriter(fw);

			SimpleDateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");// 璁剧疆鏃ユ湡鏍煎紡
			// Date d=new Date();
			String date = df1.format(new Date((long) 1504627200 * 1000 - (long) 3 * 24 * 60 * 60 * 1000)); // 涓夊ぉ鍓嶇殑鏃ユ湡,琛?40鍒嗛挓
			Long max_date = Long.parseLong(date.substring(2, 12));

			System.out.println("The flag time is " + max_date);

			for (String key : urlMap.keySet()) {
				String dt = key.substring(0, 10); // add
				if (Long.parseLong(dt) < max_date) // add
					continue; // add
				for (double item : urlMap.get(key)) {
					key = key.concat('	' + String.valueOf(item));
				}
				key = key.concat(String.valueOf('\n'));
				bw.write(key);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(df.format(new Date()) + " end, storeFeatureMatrix: ");
	}

}
