package budget;
import jakarta.servlet.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.web.servlet.HandlerInterceptor;
/** Every state-changing form needs the token issued to its browser session. */
public final class CsrfInterceptor implements HandlerInterceptor {
    @Override public boolean preHandle(HttpServletRequest request,HttpServletResponse response,Object handler) throws Exception {
        HttpSession session=request.getSession();
        String token=(String)session.getAttribute("csrfToken");
        if (token==null) { token=UUID.randomUUID().toString();session.setAttribute("csrfToken",token); }
        request.setAttribute("csrfToken",token);
        if ("POST".equals(request.getMethod())) {
            String submitted=request.getParameter("_csrf");
            if (submitted==null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),submitted.getBytes(StandardCharsets.UTF_8))) {
                response.setStatus(403);request.setAttribute("message","This form has expired. Open the planner again and retry your change.");
                request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request,response);return false;
            }
        }
        return true;
    }
}
