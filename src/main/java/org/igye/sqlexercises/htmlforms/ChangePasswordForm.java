package org.igye.sqlexercises.htmlforms;

import lombok.Data;

@Data
public class ChangePasswordForm {
    private String oldPassword;
    private String newPassword1;
    private String newPassword2;
    private String errorMsg;
}
