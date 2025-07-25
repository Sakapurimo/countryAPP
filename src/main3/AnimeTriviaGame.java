package src.main3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URI;
import java.util.InputMismatchException; // InputMismatchException を import
import java.util.Scanner;
import java.util.Collections; // Collections を import
import java.util.List; // List を import
import java.util.Arrays; // Arrays を import

import org.json.JSONArray;
import org.json.JSONObject;

public class AnimeTriviaGame { // クラス名を AnimeTriviaExample から AnimeTriviaGame に変更

    // MyMemory翻訳APIを使った翻訳メソッド
    // このメソッドはprivate staticとしてクラス内にカプセル化
    private static String translate(String text, String sourceLang, String targetLang) throws Exception {
        String encodedText = URLEncoder.encode(text, "UTF-8");
        String langpair = sourceLang + "|" + targetLang;
        String encodedLangpair = URLEncoder.encode(langpair, "UTF-8");

        String apiUrl = "https://api.mymemory.translated.net/get?q=" + encodedText + "&langpair=" + encodedLangpair;

        HttpURLConnection conn = (HttpURLConnection) new URI(apiUrl).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JSONObject json = new JSONObject(response.toString());
        JSONObject responseData = json.getJSONObject("responseData");
        return responseData.getString("translatedText");
    }

    /**
     * アニメトリビアゲームを開始します。
     * OpenTDB APIからアニメに関するクイズを取得し、日本語に翻訳してユーザーに提示します。
     *
     * @param scanner ユーザー入力のためのScannerオブジェクト
     */
    public static void playAnimeTriviaGame(Scanner scanner) { // 関数化し、Scannerを引数として受け取るように変更
        try {
            String apiUrl = "https://opentdb.com/api.php?amount=1&category=31&type=multiple";

            HttpURLConnection conn = (HttpURLConnection) new URI(apiUrl).toURL().openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder responseBuffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuffer.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(responseBuffer.toString());
            JSONArray results = json.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject questionObj = results.getJSONObject(0);

                // 問題文（英語）
                String question = questionObj.getString("question");

                // 選択肢の配列を作成
                JSONArray incorrectArray = questionObj.getJSONArray("incorrect_answers");
                String correctAnswer = questionObj.getString("correct_answer");

                // 選択肢を一つの配列にまとめる
                int totalChoices = incorrectArray.length() + 1;
                String[] choices = new String[totalChoices];
                for (int i = 0; i < incorrectArray.length(); i++) {
                    choices[i] = incorrectArray.getString(i);
                }
                choices[totalChoices - 1] = correctAnswer;

                // 翻訳処理（英語→日本語）
                String questionJa = translate(question, "en", "ja");

                for (int i = 0; i < totalChoices; i++) {
                    choices[i] = translate(choices[i], "en", "ja");
                }

                // 選択肢をシャッフル（正解がどこにあるかわからなくする）
                List<String> choiceList = Arrays.asList(choices);
                Collections.shuffle(choiceList);

                // 表示
                System.out.println("--- アニメトリビアゲーム ---");
                System.out.println("問題:");
                System.out.println(questionJa);
                System.out.println();

                for (int i = 0; i < totalChoices; i++) {
                    System.out.println((i + 1) + ": " + choiceList.get(i));
                }

                // ユーザーに回答を入力してもらう
                System.out.println();
                System.out.print("回答番号を入力してください（例: 1） > ");
                int answerIndex = -1;
                try {
                    answerIndex = scanner.nextInt() - 1;
                    scanner.nextLine(); // 改行文字を消費
                } catch (InputMismatchException e) {
                    System.out.println("無効な入力です。数字を入力してください。");
                    scanner.nextLine(); // 不正な入力をクリア
                    return; // ゲームを終了するか、再試行させるか選択
                }

                if (answerIndex >= 0 && answerIndex < totalChoices) {
                    String selectedAnswer = choiceList.get(answerIndex);

                    // 正解か判定
                    String correctAnswerJa = translate(correctAnswer, "en", "ja");

                    if (selectedAnswer.equals(correctAnswerJa)) {
                        System.out.println("正解です！おめでとう！");
                    } else {
                        System.out.println("残念、不正解です。");
                        System.out.println("正解は: " + correctAnswerJa);
                    }
                } else {
                    System.out.println("無効な回答番号です。");
                }

            } else {
                System.out.println("問題が見つかりませんでした。");
            }

        } catch (Exception e) {
            System.out.println("エラーが発生しました。クイズの取得または翻訳に失敗しました。");
            e.printStackTrace();
        }
    }

    // テスト用のmainメソッド
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            playAnimeTriviaGame(scanner);
        }
    }
}