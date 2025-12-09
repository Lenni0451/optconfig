package net.lenni0451.optconfig.cli;

import lombok.With;

@With
public record HelpOptions(boolean sort, boolean booleanType, boolean quoteStrings, boolean description, boolean depends, boolean defaults) {

    public static final HelpOptions DEFAULT = new HelpOptions(false, false, true, true, true, true);

}
