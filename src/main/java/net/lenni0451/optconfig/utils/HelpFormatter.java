package net.lenni0451.optconfig.utils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HelpFormatter {

    public static Builder builder() {
        return new Builder();
    }


    @Getter
    private final int columnCount;
    @Nullable
    private final String[] headers;
    private final HeaderSeparator headerSeparator;
    private final char separatorChar;
    private final int columnPadding;

    private final List<String>[] columns;

    public HelpFormatter add(final int column, final String text) {
        this.columns[column].add(text);
        return this;
    }

    public HelpFormatter pad() {
        int maxRows = Stream.of(this.columns).mapToInt(List::size).max().orElse(0);
        for (List<String> column : this.columns) {
            while (column.size() < maxRows) {
                column.add("");
            }
        }
        return this;
    }

    @Override
    public String toString() {
        int[] columnWidth = new int[this.columnCount];
        for (int i = 0; i < this.columnCount; i++) {
            if (this.headers != null) {
                columnWidth[i] = this.headers[i].length();
            }
            columnWidth[i] = Math.max(columnWidth[i], this.columns[i].stream().mapToInt(String::length).max().orElse(0));
        }

        List<String>[] outColumns = new List[this.columnCount];
        for (int i = 0; i < this.columnCount; i++) {
            outColumns[i] = new ArrayList<>(this.columns[i].size() + 2);
        }
        if (this.headers != null) {
            for (int i = 0; i < this.headers.length; i++) {
                outColumns[i].add(this.headers[i]);
                switch (this.headerSeparator) {
                    case HEADER_WIDTH -> outColumns[i].add(String.valueOf(this.separatorChar).repeat(this.headers[i].length()));
                    case COLUMN_WIDTH -> outColumns[i].add(String.valueOf(this.separatorChar).repeat(columnWidth[i]));
                }
            }
        }
        for (int i = 0; i < this.columns.length; i++) {
            outColumns[i].addAll(this.columns[i]);
        }

        StringBuilder out = new StringBuilder();
        int maxRows = Stream.of(outColumns).mapToInt(List::size).max().orElse(0);
        for (int row = 0; row < maxRows; row++) {
            for (int column = 0; column < this.columns.length; column++) {
                String cell = row < outColumns[column].size() ? outColumns[column].get(row) : "";
                out.append(cell);
                int padding = columnWidth[column] - cell.length() + this.columnPadding;
                if (column < this.columns.length - 1) {
                    out.append(" ".repeat(padding));
                }
            }
            if (row < maxRows - 1) {
                out.append("\n");
            }
        }
        return out.toString();
    }


    public enum HeaderSeparator {
        NONE, HEADER_WIDTH, COLUMN_WIDTH
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder {
        private int columnCount;
        private String[] headers;
        private HeaderSeparator headerSeparator = HeaderSeparator.COLUMN_WIDTH;
        private char separatorChar = '-';
        private int columnPadding = 2;

        public Builder columnCount(final int columnCount) {
            this.columnCount = columnCount;
            return this;
        }

        public Builder headers(final String... headers) {
            this.headers = headers;
            return this;
        }

        public Builder headerSeparator(final HeaderSeparator headerSeparator, final char separatorChar) {
            this.headerSeparator = headerSeparator;
            this.separatorChar = separatorChar;
            return this;
        }

        public HelpFormatter build() {
            if (this.columnCount <= 0 && this.headers != null) this.columnCount = this.headers.length;
            if (this.columnCount <= 0) throw new IllegalStateException("Column count must be greater than 0");
            if (this.headers != null && this.columnCount != this.headers.length) throw new IllegalStateException("Number of headers must match the column count");
            List<String>[] columns = new List[this.columnCount];
            for (int i = 0; i < this.columnCount; i++) columns[i] = new ArrayList<>();
            return new HelpFormatter(
                    this.columnCount,
                    this.headers,
                    this.headerSeparator,
                    this.separatorChar,
                    this.columnPadding,
                    columns
            );
        }
    }

}
