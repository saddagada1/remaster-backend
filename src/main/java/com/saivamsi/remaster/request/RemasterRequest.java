package com.saivamsi.remaster.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemasterRequest {
    @NotNull
    private String url;
    @NotNull
    private String name;
    private String description;
    @NotNull
    private Float duration;
    @NotNull
    private Integer key;
    @NotNull
    private Integer mode;
    @NotNull
    private Float tempo;
    @NotNull
    private Integer timeSignature;
    @NotNull
    private Integer tuning;
}
