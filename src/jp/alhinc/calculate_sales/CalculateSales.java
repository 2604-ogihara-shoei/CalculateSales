package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
	private static final String AMOUNT_LARGE = "合計金額が10桁を超えました";
	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		//コマンドライン引数が1つ設定されていなかった場合は、
		//エラーメッセージをコンソールに表示します。
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

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
			//対象がファイルであり、「数字8桁.rcd」なのか判定します。
			if(files[i].isFile() && files[i].getName().matches("^[0-9]{8}\\.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
		//連番チェックの前に売上ファイルを保持しているListをソートする
		rcdFiles.sort(Comparator.naturalOrder());
		//比較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ小さい数です。
		for(int i = 0; i < rcdFiles.size() -1; i++) {
			String formerFileName = rcdFiles.get(i).getName();
			String latterFileName = rcdFiles.get(i + 1).getName();

			int former = Integer.parseInt(formerFileName.substring(0, 8));
			int latter = Integer.parseInt(latterFileName.substring(0, 8));

			//比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			if((latter - former) != 1) {
				//2つのファイル名の数字を比較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表示します。
				System.out.println(FILE_NOT_CONTINUOUS);
				return;
			}
		}

		for(int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;
			try {
				File file = rcdFiles.get(i);
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);//売り上げファイル読み込み

				//抽出して格納
				List<String> fileData = new ArrayList<>();
				String line;
				while((line = br.readLine()) != null) {
					fileData.add(line);
				}
				//売上ファイルの行数が2行ではなかった場合は、
			    //エラーメッセージをコンソールに表示します。
				if(fileData.size() != 2) {
					System.out.println(rcdFiles.get(i) +  "のフォーマットが不正です");
			        return;
				}
				String branchCode = fileData.get(0);//支店コード
				String Sale = fileData.get(1);//売り上げ金額

                //支店情報を保持しているMapに売上ファイルの支店コードが存在しなかった場合は、
			    //エラーメッセージをコンソールに表示します。
				if (!branchNames.containsKey(branchCode)) {
					System.out.println(rcdFiles.get(i) + "の支店コードが不正です");
					return ;
				}

				//売上金額が数字ではなかった場合は、
				//エラーメッセージをコンソールに表示します。
				if (!Sale.matches("^[0-9]+$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				long fileSale = Long.parseLong(Sale);
				long saleAmount = branchSales.get(branchCode) + fileSale;//Mapの売上金額を取得 //合算

				if(fileSale >= 10000000000L || saleAmount >= 10000000000L){
					//売上金額が11桁以上の場合、エラーメッセージをコンソールに表示します。
					System.out.println(AMOUNT_LARGE);
					return;
				}

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

		//支店別集計ファイル書き込み処理
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
