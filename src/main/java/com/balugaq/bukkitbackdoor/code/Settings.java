package com.balugaq.bukkitbackdoor.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.ParametersAreNonnullByDefault;

@Data
@ParametersAreNonnullByDefault
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settings {
    public boolean timeit = false;
}
