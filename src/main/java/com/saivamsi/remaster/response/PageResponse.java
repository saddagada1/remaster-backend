package com.saivamsi.remaster.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private UUID next;
    @NotNull
    private List<T> items;
}
