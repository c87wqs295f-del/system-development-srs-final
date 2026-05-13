package com.fs.srs.web;

import com.fs.srs.domain.Category;
import com.fs.srs.domain.Comment;
import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Manager;
import com.fs.srs.domain.Priority;
import com.fs.srs.domain.Request;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.Status;
import com.fs.srs.domain.User;
import com.fs.srs.exceptions.AuthorizationException;
import com.fs.srs.exceptions.InvalidStatusTransitionException;
import com.fs.srs.exceptions.ValidationException;
import com.fs.srs.repository.UserRepository;
import com.fs.srs.service.RequestService;
import io.javalin.http.Context;

import java.util.List;

import static com.fs.srs.web.ViewHelpers.FMT;
import static com.fs.srs.web.ViewHelpers.esc;
import static com.fs.srs.web.ViewHelpers.overdueMarker;
import static com.fs.srs.web.ViewHelpers.page;
import static com.fs.srs.web.ViewHelpers.statusBadge;

/** HTTP handlers for /requests... routes. */
public class RequestController {

    private final RequestService service;
    private final UserRepository users;

    public RequestController(RequestService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    /* ============== LIST ============== */

    public void list(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        List<Request> all = service.listFor(actor);

        StringBuilder rows = new StringBuilder();
        if (all.isEmpty()) {
            rows.append("<tr><td colspan=\"7\" class=\"empty\">No requests yet.</td></tr>");
        } else {
            for (Request r : all) {
                rows.append("""
                        <tr>
                          <td>#%d</td>
                          <td><a href="/requests/%d">%s</a>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                          <td>%s</td>
                        </tr>
                        """.formatted(
                        r.getId(), r.getId(), esc(r.getTitle()), overdueMarker(r),
                        r.getCategory(),
                        r.getPriority(),
                        statusBadge(r.getStatus()),
                        esc(r.getSubmitter().getFullName()),
                        r.getAssignee() == null ? "—" : esc(r.getAssignee().getFullName())
                ));
            }
        }

        String body = """
                <div class="card">
                  <h2>Requests visible to you</h2>
                  <table>
                    <thead>
                      <tr><th>ID</th><th>Title</th><th>Category</th><th>Priority</th>
                          <th>Status</th><th>Submitter</th><th>Assignee</th></tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                </div>
                """.formatted(rows);
        ctx.html(page("Requests", actor, body));
    }

    /* ============== NEW (form + submit) ============== */

    public void showNewForm(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        if (!(actor instanceof Employee)) {
            ctx.status(403).html(page("Forbidden", actor,
                    "<p class=\"error\">Only employees can submit requests.</p>"));
            return;
        }
        String body = """
                <div class="card">
                  <h2>New request</h2>
                  <form method="post" action="/requests">
                    <label>Title <input name="title" maxlength="120" required></label>
                    <label>Category
                      <select name="category" required>
                        %s
                      </select>
                    </label>
                    <label>Priority
                      <select name="priority" required>
                        %s
                      </select>
                    </label>
                    <label>Description
                      <textarea name="description" rows="6" required></textarea>
                    </label>
                    <button type="submit">Submit</button>
                  </form>
                </div>
                """.formatted(options(Category.values()), options(Priority.values()));
        ctx.html(page("New request", actor, body));
    }

    public void createRequest(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        try {
            String catStr = ctx.formParam("category");
            String priStr = ctx.formParam("priority");
            if (catStr == null || priStr == null) {
                throw new ValidationException("Category and priority are required");
            }
            Category cat = Category.valueOf(catStr);
            Priority pri = Priority.valueOf(priStr);
            Request r = service.createRequest(actor,
                    ctx.formParam("title"),
                    ctx.formParam("description"),
                    cat, pri);
            ctx.redirect("/requests/" + r.getId());
        } catch (ValidationException | AuthorizationException | IllegalArgumentException e) {
            ctx.status(400).html(page("Error", actor,
                    "<p class=\"error\">" + esc(e.getMessage()) + "</p>"
                    + "<p><a href=\"/requests/new\">Back</a></p>"));
        }
    }

    /* ============== DETAIL ============== */

    public void showDetail(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        Request r;
        try {
            r = service.getVisible(actor, id);
        } catch (AuthorizationException | ValidationException e) {
            ctx.status(404).html(page("Not found", actor,
                    "<p class=\"error\">" + esc(e.getMessage()) + "</p>"));
            return;
        }

        StringBuilder comments = new StringBuilder();
        if (r.getComments().isEmpty()) {
            comments.append("<p class=\"empty\">No comments yet.</p>");
        } else {
            for (Comment c : r.getComments()) {
                comments.append("""
                        <div class="comment">
                          <div class="meta"><strong>%s</strong> (%s) &middot; %s</div>
                          <div class="body">%s</div>
                        </div>
                        """.formatted(esc(c.getAuthor().getFullName()),
                                c.getAuthor().getRole(),
                                c.getCreatedAt().format(FMT),
                                esc(c.getText()).replace("\n", "<br>")));
            }
        }

        String transitionBlock = renderTransitionBlock(actor, r);
        String assignBlock     = renderAssignBlock(actor, r);
        String priorityBlock   = renderPriorityBlock(actor, r);

        String body = """
                <div class="card">
                  <div class="req-head">
                    <h2>#%d &middot; %s%s</h2>
                    <div>%s</div>
                  </div>
                  <dl class="req-meta">
                    <dt>Category</dt><dd>%s</dd>
                    <dt>Priority</dt><dd>%s (SLA %dh)</dd>
                    <dt>Submitter</dt><dd>%s</dd>
                    <dt>Assignee</dt><dd>%s</dd>
                    <dt>Created</dt><dd>%s</dd>
                    <dt>Updated</dt><dd>%s</dd>
                    <dt>SLA deadline</dt><dd>%s</dd>
                  </dl>
                  <h3>Description</h3>
                  <p class="desc">%s</p>
                  %s %s %s
                </div>
                <div class="card">
                  <h3>Comments</h3>
                  %s
                  <form method="post" action="/requests/%d/comment">
                    <label>Add a comment<textarea name="text" rows="3" required></textarea></label>
                    <button type="submit">Post comment</button>
                  </form>
                </div>
                """.formatted(
                        r.getId(), esc(r.getTitle()), overdueMarker(r),
                        statusBadge(r.getStatus()),
                        r.getCategory(),
                        r.getPriority(), r.getPriority().getSlaHours(),
                        esc(r.getSubmitter().getFullName()),
                        r.getAssignee() == null ? "—" : esc(r.getAssignee().getFullName()),
                        r.getCreatedAt().format(FMT),
                        r.getUpdatedAt().format(FMT),
                        r.getSlaDeadline().format(FMT),
                        esc(r.getDescription()).replace("\n", "<br>"),
                        transitionBlock, assignBlock, priorityBlock,
                        comments, r.getId()
                );
        ctx.html(page("Request #" + r.getId(), actor, body));
    }

    /* ============== ACTIONS ============== */

    public void addComment(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        try {
            service.addComment(actor, id, ctx.formParam("text"));
        } catch (AuthorizationException | ValidationException e) {
            ctx.sessionAttribute("flash", e.getMessage());
        }
        ctx.redirect("/requests/" + id);
    }

    public void transition(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        try {
            Status target = Status.valueOf(ctx.formParam("target"));
            service.transitionStatus(actor, id, target);
        } catch (AuthorizationException | InvalidStatusTransitionException | IllegalArgumentException e) {
            ctx.sessionAttribute("flash", e.getMessage());
        }
        ctx.redirect("/requests/" + id);
    }

    public void assign(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        try {
            long agentId = Long.parseLong(ctx.formParam("agentId"));
            service.assignRequest(actor, id, agentId);
        } catch (AuthorizationException | ValidationException | InvalidStatusTransitionException
                 | NumberFormatException e) {
            ctx.sessionAttribute("flash", e.getMessage());
        }
        ctx.redirect("/requests/" + id);
    }

    public void changePriority(Context ctx) {
        User actor = WebApp.requireUser(ctx);
        long id = Long.parseLong(ctx.pathParam("id"));
        try {
            Priority p = Priority.valueOf(ctx.formParam("priority"));
            service.changePriority(actor, id, p);
        } catch (AuthorizationException | IllegalArgumentException e) {
            ctx.sessionAttribute("flash", e.getMessage());
        }
        ctx.redirect("/requests/" + id);
    }

    /* ============== partials ============== */

    private String renderTransitionBlock(User actor, Request r) {
        // Determine legal next states visible to this actor (UI hint — server still validates)
        StringBuilder options = new StringBuilder();
        for (Status s : Status.values()) {
            if (s != r.getStatus()) {
                options.append("<option value=\"").append(s).append("\">").append(s).append("</option>");
            }
        }
        return """
                <form method="post" action="/requests/%d/transition" class="inline-form">
                  <label>Change status
                    <select name="target">%s</select>
                  </label>
                  <button type="submit">Apply</button>
                </form>
                """.formatted(r.getId(), options);
    }

    private String renderAssignBlock(User actor, Request r) {
        if (!(actor instanceof Manager) && !(actor instanceof ServiceAgent)) return "";
        StringBuilder agentOpts = new StringBuilder();
        for (User u : users.findAll()) {
            if (u instanceof ServiceAgent) {
                boolean sel = r.getAssignee() != null && r.getAssignee().getId().equals(u.getId());
                agentOpts.append("<option value=\"").append(u.getId())
                        .append(sel ? "\" selected>" : "\">")
                        .append(esc(u.getFullName()))
                        .append("</option>");
            }
        }
        return """
                <form method="post" action="/requests/%d/assign" class="inline-form">
                  <label>(Re)assign to
                    <select name="agentId">%s</select>
                  </label>
                  <button type="submit">Assign</button>
                </form>
                """.formatted(r.getId(), agentOpts);
    }

    private String renderPriorityBlock(User actor, Request r) {
        if (!(actor instanceof Manager)) return "";
        StringBuilder opts = new StringBuilder();
        for (Priority p : Priority.values()) {
            opts.append("<option value=\"").append(p)
                    .append(p == r.getPriority() ? "\" selected>" : "\">")
                    .append(p).append("</option>");
        }
        return """
                <form method="post" action="/requests/%d/priority" class="inline-form">
                  <label>Change priority
                    <select name="priority">%s</select>
                  </label>
                  <button type="submit">Apply</button>
                </form>
                """.formatted(r.getId(), opts);
    }

    private static String options(Enum<?>[] values) {
        StringBuilder sb = new StringBuilder();
        for (Enum<?> v : values) {
            sb.append("<option value=\"").append(v).append("\">").append(v).append("</option>");
        }
        return sb.toString();
    }
}
