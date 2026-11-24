package com.fyf.util;

public class UserContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) { threadLocal.set(userId); }

    public static Long getCurrentUserId() { return threadLocal.get(); }

    public static void remove(){ threadLocal.remove();}
}
