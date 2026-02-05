package net.lenni0451.optconfig.cli;

import lombok.With;
import lombok.experimental.WithBy;

/**
 * @param sort             Sort all options alphabetically by their name.<br>
 *                         This only affects the visual order of the options.<br>
 *                         By default, the options are in the order they were registered.
 * @param showBooleanType  Show the type of boolean options.<br>
 *                         By default, the type of boolean options is not shown.<br>
 *                         Type hidden: {@code --flag}, Type shown: {@code --flag [boolean]}.
 * @param quoteStrings     Show string default values in quotes.<br>
 *                         By default, string values are shown with quotes.
 * @param showDescription  Show the description of options.<br>
 *                         By default, the description is shown.
 * @param showDepends      Show the dependencies of options.<br>
 *                         By default, the dependencies are shown.
 * @param showDefaults     Show the default values of options.<br>
 *                         By default, the default values are shown.
 * @param optionTitle      The title of the option column.<br>
 *                         By default, the title is "Option".
 * @param descriptionTitle The title of the description/default/dependencies column.<br>
 *                         By default, the title is "Description".
 * @param headerSeparator  How to separate the header from the rest of the table.<br>
 *                         The default, {@link HeaderSeparator#COLUMN_WIDTH} is used.
 * @param separatorChar    The character to use for the header separator.<br>
 *                         By default, the character is '-'.
 * @param columnPadding    The number of spaces to pad between columns.<br>
 *                         By default, the padding is 2 spaces.
 */
@With
@WithBy
public record HelpOptions(boolean sort, boolean showBooleanType, boolean quoteStrings, boolean showDescription, boolean showDepends, boolean showDefaults,
        String optionTitle, String descriptionTitle, HeaderSeparator headerSeparator, char separatorChar, int columnPadding) {

    public static final HelpOptions DEFAULT = new HelpOptions(false, false, true, true, true, true,
            "Option", "Description", HeaderSeparator.COLUMN_WIDTH, '-', 2);

}
