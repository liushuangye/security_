package com.gemini.security_.dyn.service;

import com.gemini.security_.dyn.mapper.MenuMapper;
import com.gemini.security_.dyn.model.Menu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {
    @Autowired
    MenuMapper menuMapper;
    public List<Menu> getAllMenus() {
        return menuMapper.getAllMenus();
    }
}
