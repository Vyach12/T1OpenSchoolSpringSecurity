package openschool.java.security.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import openschool.java.security.security.UserDetailsServiceImpl;
import openschool.java.security.security.jwt.JwtTokenExtractor;
import openschool.java.security.security.jwt.JwtTokenValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Фильтр для JWT-аутентификации.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    /**
     * Префикс Authorization-хедера.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Класс для работы с JWT-токенами.
     */
    private final JwtTokenExtractor tokenExtractor;

    /**
     * Класс для валидации JWT-токенов.
     */
    private final JwtTokenValidator tokenValidator;

    /**
     * Класс для получения данных о пользователях.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Метод для фильтрации запросов по JWT-токенам.
     *
     * @param request     - запрос
     * @param response    - ответ
     * @param filterChain - цепочка фильтров
     */
    @Override
    @SneakyThrows
    protected void doFilterInternal(final @NonNull HttpServletRequest request,
                                    final @NonNull HttpServletResponse response,
                                    final @NonNull FilterChain filterChain) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());
        String username = tokenExtractor.extractUsername(token);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (tokenValidator.isValid(token, userDetails)) {
                var authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );

                WebAuthenticationDetails details = new WebAuthenticationDetailsSource()
                    .buildDetails(request);
                authenticationToken.setDetails(details);

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
