package net.dahliasolutions.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.store.Cart;
import net.dahliasolutions.models.user.User;
import net.dahliasolutions.services.store.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FilterConfig extends OncePerRequestFilter {

    private final CartService cartService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getPrincipal().equals("anonymousUser")) {
            User user = (User) auth.getPrincipal();
            Cart cart = cartService.findById(user.getId());
            request.getSession().setAttribute("cartItemCount", cart.getItemCount());
        }

        if(request.getSession().getAttribute("dateFilter") == null){
            request.getSession().setAttribute("dateFilter", "3M");
        }
        if(request.getSession().getAttribute("cycle") == null){
            request.getSession().setAttribute("cycle", "0");
        }

        // Do Filtering
        filterChain.doFilter(request, response);
    }
}
