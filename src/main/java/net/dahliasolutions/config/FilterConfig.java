package net.dahliasolutions.config;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.Cart;
import net.dahliasolutions.models.User;
import net.dahliasolutions.services.CartService;
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
        // set default sidebar nav
        if (request.getSession().getAttribute("showSideNav") == null && request.getSession().getAttribute("hideSideNav") == null) {
            request.getSession().setAttribute("showSideNav", true);
        }
        // set default list-grid display for store
        if (request.getSession().getAttribute("storeListGrid") == null) {
            request.getSession().setAttribute("storeListGrid", "list");
        }
        // get the cart item count for store
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getPrincipal().equals("anonymousUser")) {
            User user = (User) auth.getPrincipal();
            Cart cart = cartService.findById(user.getId());
            request.getSession().setAttribute("cartItemCount", cart.getItemCount());
        }

        // Do Filtering
        filterChain.doFilter(request, response);
    }
}
