package com.daox.online.config;

import com.daox.online.entity.mysql.Users;
import com.daox.online.entity.RestBean;
import com.daox.online.entity.views.responseVO.user.UsersTokenVo;
import com.daox.online.filter.JwtAuthenticationFilter;
import com.daox.online.filter.RequestLogFilter;
import com.daox.online.service.SysUserService;
import com.daox.online.uilts.constant.Const;
import com.daox.online.uilts.JwtUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;

@Slf4j
@Configuration
public class SecurityConfiguration {


    @Resource
    JwtUtils jwtUtils;
    @Resource
    SysUserService sysUserService;
    @Resource
    RequestLogFilter requestLogFilter;
    @Resource
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(conf -> conf
                        // 这是最关键的修改：明确允许所有的OPTIONS预检请求，以便它们能到达我们的CorsFilter
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**",
                                "/api/public/**",
                                "/ws/**",
                                "/single", // <--- 在这里添加对 /single 端点的放行
                                "/favicon.ico", // 忽略 favicon.ico 请求 favicon.ico 是浏览器用来请求网站图标
                                "/api/student/learning/files/**"

                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole(Const.ROLE_ADMIN)
                        .requestMatchers("/api/student/**").hasRole(Const.ROLE_STUDENTS)
                        .requestMatchers("/api/teacher/**").hasRole(Const.ROLE_TEACHERS)
                        .anyRequest().authenticated()
                )
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter out = response.getWriter();
                            User user = (User) authentication.getPrincipal(); // 获取用户信息
                            Users users = sysUserService.findUserByUsernameOrEmail(user.getUsername());
                            String token;
                            if (users != null) {
                                token = jwtUtils.createJwt(
                                        user,
                                        users.getNickname(),
                                        users.getId(),
                                        users.getRole(),
                                        users.getIdentifier()
                                );
                                log.info("登录成功！用户名：{}，角色：{}，token：{}",
                                        users.getNickname(),
                                        users.getRole(),
                                        token);
                                out.write(RestBean.success(
                                        new UsersTokenVo()
                                                .setUsername(users.getNickname())
                                                .setRole(users.getRole())
                                                .setIdentifier(users.getIdentifier())
                                                .setAvatarUrl(users.getAvatarUrl())
                                                .setToken(token)
                                                .setExpire(jwtUtils.expireTime())
                                ).asJsonString());
                            } else {
                                out.write(RestBean.failure(400, "用户名或密码错误").asJsonString());
                            }
                        })
                        .failureHandler((request,
                                         response,
                                         exception) -> {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter out = response.getWriter();
                            out.write(RestBean.unauthorized(exception.getMessage()).asJsonString());
                        })
                        .permitAll() // 允许所有用户访问登录页面
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request,
                                               response,
                                               authentication) -> {
                            response.setContentType("application/json;charset=utf-8");
                            PrintWriter out = response.getWriter();
                            // String operator = sensitiveOperationLogService.getUsername();
                            // 对应的jwt令牌被列入黑名单不在使用
                            String authorization = request.getHeader("Authorization"); // 获取请求头中的 Authorization 字段
                            if (jwtUtils.invalidateJwt(authorization)) {
                                // sensitiveOperationLogService.recordSensitiveOperation("退出登录", "用户: " + operator, "Authorization|[销毁]:" + authorization, "退出登录成功");
                                out.write(RestBean.success("退出登录成功").asJsonString());
                                return;
                            }
                            out.write(RestBean.failure(400, "退出登录失败").asJsonString());
                        })
                )
                .sessionManagement(conf -> conf
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 添加自定义的RequestLogFilter过滤器 在UsernamePasswordAuthenticationFilter之前执行 用于记录请求日志
                .addFilterBefore(requestLogFilter, UsernamePasswordAuthenticationFilter.class)
                // 添加自定义的JwtAuthenticationFilter过滤器 用于对请求头中的Jwt令牌进行校验 并将用户信息存入SecurityContextHolder中 并将用户ID存入请求对象属性中 用于后续使用
                .addFilterBefore(jwtAuthenticationFilter, RequestLogFilter.class)
                .build();
    }
}