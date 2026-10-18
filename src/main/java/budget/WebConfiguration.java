package budget;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CsrfInterceptor()).addPathPatterns("/**").excludePathPatterns("/app.css","/error","/favicon.ico");
    }
}
