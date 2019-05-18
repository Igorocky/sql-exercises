package org.igye.sqlexercises.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.igye.sqlexercises.common.OutlineUtils.UUID_CHAR;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User {
    @Id
    @Type(type = UUID_CHAR)
    private UUID id = UUID.randomUUID();

    @NotNull
    @Column(unique = true, nullable = false)
    private String name;
    @NotNull
    private String password;

    private boolean locked = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLE")
    private Set<Role> roles = new HashSet<>();
}
