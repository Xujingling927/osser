package com.jingling.osser.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MouseLocation {
    private String id;
    private String name;
    private Integer x;
    private Integer y;

}
