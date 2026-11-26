package lt_web.services;

import lt_web.models.LoginModel;
import lt_web.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;

    public AuthService() {
        this(new UserServiceImpl());
    }

    @Autowired
    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public boolean isValid(LoginModel login) {
        if (login == null) {
            return false;
        }
        return userService.login(login.username(), login.password()) != null;
    }
}
