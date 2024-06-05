package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import constants.JpaConst;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * リアクションデータのDTOモデル
 */
@Table(name = JpaConst.TABLE_RXN)
@NamedQueries({
    @NamedQuery(
            name = JpaConst.Q_RXN_COUNT_REACTION,
            query = JpaConst.Q_RXN_COUNT_REACTION_DEF),
    @NamedQuery(
            name = JpaConst.Q_RXN_GET_BY_EMPLOYEE_AND_REPORT,
            query = JpaConst.Q_RXN_GET_BY_EMPLOYEE_AND_REPORT_DEF)
})

@Getter //全てのクラスフィールドについてgetterを自動生成する（Lombok）
@Setter //全てのクラスフィールドについてsetterを自動生成する（Lombok）
@NoArgsConstructor  //引数なしコンストラクタを自動生成する（Lombok）
@AllArgsConstructor //全てのクラスフィールドを引数に持つ引数ありコンストラクタを自動生成する（Lombok）
@Entity

public class Reaction {
    /**
     * id
     */
    @Id
    @Column(name = JpaConst.RXN_COL_ID)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * リアクションした社員のid
     */
    @Column(name = JpaConst.RXN_COL_EMP, nullable = false)
    private Integer employeeId;

    /**
     * リアクションされた日報のid
     */
    @Column(name = JpaConst.RXN_COL_REP, nullable = false)
    private Integer reportId;

    /**
     * リアクションの種類
     */
    @JoinColumn(name = JpaConst.RXN_COL_TYPE, nullable = false)
    private Integer type;
}
