package com.examApplication.examApplication.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.MenuDTO;
import com.examApplication.examApplication.dto.RoleResponseDTO;
import com.examApplication.examApplication.dto.SubMenuDTO;
import com.examApplication.examApplication.entity.menu.Menu;
import com.examApplication.examApplication.entity.menu.RoleMenu;
import com.examApplication.examApplication.entity.menu.SubMenu;
import com.examApplication.examApplication.helpers.UserUtils;
import com.examApplication.examApplication.repository.RoleMenuRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final RoleMenuRepository roleMenuRepository;

    @GetMapping
    public RoleResponseDTO getMenu() {
        List<RoleMenu> roleMenus = roleMenuRepository.findByRole(UserUtils.getUser().getRole());
        return mapRoleMenusToResponse(roleMenus);
    }

    private RoleResponseDTO mapRoleMenusToResponse(List<RoleMenu> roleMenus) {
        if (roleMenus == null || roleMenus.isEmpty())
            return null;

        RoleResponseDTO response = new RoleResponseDTO();
        response.setRole(roleMenus.get(0).getRole().getRoleName());
        Map<Integer, MenuDTO> menuMap = new HashMap<>();

        for (RoleMenu rm : roleMenus) {
            SubMenu sub = rm.getSubMenu();
            Menu menu = sub.getMenu();

            // Add menu if not already added
            MenuDTO menuRes = menuMap.computeIfAbsent(menu.getId(), m -> {
                MenuDTO mr = new MenuDTO();
                mr.setId(menu.getId());
                mr.setName(menu.getName());
                mr.setUrl(menu.getUrl());
                mr.setStyle(menu.getStyle());
                mr.setSubMenu(new ArrayList<>());
                return mr;
            });

            // Add submenu
            SubMenuDTO subRes = new SubMenuDTO();
            subRes.setId(sub.getId());
            subRes.setName(sub.getName());
            subRes.setUrl(sub.getUrl());
            subRes.setStyle(sub.getStyle());
            subRes.setAccessType(rm.getAccessType().name());
            menuRes.getSubMenu().add(subRes);
        }

        response.setMenu(new ArrayList<>(menuMap.values()));
        return response;
    }

}
