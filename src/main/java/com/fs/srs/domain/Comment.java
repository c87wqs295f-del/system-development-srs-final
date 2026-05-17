package com.fs.srs.domain;

import java.time.LocalDateTime;


public class Comment {

    private Long id; 
    private final Long requestId;
    private final User author;
    private final String text;
    private final LocalDateTime createdAt;

    public Comment(Long id, Long requestId, User author, String text, LocalDateTime createdAt) {
        this.id = id;
        this.requestId = requestId;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
    }

    /**  constructor for new comments (id asigned by the repository). */
    public Comment(Long requestId, User author, String text) {
        this(null, requestId, author, text, LocalDateTime.now());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRequestId() { return requestId; }
    public User getAuthor() { return author; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
