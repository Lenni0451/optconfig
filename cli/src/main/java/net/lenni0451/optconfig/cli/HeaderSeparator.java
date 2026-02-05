package net.lenni0451.optconfig.cli;

/**
 * How to separate the header from the rest of the table.
 */
public enum HeaderSeparator {

    /**
     * No separator, the header will be directly above the first row.
     */
    NONE,
    /**
     * A separator with the width of the header text for each column.
     */
    HEADER_WIDTH,
    /**
     * A separator with the width of the entire column.
     */
    COLUMN_WIDTH

}
