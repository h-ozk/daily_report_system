package actions;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.servlet.ServletException;

import actions.views.EmployeeView;
import actions.views.ReportView;
import constants.AttributeConst;
import constants.ForwardConst;
import constants.JpaConst;
import constants.MessageConst;
import models.Reaction;
import services.ReactionService;
import services.ReportService;

/**
 * 日報に関する処理を行うActionクラス
 *
 */
public class ReportAction extends ActionBase{

    private ReportService service;
    private ReactionService xService;

    /**
     * メソッドを実行する
     */
    @Override
    public void process() throws ServletException, IOException{
        service = new ReportService();
        xService = new ReactionService();

        //メソッドを実行
        invoke();
        xService.close();
        service.close();
    }

    /**
     * 一覧画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void index() throws ServletException, IOException {

        removeSessionScope(AttributeConst.RXN_REP_ID);

        //指定されたページ数の一覧画面に表示する日報データを取得
        int page = getPage();
        List<ReportView> reports = service.getAllPerPage(page);

        //全日報データの件数を取得
        long reportsCount = service.countAll();

        putRequestScope(AttributeConst.REPORTS, reports); //取得した日報データ
        putRequestScope(AttributeConst.REP_COUNT, reportsCount); //全ての日報データの件数
        putRequestScope(AttributeConst.PAGE, page); //ページ数
        putRequestScope(AttributeConst.MAX_ROW, JpaConst.ROW_PER_PAGE); //1ページに表示するレコードの数

        //セッションにフラッシュメッセージが設定されている場合はリクエストスコープに移し替え、セッションからは削除する
        String flush = getSessionScope(AttributeConst.FLUSH);
        if (flush != null) {
            putRequestScope(AttributeConst.FLUSH, flush);
            removeSessionScope(AttributeConst.FLUSH);
        }

        //一覧画面を表示
        forward(ForwardConst.FW_REP_INDEX);
    }

    /**
     * 新規登録画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void entryNew() throws ServletException, IOException {

        putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン

        //日報情報の空インスタンスに、日報の日付＝今日の日付を設定する
        ReportView rv = new ReportView();
        rv.setReportDate(LocalDate.now());
        putRequestScope(AttributeConst.REPORT, rv); //日付のみ設定済みの日報インスタンス

        //新規登録画面を表示
        forward(ForwardConst.FW_REP_NEW);
    }

    /**
     * 新規登録を行う
     * @throws ServletException
     * @throws IOException
     */
    public void create() throws ServletException, IOException {

        //CSRF対策 tokenのチェック
        if (checkToken()) {

            //日報の日付が入力されていなければ、今日の日付を設定
            LocalDate day = null;
            if (getRequestParam(AttributeConst.REP_DATE) == null
                    || getRequestParam(AttributeConst.REP_DATE).equals("")) {
                day = LocalDate.now();
            } else {
                day = LocalDate.parse(getRequestParam(AttributeConst.REP_DATE));
            }

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //パラメータの値をもとに日報情報のインスタンスを作成する
            ReportView rv = new ReportView(
                    null,
                    ev, //ログインしている従業員を、日報作成者として登録する
                    day,
                    getRequestParam(AttributeConst.REP_TITLE),
                    getRequestParam(AttributeConst.REP_CONTENT),
                    null,
                    null);

            //日報情報登録
            List<String> errors = service.create(rv);

            if (errors.size() > 0) {
                //登録中にエラーがあった場合

                putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv);//入力された日報情報
                putRequestScope(AttributeConst.ERR, errors);//エラーのリスト

                //新規登録画面を再表示
                forward(ForwardConst.FW_REP_NEW);

            } else {
                //登録中にエラーがなかった場合

                //セッションに登録完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_REGISTERED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }

    /**
     * 詳細画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void show() throws ServletException, IOException{
        //idを条件に日報データを取得する
        int employee_id = toNumber(getRequestParam(AttributeConst.REP_ID));
        String sEmpId = getRequestParam(AttributeConst.RXN_REP_ID);
        if(sEmpId != null) {
            employee_id = toNumber(sEmpId);
        }
        ReportView rv = service.findOne(employee_id);

        if(rv == null) {
            //該当の日報データが存在しないはエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);
        }else {
            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ

            //リアクション数をカウント
            long good = xService.countReaction(toNumber(getRequestParam(AttributeConst.REP_ID)), AttributeConst.RXN_TYPE_GOOD.getIntegerValue());
            long bad = xService.countReaction(toNumber(getRequestParam(AttributeConst.REP_ID)), AttributeConst.RXN_TYPE_BAD.getIntegerValue());
            putRequestScope(AttributeConst.RXN_GOOD, good);
            putRequestScope(AttributeConst.RXN_BAD, bad);

            //セッションからログイン中の従業員情報を取得
            EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

            //2つのidからリアクション情報を取得し、既に行ったリアクションがあれば送信
            Reaction x = xService.findOne(ev.getId(), rv.getId());
            if(x != null){
              putRequestScope(AttributeConst.RXN_ALREADY, x.getType());
            }

            //詳細画面を表示
            forward(ForwardConst.FW_REP_SHOW);
        }
    }

    /**
     * 編集画面を表示する
     * @throws ServletException
     * @throws IOException
     */
    public void edit() throws ServletException, IOException{
        //idを条件に日報データを取得する
        ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        if(rv == null || ev.getId() != rv.getEmployee().getId()) {
            //該当の日報データが存在しない、又はログインしている従業員が日報の作成者でない場合はエラー画面を表示
            forward(ForwardConst.FW_ERR_UNKNOWN);
        }else {
            putRequestScope(AttributeConst.TOKEN, getTokenId()); //CSRF対策用トークン
            putRequestScope(AttributeConst.REPORT, rv); //取得した日報データ

            //編集画面を表示
            forward(ForwardConst.FW_REP_EDIT);
        }
    }

    /**
     * 更新を行う
     * @throws ServletException
     * @throws IOException
     */
    public void update() throws ServletException, IOException{
        //CSRF対策tokenのチェック
        if(checkToken()) {
            //idを条件に日報データを取得する
            ReportView rv = service.findOne(toNumber(getRequestParam(AttributeConst.REP_ID)));

            //入力された日報内容を設定する
            rv.setReportDate(toLocalDate(getRequestParam(AttributeConst.REP_DATE)));
            rv.setTitle(getRequestParam(AttributeConst.REP_TITLE));
            rv.setContent(getRequestParam(AttributeConst.REP_CONTENT));

            //日報データを更新する
            List<String> errors = service.update(rv);

            if(errors.size() > 0) {
                //更新中にエラーが発生した場合

                putRequestScope(AttributeConst.TOKEN, getTokenId());    //CSRF対策用トークン
                putRequestScope(AttributeConst.REPORT, rv); //入力された日報情報
                putRequestScope(AttributeConst.ERR, errors);    //エラーのリスト

                //編集画面を再表示
                forward(ForwardConst.FW_REP_EDIT);
            }else {
                //更新中にエラーが無かった場合

                //セッションに更新完了のフラッシュメッセージを設定
                putSessionScope(AttributeConst.FLUSH, MessageConst.I_UPDATED.getMessage());

                //一覧画面にリダイレクト
                redirect(ForwardConst.ACT_REP, ForwardConst.CMD_INDEX);
            }
        }
    }

    public void goodReaction() throws ServletException, IOException{
        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);
        int employee_id = ev.getId();
        int report_id = toNumber(getRequestParam(AttributeConst.REP_ID));

        //リアクションされた日報のidをセッションに設定
        putSessionScope(AttributeConst.RXN_REP_ID, report_id);

        //2つのidからリアクション情報を取得する
        Reaction x = xService.findOne(employee_id, report_id);

        //もし既にリアクションしていたら
        if(x != null) {
            //もし既に行ったリアクションが同一なら
            if(x.getType() == AttributeConst.RXN_TYPE_GOOD.getIntegerValue()) {
                destroyReaction(x);
            }else {
                updateReaction(x, AttributeConst.RXN_TYPE_GOOD.getIntegerValue());
            }

        } else {
            createReaction(employee_id, report_id, AttributeConst.RXN_TYPE_GOOD.getIntegerValue());
        }
    }

    public void badReaction() throws ServletException, IOException{
        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);
        int employee_id = ev.getId();
        int report_id = toNumber(getRequestParam(AttributeConst.REP_ID));

        //リアクションされた日報のidをセッションに設定
        putSessionScope(AttributeConst.RXN_REP_ID, report_id);

        //2つのidからリアクション情報を取得する
        Reaction x = xService.findOne(employee_id, report_id);

        //もし既にリアクションしていたら
        if(x != null) {
            //もし既に行ったリアクションが同一なら
            if(x.getType() == AttributeConst.RXN_TYPE_BAD.getIntegerValue()) {
                destroyReaction(x);
            }else {
                updateReaction(x, AttributeConst.RXN_TYPE_BAD.getIntegerValue());
            }

        } else {
            createReaction(employee_id, report_id, AttributeConst.RXN_TYPE_BAD.getIntegerValue());
        }
    }

    public void createReaction(int employee_id, int report_id, int type) throws ServletException, IOException{
        //セッションからログイン中の従業員情報を取得
        EmployeeView ev = (EmployeeView) getSessionScope(AttributeConst.LOGIN_EMP);

        //パラメータの値を元にリアクションのインスタンスを作成する
        Reaction x = new Reaction(
                null,
                ev.getId(), //ログインしている従業員を、日報作成者として登録する
                toNumber(getRequestParam(AttributeConst.REP_ID)),
                type);

        //日報情報登録
        xService.create(x);

        //詳細画面を再表示
        show();
    }

    public void updateReaction(Reaction x, int type) throws ServletException, IOException{
        //idを条件にリアクション情報を更新
        xService.update(x, type);

      //詳細画面を再表示
        show();
    }

    public void destroyReaction(Reaction x) throws ServletException, IOException{
        //idを条件にリアクション情報を削除
        xService.destroy(x);

        //詳細画面を再表示
        show();
    }
}