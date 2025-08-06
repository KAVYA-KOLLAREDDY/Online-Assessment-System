package com.examApplication.examApplication.entity.menu;

import com.examApplication.examApplication.entity.auth.Role;
import com.examApplication.examApplication.model.AccessType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "submenu_id")
    private SubMenu subMenu;

    @Enumerated(EnumType.STRING)
    private AccessType accessType;
}
