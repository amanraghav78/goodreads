package com.Goodreads.UserService.Auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

public class CustomAuthFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(CustomAuthFilter.class);
    private final AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    @Autowired
    private AdminDetailsService adminDetailsService;

    @Autowired
    private EmployeeDetailsService employeeDetailsService;

    public CustomAuthFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, ServletException, IOException {
        String username = "";
        String password = "";
        UsernamePasswordAuthenticationToken authenticationToken;
        String uri = request.getRequestURI();

        String headerToken = "";
        headerToken = request.getHeader(AUTH_HEADER);
        logger.info("Authorization Header value: " + headerToken);
        if (headerToken == null || (!headerToken.startsWith(AppConstants.BASIC_TOKEN_PREFIX) && !headerToken.startsWith(AppConstants.BEARER_TOKEN_PREFIX))) {
            this.logger.info("No Authorization header found!");
            filterChain.doFilter(request, response);
            return;
        }
        if (headerToken.startsWith(AppConstants.BASIC_TOKEN_PREFIX) && uri.endsWith(AppConstants.SIGN_IN_URI_ENDING)) {
            headerToken = StringUtils.delete(headerToken, AppConstants.BASIC_TOKEN_PREFIX).trim();
            username = JwtUtil.decodedBase64(headerToken)[0];
            password = JwtUtil.decodedBase64(headerToken)[1];
            this.logger.info("Credentials in basic token: username: " + username + " password: " + password);
            authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authenticationResult = null;
            try {
                authenticationResult = this.authenticationManager.authenticate(authenticationToken);

            } catch (AuthenticationException e) {
                logger.info("Error message is " + e.getMessage());

                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.unauthenticated(username, password));
                exceptionResolver.resolveException(request, response, null, e);
            }
            if (authenticationResult != null) {
                SecurityContextHolder.getContext().setAuthentication(authenticationResult);
            }

            filterChain.doFilter(request, response);
        } else if (headerToken.startsWith(AppConstants.BEARER_TOKEN_PREFIX) && !uri.endsWith(AppConstants.SIGN_IN_URI_ENDING)) {
            UserDetails userDetails = null;
            try {
                headerToken = StringUtils.delete(headerToken, AppConstants.BEARER_TOKEN_PREFIX).trim();
                String entityType = JwtUtil.extractEntityType(headerToken);
                logger.info("jwt auth token: " + headerToken + " entityType: " + entityType);
                username = JwtUtil.extractUsername(headerToken);
                logger.info("Username in jwt token: " + username);


                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    if (entityType.equals(AppConstants.ENTITY_TYPE_ADMIN)) {
                        userDetails = this.adminDetailsService.loadUserByUsername(username);
                    } else if (entityType.equals(AppConstants.ENTITY_TYPE_USER)) {
                        userDetails = this.employeeDetailsService.loadUserByUsername(username);
                    }
                    if (userDetails == null) {
                        logger.info("User details is empty: " + userDetails);

                        throw new UsernameNotFoundException("User not found with username: "+username);
                    } else if (JwtUtil.validateToken(headerToken, userDetails)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


                        filterChain.doFilter(request, response);
                    } else {
                        logger.info("Jwt token is invalid!");
                        throw new InvalidTokenInHeaderException("Token validation returned false");
                    }
                } else {
                    logger.info("Token is not correct!");
                    throw new InvalidTokenInHeaderException("Username not found in token");
                }
            } catch (ExpiredJwtException e) {
                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.unauthenticated(userDetails, null));

                exceptionResolver.resolveException(request, response, null, e);
            } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException |
                     InvalidTokenInHeaderException | ResourceNotFoundException e) {
                SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.unauthenticated(userDetails, null));

                exceptionResolver.resolveException(request, response, null, new InvalidTokenInHeaderException(e.getMessage()));
            }
            catch(UsernameNotFoundException e){
                exceptionResolver.resolveException(request,response,null,e);

            }
        }

    }
}
