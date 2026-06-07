package com.santediagnostics.session;

import com.santediagnostics.models.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(User user) {
        this.currentUser = user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public String getCurrentRole() {
        if (currentUser == null) return null;
        return currentUser.getRole();
    }

    public int getCurrentUserId() {
        if (currentUser == null) return -1;
        return currentUser.getId();
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(getCurrentRole());
    }

    public boolean isLabAttendant() {
        return "LAB_ATTENDANT".equals(getCurrentRole());
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(getCurrentRole());
    }
}