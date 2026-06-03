package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_CONTINUOUS = "売上ファイル名が連番になっていません";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<>();
		for(int i = 0; i < files.length ; i++) {
			if(files[i].getName().matches("^[0-9]{8}\\.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		for(int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;
			try {
				File file = rcdFiles.get(i);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
                List<String> fileData = new ArrayList<>();
                String line;
                while((line = br.readLine()) != null) {
                	fileData.add(line);
                }
                String branchCode = fileData.get(0);
                long fileSale = Long.parseLong(fileData.get(1));
                //long fileSale = Long.parseLong(売上金額);
          		//Long saleAmount = HashMap.get(支店コード) + long に変換した売上金額;
                long saleAmount = branchSales.get(branchCode) +  fileSale;//Mapの売上金額を取得 //合算
                branchSales.put(branchCode,saleAmount);
            } catch(IOException e) {
            	System.out.println(UNKNOWN_ERROR);
    			return ;
            } finally {
    			// ファイルを開いている場合
    			if(br != null) {
    				try {
    					// ファイルを閉じる
    					br.close();
    				} catch(IOException e) {
    					System.out.println(UNKNOWN_ERROR);
    					return;
    				}
    			}
    		 }
    	}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}
	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			//ファイルの存在確認
			if(!file.exists()) {
			    //支店定義ファイルが存在しない場合、コンソールにエラーメッセージを表示します。
			    System.out.println(FILE_NOT_EXIST);
			    return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				 //split を使って「,」(カンマ)で分割すると、
			    //items[0] には支店コード、items[1] には支店名が格納されます。
				String[] items = line.split(",");
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
				System.out.println(line);
				if((items.length != 2) || (!items[0].matches("^\\d{3}$"))){
				    //支店定義ファイルの仕様が満たされていない場合、
				    //エラーメッセージをコンソールに表示します。
				    System.out.println(FILE_INVALID_FORMAT);
				    return false;
				}

			}
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		try {
			File file = new File(path,fileName);
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String key : branchNames.keySet()) {
				String branchName = branchNames.get(key);
				Long saleAmount = branchSales.get(key);
				bw.write(key + "," + branchName + "," + saleAmount);
				bw.newLine();
			}
			bw.close();
		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		}
		return true;
	}
}
