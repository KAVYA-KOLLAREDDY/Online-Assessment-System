package com.examApplication.examApplication.entity.menu;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Integer id;
    private String name;
    private String url;
    private String style;

    @OneToMany(mappedBy = "menu")
    private List<SubMenu> subMenus;

    public void addSubMenu(SubMenu subMenu) {
        if (this.subMenus == null) {
            this.subMenus = new ArrayList<>();
        }
        this.subMenus.add(subMenu);
    }
}
