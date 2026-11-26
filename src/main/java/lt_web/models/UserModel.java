package lt_web.models;

import lt_web.entity.User;

@SuppressWarnings("serial")
public class UserModel extends User {
    public UserModel() {
        super();
    }

    public UserModel(String username, String password) {
        super();
        setUserName(username);
        setPassWord(password);
    }
}
