package net.dahliasolutions.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Service
public class FilterConfig extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getSession().getAttribute("showSideNav") == null && request.getSession().getAttribute("hideSideNav") == null) {
            request.getSession().setAttribute("showSideNav", true);
        }
        if (request.getSession().getAttribute("storeListGrid") == null) {
            request.getSession().setAttribute("storeListGrid", "list");
        }
        // Do Filtering
        filterChain.doFilter(request, response);
    }
}
