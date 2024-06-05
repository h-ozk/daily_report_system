package services;

import javax.persistence.NoResultException;

import constants.JpaConst;
import models.Reaction;

/**
 * リアクションテーブルの操作に関わる処理を行うクラス
 */
public class ReactionService extends ServiceBase{
        /**
         * 指定した日報の指定したリアクションの件数を取得し、返却する
         * @param id 日報のid
         * @param type
         * @return リアクションの件数
         */
        public long countReaction(int id, int type) {
            long count = (long) em.createNamedQuery(JpaConst.Q_RXN_COUNT_REACTION, Long.class)
                    .setParameter(JpaConst.JPQL_PARM_REPORT_ID, id)
                    .setParameter(JpaConst.JPQL_PARM_TYPE, type)
                    .getSingleResult();

            return count;
        }

        /**
         * 指定した従業員と指定した日報から日報データを1件取得し、Reactionのインスタンスで返却する
         * @param employee_id
         * @param report_id
         * @return 取得データのインスタンス
         */
        public Reaction findOne(int employee_id, int report_id) {
            Reaction x;

            try {
                x = em.createNamedQuery(JpaConst.Q_RXN_GET_BY_EMPLOYEE_AND_REPORT, Reaction.class)
                        .setParameter(JpaConst.JPQL_PARM_EMPLOYEE_ID, employee_id)
                        .setParameter(JpaConst.JPQL_PARM_REPORT_ID, report_id)
                        .getSingleResult();

            } catch (NoResultException ex) {
                x = null;
            }

            return x;
        }

        /**
         * 日報データを1件登録する
         * @param x 日報データ
         */
        public void create(Reaction x) {
            em.getTransaction().begin();
            em.persist(x);
            em.getTransaction().commit();
        }

        /**
         * 日報データを1件更新する
         * @param x 日報データ
         * @param type リアクションタイプ
         */
        public void update(Reaction x, int type) {
            em.getTransaction().begin();
            x.setType(type);
            em.getTransaction().commit();
        }

        /**
         * 日報データを1件削除する
         * @param xv 日報データ
         */
        public void destroy(Reaction x) {
            em.getTransaction().begin();
            em.remove(x);
            em.getTransaction().commit();
        }
}