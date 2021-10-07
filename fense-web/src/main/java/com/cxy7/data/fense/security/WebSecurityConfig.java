package com.cxy7.data.fense.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: XiaoYu
 * @Date: 2021/3/21 0:10
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private FenseUserDetailsService fenseUserDetailsService;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(fenseUserDetailsService)
            .passwordEncoder(passwordEncoderBean());
    }

    @Bean
    public PasswordEncoder passwordEncoderBean() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // we don't need CSRF because our token is invulnerable
            .csrf().disable()

            .formLogin()
            .loginPage("/login")
            .successForwardUrl("/index")
            .failureForwardUrl("/login")
            .and()

            .authorizeRequests()

            // Un-secure H2 Database
            .antMatchers("/user/login", "/user/register", "/user/iforgot", "/user/resetPassword", "/user/resetPassword2").permitAll()

            .antMatchers("/login", "/register", "/confirm_email", "/iforgot", "/iforgot_confirm").permitAll()
            .antMatchers(
                    HttpMethod.GET,
                    "/favicon.ico",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js",
                    "/font-awesome/**",
                    "/img/**"
            ).permitAll()
            .anyRequest().authenticated();

        // disable page caching
        http
            .headers()
            .frameOptions().sameOrigin()  // required to set for H2 else H2 Console will be blank.
            .cacheControl();
    }

}
