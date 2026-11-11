/*
 * Copyright (C) 2026 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.componentregistry.rest.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Accepts all usernames as long as the general access key is provided as a
 * password
 *
 * ONLY USE IN SECURE CONTEXTS, USERNAME IS ACCEPTED WITHOUT VERIFICATION!
 *
 * @author twagoo
 */
@Configuration
@EnableWebSecurity
@Profile("security-basicauth")
class PassthroughBasicAuthWebSecurityConfig {

    @Value("${compreg.api.security.preauth.preAuthAccessKey}")
    private String preAuthAccessKey;

    @Value("${compreg.api.security.preauth.enabled}")
    private boolean enabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(a
                -> a.requestMatchers("/registry/**").permitAll()
                        .requestMatchers("/user", "/user/**").authenticated())
                .httpBasic(Customizer.withDefaults());   // use HTTP Basic auth

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                if (enabled) {
                    return User.withUsername(username).password(encoder.encode(preAuthAccessKey)).build();
                } else {
                    throw new UsernameNotFoundException("Pass-through basic auth is not enabled");
                }
            }

        };

    }

}
