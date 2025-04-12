package com.balugaq.bukkitbackdoor.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    public String code = ";";
    public Settings settings = new Settings();
}
