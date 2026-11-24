package com.liftcode.liftcode_server.domain.body;

import com.liftcode.liftcode_server.domain.User;
import com.liftcode.liftcode_server.domain.VersionedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user_body_profiles", check = @CheckConstraint(name = "body_profile_complete",
        constraint = "num_nonnulls(sex, birth_year, height_cm, weight_kg) in (0, 4)"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBodyProfile extends VersionedEntity {

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(check = @CheckConstraint(constraint = "birth_year between 1900 and extract(year from current_date)"))
    private Integer birthYear;

    @Column(check = @CheckConstraint(constraint = "height_cm between 80 and 250"))
    private Double heightCm;

    @Column(check = @CheckConstraint(constraint = "weight_kg > 0 and weight_kg <= 300"))
    private Double weightKg;

    public UserBodyProfile(@NonNull User user) {
        super(user.getId());
        this.user = user;
    }

    public UserBodyProfile(User user, @NonNull Sex sex, int birthYear, double heightCm, double weightKg) {
        this(user);
        this.sex = sex;
        this.birthYear = birthYear;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    public enum Sex {
        FEMALE, MALE
    }
}
