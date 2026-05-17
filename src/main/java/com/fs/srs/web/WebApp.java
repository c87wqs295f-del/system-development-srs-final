package com.fs.srs.web;

import com.fs.srs.domain.User;
import com.fs.srs.repository.UserRepository;
import com.fs.srs.service.AuthService;
import com.fs.srs.service.RequestService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;

/**
 * Javalin application bootstrap: wires routes to controllers and sets up the "require login" filter.
 */
public class WebApp {

    private final AuthController authController;
    private final RequestController requestController;
    private final UserRepository users;

    public WebApp(AuthService auth, RequestService requests, UserRepository users) {
        this.authController = new AuthController(auth);
        this.requestController = new RequestController(requests, users);
        this.users = users;
    }

    public Javalin build() {
        Javalin app = Javalin.create(cfg -> {
            cfg.staticFiles.add(sf -> {
                sf.hostedPath = "/static";
                sf.directory = "/static";
                sf.location = Location.CLASSPATH;
            });
            cfg.showJavalinBanner = false;
        });

        // Require login on everything except /login and /static/*
        app.before(ctx -> {
            String path = ctx.path();
            if (path.equals("/login") || path.startsWith("/static")) return;
            Long userId = ctx.sessionAttribute(AuthController.SESSION_USER_ID);
            if (userId == null) {
                ctx.redirect("/login");
            }
        });

        app.get("/",          ctx -> ctx.redirect("/requests"));
        app.get("/login",     authController::showLogin);
        app.post("/login",    authController::handleLogin);
        app.post("/logout",   authController::handleLogout);

        app.get("/requests",                   requestController::list);
        app.get("/requests/new",               requestController::showNewForm);
        app.post("/requests",                  requestController::createRequest);
        app.get("/requests/{id}",              requestController::showDetail);
        app.post("/requests/{id}/comment",     requestController::addComment);
        app.post("/requests/{id}/transition",  requestController::transition);
        app.post("/requests/{id}/assign",      requestController::assign);
        app.post("/requests/{id}/priority",    requestController::changePriority);

        return app;
    }

    /**
     * Pulls the logged-in user from the session.
     */
    public User getUserFromSession(Context ctx) {
        Long userId = ctx.sessionAttribute(AuthController.SESSION_USER_ID);
        if (userId == null) {
            ctx.redirect("/login");
            return null;
        }
        return users.findById(userId).orElse(null);
    }

    /** Convenience static accessor used by controllers, set by com.fs.srs.Main. */
    private static WebApp INSTANCE;
    public static void setInstance(WebApp app) { INSTANCE = app; }
    public static User requireUser(Context ctx) {
        if (INSTANCE == null) throw new IllegalStateException("WebApp not initialized");
        User u = INSTANCE.getUserFromSession(ctx);
        if (u == null) throw new IllegalStateException("Expected authenticated user");
        return u;
    }
}
