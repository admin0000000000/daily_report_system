package services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import actions.views.EmployeeConverter;
import actions.views.EmployeeView;
import actions.views.ReportConverter;
import actions.views.ReportView;
import constants.JpaConst;
import models.Report;
import models.validators.ReportValidator;



/**
 * 日報テーブルの操作に関わる処理を行うクラス
 */
public class ReportService extends ServiceBase {

    /**
     * 指定した従業員が作成した日報データを、指定されたページ数の一覧画面に表示する分取得しReportViewのリストで返却する
     * @param employee 従業員
     * @param page ページ数
     * @return 一覧画面に表示するデータのリスト
     */
    public List<ReportView> getMinePerPage(EmployeeView employee, int page) {

        List<Report> reports = em.createNamedQuery(JpaConst.Q_REP_GET_ALL_MINE, Report.class)
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(employee))
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();
        return ReportConverter.toViewList(reports);
    }

    /**
     * 指定した従業員が作成した日報データの件数を取得し、返却する
     * @param employee
     * @return 日報データの件数
     */
    public long countAllMine(EmployeeView employee) {

        long count = (long) em.createNamedQuery(JpaConst.Q_REP_COUNT_ALL_MINE, Long.class)
                .setParameter(JpaConst.JPQL_PARM_EMPLOYEE, EmployeeConverter.toModel(employee))
                .getSingleResult();

        return count;
    }


    /**
     * 指定されたページ数の一覧画面に表示する日報データを取得し、ReportViewのリストで返却する
     * @param page ページ数
     * @return 一覧画面に表示するデータのリスト
     */
    public List<ReportView> getAllPerPage(int page) {

        List<Report> reports = em.createNamedQuery(JpaConst.Q_REP_GET_ALL, Report.class)
                .setFirstResult(JpaConst.ROW_PER_PAGE * (page - 1))
                .setMaxResults(JpaConst.ROW_PER_PAGE)
                .getResultList();
        return ReportConverter.toViewList(reports);
    }

    /**
     * 日報テーブルのデータの件数を取得し、返却する
     * @return データの件数
     */
    public long countAll() {
        long reports_count = (long) em.createNamedQuery(JpaConst.Q_REP_COUNT, Long.class)
                .getSingleResult();
        return reports_count;
    }

    /**
     * 日付を条件に取得したデータをReportViewのインスタンスで返却する
     * @param report_date 日付
     */
    public ReportView findOne(LocalDate report_date) {

        return ReportConverter.toView(findOneInternal(report_date));

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
     * 画面から入力された日報の登録内容を元にデータを1件作成し、日報テーブルに登録する
     * @param rv 日報の登録内容
     * @return バリデーションで発生したエラーのリスト
     */
    public List<String> create(ReportView id,ReportView ra,ReportView rb,EmployeeView ev,ReportService service,ReportView rv,LocalDateTime clock_in,LocalDateTime clock_out) {


        //登録済みの日付を取得する
       ReportView savedreport_date = countByreport_Id(rv.getReportDate());

        boolean isReport_Date = false;

      if(id!=null) {

          //日付と従業員IDのチェック
          if (!ev.getId().equals(id.getEmployee().getId())){

             if(savedreport_date.getReportDate().equals(rv.getReportDate())){


              //バリデーションを行わない
              isReport_Date = false;
              }
           }

          else{isReport_Date = true;
          }
      }
        List<String> errors = ReportValidator.validate(id,ra,rb,ev,rv,this, isReport_Date);

        if (errors.size() == 0) {
            LocalDateTime ldt = LocalDateTime.now();
            rv.setCreatedAt(ldt);
            rv.setUpdatedAt(ldt);
            createInternal(rv);
        }

        //バリデーションで発生したエラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * 画面から入力された日報の登録内容を元に、日報データを更新する
     * @param rv 日報の更新内容
     * @return バリデーションで発生したエラーのリスト
     */
    public List<String> update(ReportView id,ReportView ra,ReportView rb,EmployeeView ev,ReportService service,ReportView rv,LocalDateTime clock_out,LocalDateTime clock_in) {


        //登録済みの日付を取得する
       ReportView savedreport_date = findOne(rv.getId());

        boolean isReport_Date = false;

        if(id!=null) {

        //日付と従業員IDのチェック
        if (ev.getId().equals(id.getEmployee().getId())){

           if(savedreport_date.getReportDate().equals(rv.getReportDate())){


            //バリデーションを行わない
            isReport_Date = false;
            }
           //データベースにすでに登録がある場合はtrue
           else if (rv.getReportDate() != null && rv.getEmployee().getId()!= null) {

               isReport_Date = true;

        }
         }

        }
        else {
            //変更後の日報を設定する
            savedreport_date.setReportDate(rv.getReportDate());
        }

        //更新内容についてバリデーションを行う
        List<String> errors = ReportValidator.validate(id,ra,rb,ev,rv, this, isReport_Date);

        if (errors.size() == 0) {

            //更新日時を現在時刻に設定
            LocalDateTime ldt = LocalDateTime.now();
            rv.setUpdatedAt(ldt);

            updateInternal(rv);
        }

        //バリデーションで発生したエラーを返却（エラーがなければ0件の空リスト）
        return errors;
    }

    /**
     * 出勤時刻に該当するデータの件数を取得し、返却する
     * @param clock_in 出勤時刻
     * @return 重複期間
     */

    public boolean betweenafter(ReportView rv,LocalDateTime clock_in,LocalDateTime clock_out) {

      //登録済みの日付を取得する
        ReportView betweenclock_in = findOne(rv.getId());

      //登録済みの期間を返す
        return betweenclock_in.getClock_out().isBefore(clock_in);
    }


    /**
     * 退勤時刻に該当するデータの件数を取得し、返却する
     * @param clock_out 退勤時刻
     * @return 重複期間
     */

    public boolean betweenbefore(ReportView rb,ReportView rv,LocalDateTime clock_out,LocalDateTime clock_in) {

      //登録済みの日付を取得する
      //  ReportView betweenclock_out = findOne(rv.getId());

      //登録済みの期間を返す
        return rb.getClock_in().isAfter(clock_out);
    }

    /**
     * 日付を条件に該当するデータの件数を取得し、返却する
     * @param dete 日付
     * @return 該当するデータの件数
     */
    public long countByreport_Date(LocalDate report_date) {

        //指定した日付を保持する日報の件数を取得する
        long reportcount = (long) em.createNamedQuery(JpaConst.Q_REP_COUNT_REGISTERED_BY_REP_DATE, Long.class)
                .setParameter(JpaConst.JPQL_PARM_REP_DATE, report_date)
                .getSingleResult();
        return reportcount;
    }

    /**
     * 日付を条件に該当するデータの件数を取得し、返却する
     * @param dete 日付
     * @return 該当するデータの件数
     */
    public ReportView countByreport_Id(LocalDate report_date) {

        Report id = null;

      //指定した日付を保持する日報を取得する
        try {
        id =  em.createNamedQuery(JpaConst.Q_REP_GET_REGISTERED_BY_REP_DATE, Report.class)
                .setParameter(JpaConst.JPQL_PARM_REP_DATE, report_date)
                .setMaxResults(1)
                .getSingleResult();


        } catch (Exception e) {
            return null;
        }

        return ReportConverter.toView(id);
    }



    //1日後
    /**
     * 日付を条件に該当するデータの件数を取得し、返却する
     * @param dete 日付
     * @return 該当するデータの件数
     */
    public ReportView countByAfterDate(LocalDate report_date) {

        LocalDate afterreport_date = report_date.plusDays(1);
        Report ra = null;

        //指定した日付を保持する日報を取得する
        try {
        ra =  em.createNamedQuery(JpaConst.Q_REP_GET_REGISTERED_BY_REP_AFTERDATE, Report.class)
                .setParameter(JpaConst.JPQL_PARM_REP_AFTERDATE, afterreport_date)
                .setMaxResults(1)
                .getSingleResult();


        } catch (Exception e) {
            return null;
        }

        return ReportConverter.toView(ra);
    }



    //1日前
    /**
     * 日付を条件に該当するデータの件数を取得し、返却する
     * @param dete 日付
     * @return 該当するデータの件数
     */
    public ReportView countByBeforeDate(LocalDate report_date) {

        LocalDate beforereport_date = report_date.minusDays(1);
        Report rb = null;

        //指定した日付を保持する日報を取得する

        try {
        rb =  em.createNamedQuery(JpaConst.Q_REP_GET_REGISTERED_BY_REP_BEFOREDATE, Report.class)
                .setParameter(JpaConst.JPQL_PARM_REP_BEFOREDATE, beforereport_date)
                .setMaxResults(1)
                .getSingleResult();


        } catch (Exception e) {
            return null;
        }

        return ReportConverter.toView(rb);
    }




    /**
     * 日付を条件にデータを1件取得する
     * @param report_date
     * @return 取得データのインスタンス
     */
    private Report findOneInternal(LocalDate report_date) {
        return em.find(Report.class, report_date);
    }

    /**
     * idを条件にデータを1件取得する
     * @param id
     * @return 取得データのインスタンス
     */
    private Report findOneInternal(int id) {
        return em.find(Report.class, id);
    }




    /**
     * 日報データを1件登録する
     * @param rv 日報データ
     */
    private void createInternal(ReportView rv) {

        em.getTransaction().begin();
        em.persist(ReportConverter.toModel(rv));
        em.getTransaction().commit();

    }

    /**
     * 日報データを更新する
     * @param rv 日報データ
     */
    private void updateInternal(ReportView rv) {

        em.getTransaction().begin();
        Report r = findOneInternal(rv.getId());
        ReportConverter.copyViewToModel(r, rv);
        em.getTransaction().commit();

        }

  }



