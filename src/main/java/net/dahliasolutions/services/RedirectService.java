package net.dahliasolutions.services;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class RedirectService {
    public void setHistory(HttpSession session, String pathname) {
        session.setAttribute("redirectPath", pathname);
    }

    public String pathName(HttpSession session, String fallBack) {
        try {
            String path = session.getAttribute("redirectPath").toString();
            if (path.isEmpty()) {
                throw new Exception();
            }
            return "redirect:"+path;
        } catch (Exception e) {
            return "redirect:/"+fallBack;
        }
    }


}
