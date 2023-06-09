package com.greedytown.global.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.greedytown.domain.user.model.User;
import com.greedytown.domain.user.repository.UserRepository;
import com.greedytown.global.config.auth.PrincipalDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(JwtProperties.HEADER_STRING);
        if(header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(JwtProperties.HEADER_STRING)
                .replace(JwtProperties.TOKEN_PREFIX, "");
        String username = null;
        try {
            DecodedJWT userJwt = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token);
            username = userJwt.getClaim("username").asString();
        } catch (TokenExpiredException expiredException) {
            response.sendError(401); // 토큰 만료되었으면 401 반환
            return;
        }

        if(username != null) {
            User user = userRepository.findByUserEmail(username);
            PrincipalDetails principalDetails = new PrincipalDetails(user);
            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            principalDetails,
                            null, // 패스워드는 모른다. 인증 용도 x
                            principalDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication); // 권한 관리를 위해 세션에 값 저장
            request.setAttribute("USER", user); // api 호출을 위해 request에 user 정보 저장
        }
        chain.doFilter(request, response);
    }
}
