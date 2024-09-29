package models.validators;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import actions.views.EmployeeView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.MessageConst;
import models.Report;
import services.ReportService;
import utils.DBUtil;

/**
 * 日報インスタンスの各項目についてバリデーションを行う
 * @param rv 日報インスタンス
 * @return エラーのリスト
 * @param service 呼び出し元Serviceクラスのインスタンス
 * @param report_dateDuplicateCheckFlag 日付の重複チェックを実施するかどうか(実施する:true 実施しない:false)
 */

public class ReportValidator {

    public static List<String> validate(ReportView id,ReportView ra,ReportView rb,EmployeeView ev,ReportView rv,ReportService service, Boolean report_dateDuplicateCheckFlag) {
        List<String> errors = new ArrayList<String>();

        //日付のチェック
        String report_dateError = validatereport_Date(id,service, rv.getReportDate(), report_dateDuplicateCheckFlag);
        if (!report_dateError.equals("")) {
            errors.add(report_dateError);
        }


        //タイトルのチェック
        String titleError = validateTitle(rv.getTitle());
        if (!titleError.equals("")) {
            errors.add(titleError);
        }

        //出勤日時のチェック
        String clock_inError = validateClock_in(rb,rv, ev, service,  rv.getReportDate(),rv.getClock_in() ,rv.getClock_out());
        if (!clock_inError.equals("")) {
            errors.add(clock_inError);
        }

        //退勤日時のチェック
        String clock_outError = validateClock_out(ra,rv, ev, service,  rv.getReportDate(),rv.getClock_in(), rv.getClock_out());
        if (!clock_outError.equals("")) {
            errors.add(clock_outError);
        }

        //退勤日時のチェック
        String clock_CompareError = validateClock_compare(rv.getClock_in(), rv.getClock_out());
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
    private static String validatereport_Date(ReportView id,ReportService service, LocalDate report_date,Boolean report_dateDuplicateCheckFlag) {

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
    private static String validateClock_in(ReportView rb,ReportView rv,EmployeeView ev,ReportService service,LocalDate report_date,LocalDateTime clock_in, LocalDateTime clock_out) {



        if (clock_in == null || clock_in.equals("")) {
            return MessageConst.E_NOCLOCK_IN.getMessage();
        }

        LocalDateTime clock_indate =clock_in;
        LocalDate isclock_indate = LocalDate.of(clock_indate.getYear(), clock_indate.getMonth(), clock_indate.getDayOfMonth());


       if(rb!=null) {

           if (ev.getId().equals(rb.getEmployee().getId())){
            if (rv.getClock_in().isBefore(rb.getClock_out())) {
            return MessageConst.E_CLOCK_IN_EXIST.getMessage();
                }
           }
       }

        //日報作成日と出勤日を比較し、エラーチェック
      if(!report_date.isEqual(isclock_indate)) {
            return MessageConst.E_CLOCK_IN_EXIST.getMessage();
        }

        //入力値がある場合は空文字を返却
        return "";
    }

    /**
     * 退勤日時に入力値があるかをチェックし、入力値がなければエラーメッセージを返却
     * @param clock_out 退勤日時
     * @return エラーメッセージ
     */

    private static String validateClock_out(ReportView ra,ReportView rv,EmployeeView ev, ReportService service,LocalDate report_date, LocalDateTime clock_in, LocalDateTime clock_out) {

        if (clock_out == null || clock_out.equals("")) {
            return MessageConst.E_NOCLOCK_OUT.getMessage();
        }

        LocalDateTime clock_outdate =clock_out;
        LocalDate isclock_outdate = LocalDate.of(clock_outdate.getYear(), clock_outdate.getMonth(), clock_outdate.getDayOfMonth());
        LocalDate afterclock_outdate = isclock_outdate.minusDays(1);

        if(ra!=null) {

            if (ev.getId().equals(ra.getEmployee().getId())){
            if (rv.getClock_out().isAfter(ra.getClock_in())) {
            return MessageConst.E_CLOCK_OUT_EXIST.getMessage();
                    }
                }
        }

        if(!report_date.isEqual(isclock_outdate)) {
            if(!report_date.isEqual(afterclock_outdate)) {
            return MessageConst.E_CLOCK_OUT_EXIST.getMessage();

            }
        }
        //入力値がある場合は空文字を返却
        return "";

    }

    /**
     * 退勤時刻より出勤時刻が大きければエラーメッセージを返却
     * @param clock_Compare 時刻比較
     * @return エラーメッセージ
     */

    private static String validateClock_compare(LocalDateTime clock_in, LocalDateTime clock_out) {
        if (clock_in != null && clock_out != null) {
            if (clock_in.compareTo(clock_out) > 0) {
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

    /**
     * idを条件に取得したデータをReportViewのインスタンスで返却する
     * @param id
     * @return 取得データのインスタンス
     */
    public ReportView findOne(int id) {
        return ReportConverter.toView(findOneInternal(id));
    }

    /**
     * idを条件にデータを1件取得する
     * @param id
     * @return 取得データのインスタンス
     */
    public Report findOneInternal(int id) {
        return DBUtil.createEntityManager().find(Report.class, id);
    }

}