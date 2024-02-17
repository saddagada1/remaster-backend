package com.saivamsi.remaster.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ObjectWithCount<T> {
    private T object;
    private int count;

    public void incrementCount() {
        count++;
    }
}
