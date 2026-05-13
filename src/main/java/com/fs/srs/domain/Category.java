package com.fs.srs.domain;

/**
 * The business department a request belongs to.
 * Using an enum (not a String) makes invalid categories impossible at compile time.
 */
public enum Category {
    IT,
    FACILITY,
    HR,
    SUPPLY,
    ACCESS
}
