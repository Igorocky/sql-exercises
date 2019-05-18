package org.igye.sqlexercises.data;

import fj.data.Validation;
import org.igye.sqlexercises.data.repository.RoleRepository;
import org.igye.sqlexercises.data.repository.UserRepository;
import org.igye.sqlexercises.htmlforms.SessionData;
import org.igye.sqlexercises.model.Role;
import org.igye.sqlexercises.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.igye.sqlexercises.common.OutlineUtils.hashPwd;
import static org.igye.sqlexercises.common.OutlineUtils.map;

@Component
public class UserDao {
    public static final String ADMIN_ROLE_NAME = "ADMIN";

    @Autowired
    private SessionData sessionData;
    @Autowired
    private DaoUtils daoUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public List<User> loadUsers() {
        return doAsAdmin(() -> userRepository.findAll());
    }

    @Transactional
    public User loadUser(UUID id) {
        return doAsAdmin(() -> userRepository.getOne(id));
    }

    @Transactional
    public void createUser(String name, String password, Set<UUID> roles) {
        doAsAdminV(() -> {
            User newUser = new User();
            newUser.setName(name);
            newUser.setPassword(hashPwd(password));
            newUser.setRoles(map(roles, roleRepository::getOne));
            userRepository.save(newUser);
        });
    }

    @Transactional
    public void updateUser(UUID id, Consumer<User> updates) {
        doAsAdminV(() -> {
            User user = userRepository.getOne(id);
            Boolean lockedBefore = user.isLocked();
            updates.accept(user);
            if (sessionData.getCurrentUser().getId().equals(id) && !lockedBefore && user.isLocked()) {
                user.setLocked(false);
            }
        });
    }

    @Transactional
    public Validation<String, Void> changePassword(String oldPassword, String newPassword) {
        User user = userRepository.findById(sessionData.getCurrentUser().getId()).get();
        return Validation.fail("Old password doesn't match.");
    }

    @Transactional
    public List<Role> loadAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional
    public List<Role> loadRoles(Set<UUID> ids) {
        return roleRepository.findAllById(ids);
    }

    @Transactional
    public User loadUserById(UUID id) {
        return doAsAdmin(() -> userRepository.findById(id).get());
    }

    private <T> T doAsAdmin(Supplier<T> supplier) {
        return daoUtils.doAsAdmin(supplier);
    }

    private <T> void doAsAdminV(Runnable runnable) {
        daoUtils.doAsAdminV(runnable);
    }
}
