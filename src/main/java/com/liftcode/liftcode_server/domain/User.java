package com.liftcode.liftcode_server.domain;

import com.liftcode.liftcode_server.domain.gym.Gym;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends VersionedEntity {

    public User(UUID id) {
        super(id);
    }

    @Getter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_gym_id", unique = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Gym defaultGym;

    public void setDefaultGym(Gym gym) {
        if (gym != null && !getId().equals(gym.getUser().getId())) {
            throw new IllegalArgumentException("Default gym must belong to the user");
        }
        defaultGym = gym;
    }
}
