package net.dahliasolutions.config;

import lombok.RequiredArgsConstructor;
import net.dahliasolutions.models.AppServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppServer appServer;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(new HandlerConfig());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/")
                .setCacheControl(CacheControl.noCache());
        registry.addResourceHandler("/content/**")
                .addResourceLocations("file:///var/destinyworshipexchange/content/")
                .setCacheControl(CacheControl.noCache());
    }
// file:///Users/afcripe/var/destinyworshipexchange/content/
// file:///var/destinyworshipexchange/content/
}
