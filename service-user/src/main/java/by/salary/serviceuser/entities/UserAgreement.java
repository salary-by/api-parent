package by.salary.serviceuser.entities;


import by.salary.serviceuser.model.UserAgreementRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;

/**
 * User agreement
 * One table with all user agreements
 * UserAgreement=
 * userId - WHO take the add
 * agreementId - WHAT agreement (class AgreementState - service-agreement module)
 * moderatorName (just Name of user, who assigned the add)
 * moderatorComment (moderator comment, text )
 * count (the count of %)
 * time (when the add was assigned -set in create time)
 * currentBaseReward (current base reward from ORGANISATION property -set in create time)
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    @NotNull
    private BigInteger agreementId;

    @Size(min = 1, max = 50)
    private String moderatorName;
    @Size(min = 1, max = 2000)
    private String moderatorComment;
    @NotNull
    private BigDecimal count;
    @NotNull
    private Time time;
    @NotNull
    private BigDecimal currentBaseReward;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public UserAgreement(UserAgreementRequestDTO userAgreement, User user) {
        this.agreementId = userAgreement.getAgreementId();
        this.moderatorName = userAgreement.getModeratorName();
        this.moderatorComment = userAgreement.getModeratorComment();
        this.count = userAgreement.getCount();
        this.time = new Time(System.currentTimeMillis());
        this.currentBaseReward = user.getOrganisation().getBaseReward();
        this.user = user;
    }
}
