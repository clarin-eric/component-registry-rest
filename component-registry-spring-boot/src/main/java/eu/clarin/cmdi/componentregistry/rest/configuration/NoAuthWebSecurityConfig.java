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

import com.google.common.collect.ImmutableList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;

/**
 *
 * @author twagoo
 */
@Configuration
@EnableWebSecurity
@Profile({"security-noauth", "default"})
public class NoAuthWebSecurityConfig {
    
    private static final String KEY_ANONYMOUS = "KEY_ANONYMOUS";
    private static final String ANONYMOUS_USER_NAME = "anonymous";
    private static final ImmutableList<GrantedAuthority> ANONYMOUS_AUTHORITIES = ImmutableList.of(new SimpleGrantedAuthority("ANONYMOUS"));
    private static final User ANONYMOUS_PRINCIPAL = new User(ANONYMOUS_USER_NAME, "", ANONYMOUS_AUTHORITIES);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(anonymousAuthFilter(), HeaderWriterFilter.class)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry
                        -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers("/**")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AnonymousAuthenticationFilter anonymousAuthFilter() {
        return new AnonymousAuthenticationFilter(KEY_ANONYMOUS, ANONYMOUS_PRINCIPAL, ANONYMOUS_AUTHORITIES);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(new AnonymousAuthenticationProvider(KEY_ANONYMOUS));
    }
}
