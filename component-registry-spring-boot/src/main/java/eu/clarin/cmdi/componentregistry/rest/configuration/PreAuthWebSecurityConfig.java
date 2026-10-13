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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;

/**
 *
 * @author twagoo
 */
@Configuration
@EnableWebSecurity
@Profile("security-preauthenticated")
public class PreAuthWebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //                .cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(requestHeaderAuthenticationFilter(), HeaderWriterFilter.class)
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry
                        -> authorizationManagerRequestMatcherRegistry
                        .requestMatchers("/**")
                        .hasAuthority("REGISTRY")
                        .requestMatchers("/**")
                        .permitAll());

        return http.build();
    }

    @Bean
    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        final RequestHeaderAuthenticationFilter authFilter = new RequestHeaderAuthenticationFilter();
        authFilter.setPrincipalRequestHeader("x-auth-principal");
        authFilter.setExceptionIfHeaderMissing(false);
        authFilter.setAuthenticationManager(authenticationManager());
        return authFilter;
    }

    @Bean
    public AuthenticationUserDetailsService userDetailsService() {
        AuthenticationUserDetailsService userDetailsService = (token) -> {
            if (token.getPrincipal() instanceof String principalName) {
                return new User(principalName, "", ImmutableList.of(new SimpleGrantedAuthority("REGISTRY")));
            } else {
                throw new UsernameNotFoundException("No username");
            }
        };
        return userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        final PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                return super.authenticate(authentication); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
            }

        };
        provider.setPreAuthenticatedUserDetailsService(userDetailsService());
        return new ProviderManager(provider);
    }
}
