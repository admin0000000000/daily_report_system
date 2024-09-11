package models.validators;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import actions.views.ReportView;
import constants.MessageConst;
import services.ReportService;


    /**
     * 日報インスタンスの各項目についてバリデーションを行う
     * @param rv 日報インスタンス
     * @return エラーのリスト
     * @param service 呼び出し元Serviceクラスのインスタンス
     * @param report_dateDuplicateCheckFlag 日付の重複チェックを実施するかどうか(実施する:true 実施しない:false)
     */

public class ReportValidator {

    public static List<String> validate(ReportView rv, ReportService service,  Boolean report_dateDuplicateCheckFlag) {
        List<String> errors = new ArrayList<String>();

        //日付のチェック
        String report_dateError = validatereport_Date(service, rv.getReportDate(), report_dateDuplicateCheckFlag);
        if (!report_dateError.equals("")) {
            errors.add(report_dateError);
            }

        //タイトルのチェック
        String titleError = validateTitle(rv.getTitle());
        if (!titleError.equals("")) {
            errors.add(titleError);
        }

        //出勤日時のチェック
        String clock_inError = validateClock_in(rv.getClock_in());
        if (!clock_inError.equals("")) {
            errors.add(clock_inError);
        }


        //退勤日時のチェック
        String clock_outError = validateClock_out(rv.getClock_out());
        if (!clock_outError.equals("")) {
            errors.add(clock_outError);
        }

        //退勤日時のチェック
        String clock_CompareError = validateClock_compare(rv.getClock_in(),rv.getClock_out());
        if (!clock_CompareError.equals("")) {
            errors.add(clock_CompareError);
        }

        //内容のチェック
        String contentError = validateContent(rv.getContent());
        if (!contentError.equals("")) {
            errors.add(contentError);
        }

        return errors;
    }

    /**
     * 日付の入力チェックを行い、エラーメッセージを返却
     * @param service ReportServiceのインスタンス
     * @param date 日付
     * @param dateDuplicateCheckFlag 日付の重複チェックを実施するかどうか(実施する:true 実施しない:false)
     * @return エラーメッセージ
     */
    private static String validatereport_Date(ReportService service, LocalDate report_date, Boolean report_dateDuplicateCheckFlag) {

        if (report_dateDuplicateCheckFlag) {
            //日付の重複チェックを実施

            long reportCount = isReport_Date(service, report_date);

            //同一日付が既に登録されている場合はエラーメッセージを返却
            if (reportCount > 0) {
                return MessageConst.E_REP_DATE_EXIST.getMessage();
            }
        }

        //エラーがない場合は空文字を返却
        return "";
    }


    /**
     * @param service EmployeeServiceのインスタンス
     * @param report_date 日付
     * @return 日報テーブルに登録されている同一日付のデータの件数
     */
    private static long isReport_Date(ReportService service, LocalDate report_date) {

        long reportCount = service.countByreport_Date(report_date);
        return reportCount;
    }


    /**
     * タイトルに入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param title タイトル
     * @return エラーメッセージ
     */
    private static String validateTitle(String title) {
        if (title == null || title.equals("")) {
            return MessageConst.E_NOTITLE.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 出勤日時に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param clock_in 出勤日時
     * @return エラーメッセージ
     */
    private static String validateClock_in(LocalDateTime clock_in) {
        if (clock_in == null || clock_in.equals("") ) {
            return MessageConst.E_NOCLOCK_IN.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 退勤日時に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param clock_out 退勤日時
     * @return エラーメッセージ
     */

    private static String validateClock_out(LocalDateTime clock_out) {

        if (clock_out == null || clock_out.equals("") ) {
            return MessageConst.E_NOCLOCK_OUT.getMessage();
        }
        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 退勤時刻より出勤時刻が大きければエラーメッセージを返却
     * @param clock_Compare 時刻比較
     * @return エラーメッセージ
     */

    private static String validateClock_compare(LocalDateTime clock_in,LocalDateTime clock_out) {
        if (clock_in != null && clock_out != null ) {
        if(clock_in .compareTo(clock_out)>0) {
            return MessageConst.E_CLOCK_COMPARE.getMessage();
            }
        }
        //入力値がある場合は空文字を返却
        return "";
    }


    /**
     * 内容に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param content 内容
     * @return エラーメッセージ
     */
    private static String validateContent(String content) {
        if (content == null || content.equals("")) {
            return MessageConst.E_NOCONTENT.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }
}



