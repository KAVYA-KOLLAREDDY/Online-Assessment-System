package com.examApplication.examApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.auth.Role;
import com.examApplication.examApplication.entity.menu.RoleMenu;
import com.examApplication.examApplication.entity.menu.SubMenu;

public interface RoleMenuRepository extends JpaRepository<RoleMenu, Integer> {
    List<RoleMenu> findByRole(Role role);

    List<RoleMenu> findByRoleAndSubMenu(Role role, SubMenu subMenu);
}
