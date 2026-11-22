package com.liftcode.liftcode_server.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends VersionedEntity {
}
