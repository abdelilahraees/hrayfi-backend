package org.example.backend.admin.dto;

import jakarta.validation.constraints.Size;

public record NoteAdminRequest(@Size(max = 5000) String note) {}
