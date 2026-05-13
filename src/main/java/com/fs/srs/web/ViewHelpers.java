package com.fs.srs.web;

import com.fs.srs.domain.Request;
import com.fs.srs.domain.Status;
import com.fs.srs.domain.User;

import java.time.format.DateTimeFormatter;

/**
 * Tiny HTML helpers. Kept deliberately simple — this module could be swapped
 * for a real template engine (JTE, Thymeleaf) later without touching services.
 */
public final class ViewHelpers {

    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ViewHelpers() {}

    /** Escape a user-supplied string so it is safe to drop into HTML. */
    public static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Render the page chrome with the given title, user (nullable) and body. */
    public static String page(String title, User user, String body) {
        String nav = user == null ? "" : """
                <nav>
                  <a href="/requests">Requests</a>
                  %s
                  <span class="spacer"></span>
                  <span class="user">%s (%s)</span>
                  <form method="post" action="/logout" style="display:inline">
                    <button class="link" type="submit">Log out</button>
                  </form>
                </nav>
                """.formatted(
                        "EMP".equals(user.getRole())
                                ? "<a href=\"/requests/new\">New request</a>"
                                : "",
                        esc(user.getFullName()),
                        user.getRole());

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>%s</title>
                  <link rel="stylesheet" href="/static/style.css">
                </head>
                <body>
                  <header><h1>Service Request Management</h1></header>
                  %s
                  <main>%s</main>
                </body>
                </html>
                """.formatted(esc(title), nav, body);
    }

    public static String statusBadge(Status s) {
        String cls = switch (s) {
            case NEW -> "badge-new";
            case ASSIGNED -> "badge-assigned";
            case IN_PROGRESS -> "badge-progress";
            case WAITING_FOR_INFO -> "badge-waiting";
            case RESOLVED -> "badge-resolved";
            case CLOSED -> "badge-closed";
        };
        return "<span class=\"badge " + cls + "\">" + s + "</span>";
    }

    public static String overdueMarker(Request r) {
        return r.isOverdue() ? " <span class=\"overdue\">OVERDUE</span>" : "";
    }
}
