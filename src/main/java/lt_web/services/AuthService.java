package lt_web.services;

import lt_web.models.LoginModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;

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
