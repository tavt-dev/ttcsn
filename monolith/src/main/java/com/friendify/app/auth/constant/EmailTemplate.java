package com.friendify.app.auth.constant;

public final class EmailTemplate {

    private EmailTemplate() {
    }

    public static String otpEmail(String username, String otpCode) {
        return "<p>Hello " + username + ",</p>"
                + "<p>Your Friendify verification code is <strong>" + otpCode + "</strong>.</p>";
    }

    public static String welcomeEmail(String username) {
        return "<p>Hello " + username + ",</p>"
                + "<p>Welcome to Friendify.</p>";
    }

    public static String resendVerificationEmail(String username, String otpCode) {
        return "<p>Hello " + username + ",</p>"
                + "<p>Your new Friendify verification code is <strong>" + otpCode + "</strong>.</p>";
    }

    public static String resetPasswordEmail(String username, String otpCode) {
        return "<p>Hello " + username + ",</p>"
                + "<p>Your Friendify password reset code is <strong>" + otpCode + "</strong>.</p>";
    }
}
