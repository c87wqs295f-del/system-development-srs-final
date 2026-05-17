package com.fs.srs.domain;

/**
 * The business department a request belongs to. Using enum instead of int so there can be no false Department assignements
 */
public enum Category {
    IT,
    FACILITY,
    HR,
    SUPPLY,
    ACCESS
}
