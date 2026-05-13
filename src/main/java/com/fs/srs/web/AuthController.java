package com.fs.srs.web;

import com.fs.srs.domain.User;
import com.fs.srs.exceptions.AuthenticationException;
import com.fs.srs.service.AuthService;
import io.javalin.http.Context;

import static com.fs.srs.web.ViewHelpers.esc;
import static com.fs.srs.web.ViewHelpers.page;

/** HTTP handlers for /login and /logout. */
public class AuthController {

    public static final String SESSION_USER_ID = "userId";

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    public void showLogin(Context ctx) {
        String error = ctx.queryParam("error");
        String form = """
                <div class="card">
                  <h2>Log in</h2>
                  %s
                  <form method="post" action="/login">
                    <label>Username <input name="username" autofocus required></label>
                    <label>Password <input name="password" type="password" required></label>
                    <button type="submit">Log in</button>
                  </form>
                  <p class="hint">Demo users are listed in the README.</p>
                </div>
                """.formatted(error == null ? "" : "<p class=\"error\">" + esc(error) + "</p>");
        ctx.html(page("Log in", null, form));
    }

    public void handleLogin(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        try {
            User user = auth.login(username, password);
            ctx.sessionAttribute(SESSION_USER_ID, user.getId());
            ctx.redirect("/requests");
        } catch (AuthenticationException e) {
            ctx.redirect("/login?error=" + java.net.URLEncoder.encode(e.getMessage(),
                    java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public void handleLogout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/login");
    }
}
