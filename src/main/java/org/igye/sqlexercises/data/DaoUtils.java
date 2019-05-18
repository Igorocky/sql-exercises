package org.igye.sqlexercises.data;

import org.igye.sqlexercises.common.OutlineUtils;
import org.igye.sqlexercises.htmlforms.SessionData;
import org.igye.sqlexercises.model.Role;
import org.igye.sqlexercises.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class DaoUtils {
    @Autowired
    private SessionData sessionData;

    public <T> T doAsAdmin(Supplier<T> supplier) {
        if (!isAdmin(sessionData.getCurrentUser())) {
            return OutlineUtils.accessDenied();
        } else {
            return supplier.get();
        }
    }

    public <T> void doAsAdminV(Runnable runnable) {
        doAsAdmin(() -> {
            runnable.run();
            return null;
        });
    }


    public boolean isAdmin(User user) {
        for (Role role : user.getRoles()) {
            if (UserDao.ADMIN_ROLE_NAME.equals(role.getName())) {
                return true;
            }
        }
        return false;
    }
}
