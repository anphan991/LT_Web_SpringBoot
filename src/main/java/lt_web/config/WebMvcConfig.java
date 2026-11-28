package lt_web.config;

import jakarta.servlet.DispatcherType;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;
import lt_web.controllers.AuthorizationFilter;
import lt_web.controllers.CsrfFilter;
import lt_web.controllers.RememberMeFilter;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setContentType("text/html;charset=UTF-8");
        resolver.setViewClass(JstlView.class);
        resolver.setAlwaysInclude(true);
        resolver.setOrder(0);
        return resolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/", "/assets/");
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            File docRoot = resolveResourcesDocumentRoot();
            if (docRoot != null) {
                factory.setDocumentRoot(docRoot);
            }
            factory.addContextCustomizers(context -> {
                context.setAllowCasualMultipartParsing(true);
            });
        };
    }

    private File resolveResourcesDocumentRoot() {
        File directSrcResources = new File("src/main/resources");
        if (new File(directSrcResources, "WEB-INF").isDirectory()) {
            return directSrcResources.getAbsoluteFile();
        }
        try {
            URL webInfUrl = WebMvcConfig.class.getClassLoader().getResource("WEB-INF");
            if (webInfUrl != null && "file".equalsIgnoreCase(webInfUrl.getProtocol())) {
                File webInfDir = new File(URI.create(webInfUrl.toString()));
                File classesDir = webInfDir.getParentFile();
                if (classesDir != null) {
                    File targetDir = classesDir.getParentFile();
                    File projectRoot = targetDir != null ? targetDir.getParentFile() : null;
                    if (projectRoot != null) {
                        File projectSrcResources = new File(projectRoot, "src/main/resources");
                        if (new File(projectSrcResources, "WEB-INF").isDirectory()) {
                            return projectSrcResources.getAbsoluteFile();
                        }
                    }
                    if (classesDir.isDirectory()) {
                        return classesDir.getAbsoluteFile();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Bean
    public FilterRegistrationBean<RememberMeFilter> rememberMeFilterRegistration(RememberMeFilter filter) {
        FilterRegistrationBean<RememberMeFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("RememberMeFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilterRegistration(CsrfFilter filter) {
        FilterRegistrationBean<CsrfFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("CsrfFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilterRegistration(AuthorizationFilter filter) {
        FilterRegistrationBean<AuthorizationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("AuthorizationFilter");
        registration.addUrlPatterns(
                "/admin/*",
                "/admin",
                "/manager/*",
                "/home",
                "/profile",
                "/video",
                "/video/*",
                "/video-file"
        );
        registration.setOrder(3);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ConfigurableSiteMeshFilter> siteMeshFilterRegistration() {
        FilterRegistrationBean<ConfigurableSiteMeshFilter> registration =
                new FilterRegistrationBean<>(new SpringSiteMeshFilter());
        registration.setName("sitemesh");
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registration.setOrder(4);
        return registration;
    }

    public static class SpringSiteMeshFilter extends ConfigurableSiteMeshFilter {
        @Override
        protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
            builder.setDecoratorPrefix("/WEB-INF/decorators/")
                    .addDecoratorPath("/*", "web.jsp")
                    .addDecoratorPath("/admin/*", "admin.jsp")
                    .addDecoratorPath("/login", "login.jsp")
                    .addDecoratorPath("/register", "login.jsp")
                    .addDecoratorPath("/forgot-password", "login.jsp")
                    .addDecoratorPath("/verify-otp", "login.jsp")
                    .addDecoratorPath("/reset-password", "login.jsp")
                    .addExcludedPath("/assets/*")
                    .addExcludedPath("/api*")
                    .addExcludedPath("/image")
                    .addExcludedPath("/image*")
                    .addExcludedPath("/video-file")
                    .addExcludedPath("/video-file*");
        }
    }
}
