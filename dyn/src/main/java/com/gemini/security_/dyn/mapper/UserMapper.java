package com.gemini.security_.dyn.mapper;


import com.gemini.security_.dyn.model.Role;
import com.gemini.security_.dyn.model.User;

import java.util.List;

public interface UserMapper {

    List<Role> getRolesById(Integer id);

    User loadUserByUsername(String username);
}
