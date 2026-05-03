package net.lenni0451.optconfig.utils;

import net.lenni0451.optconfig.cli.model.HelpOptions;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ApiStatus.Internal
public class HelpTable {

    private final HelpOptions options;
    private final int maxWidth;
    private final List<Row> rows = new ArrayList<>();

    public HelpTable(final HelpOptions options) {
        this.options = options;
        this.maxWidth = TerminalUtils.getTerminalWidth();
    }

    public void addRow(final List<String> names, final String type, final boolean required, final Collection<String> description) {
        this.addRow(names, type, required, description.toArray(new String[0]));
    }

    public void addRow(final List<String> names, final String type, final boolean required, final String... description) {
        this.rows.add(new Row(names, type, required, description));
    }

    private String buildOptionString(final Row row) {
        StringBuilder sb = new StringBuilder(String.join(", ", row.names()));
        if (row.type() != null && !row.type().isEmpty()) {
            sb.append(" ").append(row.type());
        }
        if (this.options.showRequired() && row.required()) {
            sb.append(" (required)");
        }
        return sb.toString();
    }

    private Size calculateSize() {
        int optionColumnWidth = Math.max(
                this.rows.stream()
                        .map(this::buildOptionString)
                        .mapToInt(String::length)
                        .max().orElse(0),
                this.options.optionTitle().length()
        );
        int maxOptionColumnWidth = Math.max(20, this.maxWidth / 3);
        if (optionColumnWidth > maxOptionColumnWidth) {
            optionColumnWidth = Math.max(maxOptionColumnWidth, this.options.optionTitle().length());
        }

        int columnPadding = this.options.columnPadding();

        int maxDescriptionSize = this.rows.stream()
                .flatMap(r -> Arrays.stream(r.description()))
                .mapToInt(String::length)
                .max()
                .orElse(0);
        if (maxDescriptionSize > 0) {
            maxDescriptionSize = Math.max(this.options.descriptionTitle().length(), maxDescriptionSize);
        }

        if (optionColumnWidth + columnPadding + maxDescriptionSize <= this.maxWidth) {
            return new Size(optionColumnWidth, columnPadding, maxDescriptionSize);
        } else {
            int availableForDesc = this.maxWidth - optionColumnWidth - columnPadding;
            return new Size(optionColumnWidth, columnPadding, Math.max(10, availableForDesc));
        }
    }

    public String build() {
        if (this.rows.isEmpty()) return "";

        Size size = this.calculateSize();
        StringBuilder out = new StringBuilder();
        boolean hasDescription = size.description > 0;

        // Render Header
        out.append(this.options.optionTitle());
        if (hasDescription) {
            int paddingToDesc = size.option - this.options.optionTitle().length() + size.gap;
            out.append(" ".repeat(Math.max(0, paddingToDesc)));
            out.append(this.options.descriptionTitle());
        }
        out.append("\n");

        // Render Separator
        switch (this.options.headerSeparator()) {
            case NONE -> {
            }
            case HEADER_WIDTH -> {
                out.append(String.valueOf(this.options.separatorChar()).repeat(this.options.optionTitle().length()));
                if (hasDescription) {
                    int paddingToDesc = size.option - this.options.optionTitle().length() + size.gap;
                    out.append(" ".repeat(Math.max(0, paddingToDesc)));
                    out.append(String.valueOf(this.options.separatorChar()).repeat(this.options.descriptionTitle().length()));
                }
                out.append("\n");
            }
            case COLUMN_WIDTH -> {
                out.append(String.valueOf(this.options.separatorChar()).repeat(size.option));
                if (hasDescription) {
                    out.append(" ".repeat(size.gap));
                    out.append(String.valueOf(this.options.separatorChar()).repeat(size.description));
                }
                out.append("\n");
            }
            default -> throw new IllegalStateException("Unexpected header separator: " + this.options.headerSeparator());
        }

        // Render Rows
        for (Row row : this.rows) {
            List<String> wrappedOption = this.wrapText(this.buildOptionString(row), size.option, false);
            List<String> wrappedDescription = Arrays.stream(row.description)
                    .map(line -> this.wrapText(line, size.description, true))
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(ArrayList::new));

            while (!wrappedOption.isEmpty() || !wrappedDescription.isEmpty()) {
                String optionLine = wrappedOption.isEmpty() ? "" : wrappedOption.remove(0);
                out.append(optionLine);
                if (optionLine.length() <= size.option && !wrappedDescription.isEmpty()) {
                    String descriptionLine = wrappedDescription.remove(0);
                    out.append(" ".repeat(Math.max(0, size.option - optionLine.length() + size.gap)));
                    out.append(descriptionLine);
                }
                out.append("\n");
            }
        }

        return out.toString();
    }

    private List<String> wrapText(final String text, final int maxWidth, final boolean wrapWords) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n", -1)) {
            if (line.length() <= maxWidth || maxWidth <= 0) {
                lines.add(line);
                continue;
            }

            StringBuilder currentLine = new StringBuilder();
            for (String word : line.split(" ")) {
                if (word.isEmpty()) continue;

                if (currentLine.length() + word.length() + (currentLine.isEmpty() ? 0 : 1) > maxWidth) {
                    if (!currentLine.isEmpty()) {
                        lines.add(currentLine.toString());
                        currentLine.setLength(0);
                    }
                    if (wrapWords && word.length() > maxWidth) {
                        while (word.length() > maxWidth) {
                            lines.add(word.substring(0, maxWidth));
                            word = word.substring(maxWidth);
                        }
                    }
                } else {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                }
                currentLine.append(word);
            }
            if (!currentLine.isEmpty()) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }


    private record Row(List<String> names, String type, boolean required, String[] description) {
    }

    private record Size(int option, int gap, int description) {
    }

}
