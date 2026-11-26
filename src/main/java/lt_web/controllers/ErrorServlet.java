package lt_web.controllers;

import lt_web.utils.Constant;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorServlet implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return Constant.View.ERROR;
    }
}
