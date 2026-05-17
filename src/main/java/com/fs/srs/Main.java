package com.fs.srs;

import com.fs.srs.domain.Category;
import com.fs.srs.domain.Employee;
import com.fs.srs.domain.Manager;
import com.fs.srs.domain.Priority;
import com.fs.srs.domain.Request;
import com.fs.srs.domain.ServiceAgent;
import com.fs.srs.domain.Status;
import com.fs.srs.domain.User;
import com.fs.srs.repository.Database;
import com.fs.srs.repository.RequestRepository;
import com.fs.srs.repository.SqliteRequestRepository;
import com.fs.srs.repository.SqliteUserRepository;
import com.fs.srs.repository.UserRepository;
import com.fs.srs.service.AuthService;
import com.fs.srs.service.RequestService;
import com.fs.srs.web.WebApp;
import io.javalin.Javalin;

/**
 * Application entry point it connects the layers: Database + Repositories + Services + WebApp.
 */
public final class Main {

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", System.getProperty("port", "7070")));

        // 1. Persistence
        Database db = Database.openDefault();
        UserRepository userRepo = new SqliteUserRepository(db);
        RequestRepository requestRepo = new SqliteRequestRepository(db, userRepo);

        // 2. Seed demo data on first run 
        Seed.run(userRepo, requestRepo);

        // 3. Services
        AuthService auth = new AuthService(userRepo);
        RequestService requests = new RequestService(requestRepo, userRepo);

        // 4. Web
        WebApp webApp = new WebApp(auth, requests, userRepo);
        WebApp.setInstance(webApp);
        Javalin javalin = webApp.build();
        javalin.start(port);

        System.out.println("""

                ============================================================
                  Service Request Management System is running.
                  Open http://localhost:%d in your browser.

                  Demo accounts (all passwords = "pw"):
                    manager1 / pw   (Manager)
                    agent1   / pw   (Service Agent, IT)
                    agent2   / pw   (Service Agent, Facility)
                    emp1     / pw   (Employee)
                    emp2     / pw   (Employee)
                ============================================================
                """.formatted(port));
    }

    /** data for demo purpose it only inserts data if the users table is empty. */
    private static final class Seed {
        static void run(UserRepository userRepo, RequestRepository requestRepo) {
            if (!userRepo.findAll().isEmpty()) return;

            Manager boss     = new Manager(null, "manager1", "pw", "Maria Boss",   "maria.boss@example.com");
            ServiceAgent a1  = new ServiceAgent(null, "agent1", "pw", "Adam Agent",  "adam@example.com");
            ServiceAgent a2  = new ServiceAgent(null, "agent2", "pw", "Ava Agent",   "ava@example.com");
            Employee e1      = new Employee(null, "emp1", "pw", "Erik Employee", "erik@example.com");
            Employee e2      = new Employee(null, "emp2", "pw", "Elena Employee", "elena@example.com");
            for (User u : new User[]{boss, a1, a2, e1, e2}) userRepo.save(u);

            Request r1 = new Request("Laptop won't boot",
                    "Since this morning my MacBook shows a black screen after the Apple logo.",
                    Category.IT, Priority.HIGH, e1);
            requestRepo.save(r1);
            r1.assign(a1);
            requestRepo.save(r1);

            Request r2 = new Request("Office AC not working",
                    "Room 3.14 has no cold air. It's 28°C in there.",
                    Category.FACILITY, Priority.MEDIUM, e2);
            requestRepo.save(r2);

            Request r3 = new Request("New hire onboarding for Tom",
                    "Please prepare laptop, email account and building card for new hire Tom.",
                    Category.HR, Priority.LOW, e1);
            requestRepo.save(r3);
            r3.assign(a2);
            r3.transitionTo(Status.IN_PROGRESS);
            r3.transitionTo(Status.RESOLVED);
            requestRepo.save(r3);
        }
    }

    private Main() { /* utility class */ }
}
