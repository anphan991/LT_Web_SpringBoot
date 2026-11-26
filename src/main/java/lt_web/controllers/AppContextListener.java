package lt_web.controllers;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lt_web.repository.JpaConfig;
import lt_web.utils.ValidationUtil;
import org.springframework.stereotype.Component;

@Component
public class AppContextListener implements ServletContextListener {

    @PreDestroy
    public void onDestroy() {
        JpaConfig.close();
        ValidationUtil.close();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        onDestroy();
    }
}
