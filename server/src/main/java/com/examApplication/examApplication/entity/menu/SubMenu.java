package com.examApplication.examApplication.entity.menu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "sub_menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submenu_id")
    private Integer id;
    private String name;
    private String url;
    private String style;

    @ManyToOne
    @JoinColumn(name = "menu_id")
    private Menu menu;
}
