package by.salary.serviceuser.model;

import by.salary.serviceuser.entities.User;
import by.salary.serviceuser.entities.UserAgreement;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAgreementResponseDTO {

    private BigInteger id;
    private BigInteger userId;
    private BigInteger agreementId;
    private String moderatorName;
    private String moderatorComment;
    private BigDecimal count;
    private Time time;
    private BigDecimal currentBaseReward;

    public UserAgreementResponseDTO(UserAgreement userAgreement) {
        this.id = userAgreement.getId();
        this.userId = userAgreement.getUser().getId();
        this.agreementId = userAgreement.getAgreementId();
        this.moderatorName = userAgreement.getModeratorName();
        this.moderatorComment = userAgreement.getModeratorComment();
        this.count = userAgreement.getCount();
        this.time = userAgreement.getTime();
        this.currentBaseReward = userAgreement.getCurrentBaseReward();
    }
}
