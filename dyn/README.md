##### 通过数据库中的用户、角色、菜单 进行权限认证
#### 1.自己的User类实现UserDetails接口，覆盖其方法，重写getAuthorities() ，填充用户拥有的角色
```
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    for (Role role : roles) {
        authorities.add(new SimpleGrantedAuthority(role.getName()));
    }
    return authorities;
}
```
#### 2.UserService实现UserDetailsService接口，重写loadUserByUsername方法，提供获取用户，本方法会比上一步的getAuthorities先执行。
```
@Override
public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
    User user = userMapper.loadUserByUsername(s);
    if (user == null) {
        throw new UsernameNotFoundException("用户不存在!");
    }
    user.setRoles(userMapper.getRolesById(user.getId()));
    return user;
}

```
#### 3.提供自己的Filter实现FilterInvocationSecurityMetadataSource接口，根据url或其他条件找出具有权限的目标角色集合Collection<ConfigAttribute>
```
package com.gemini.security_.dyn.config;


import com.gemini.security_.dyn.model.Menu;
import com.gemini.security_.dyn.model.Role;
import com.gemini.security_.dyn.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;
import java.util.List;

@Component
public class MyFilter implements FilterInvocationSecurityMetadataSource {
    AntPathMatcher pathMatcher = new AntPathMatcher();
    @Autowired
    MenuService menuService;

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        List<Menu> allMenus = menuService.getAllMenus();
        for (Menu menu : allMenus) {
            if (pathMatcher.match(menu.getPattern(), requestUrl)) {
                List<Role> roles = menu.getRoles();
                String[] rolesStr = new String[roles.size()];
                for (int i = 0; i < roles.size(); i++) {
                    rolesStr[i] = roles.get(i).getName();
                }
                return SecurityConfig.createList(rolesStr);
            }
        }
        return SecurityConfig.createList("ROLE_login");
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}

```

#### 4.提供权限判断的Manager实现AccessDecisionManager接口，重写decide()方法，判断当前用户是否有权限访问
```java
package com.gemini.security_.dyn.config;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class MyAccessDecisionManager implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object o, Collection<ConfigAttribute> collection) throws AccessDeniedException, InsufficientAuthenticationException {
        for (ConfigAttribute attribute : collection) {
            if ("ROLE_login".equals(attribute.getAttribute())) {
                if (authentication instanceof AnonymousAuthenticationToken) {
                    throw new AccessDeniedException("非法请求!");
                } else {
                    return;
                }
            }
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(attribute.getAttribute())) {
                    return;
                }
            }
        }
        throw new AccessDeniedException("非法请求!");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
```
#### 提供Configuration初始化上述配置
```java
package com.gemini.security_.dyn.config;

import com.gemini.security_.dyn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;
    @Autowired
    MyFilter myFilter;
    @Autowired
    MyAccessDecisionManager myAccessDecisionManager;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }//BCrypt加密同样的明文每次加密结果都不同

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O o) {
                        o.setAccessDecisionManager(myAccessDecisionManager);
                        o.setSecurityMetadataSource(myFilter);
                        return o;
                    }
                })
                .and()
                .formLogin()
                .permitAll()
                .and()
                .csrf().disable();
    }
}

```



##### 测试数据在data.sql中

参照资料：https://blog.csdn.net/zhaoxichen_10/article/details/88713799