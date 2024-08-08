package net.lenni0451.optconfig.utils;

import java.util.function.Function;
import java.util.function.IntFunction;

public class ArrayUtils {

    /**
     * Map an array from one type to another.
     *
     * @param input        The input array
     * @param mapper       The function to map the input to the output
     * @param arrayCreator The function to create a new array with the length of the input array
     * @param <I>          The input type
     * @param <O>          The output type
     * @return The mapped array
     */
    public static <I, O> O[] map(final I[] input, final Function<I, O> mapper, IntFunction<O[]> arrayCreator) {
        O[] output = arrayCreator.apply(input.length);
        for (int i = 0; i < input.length; i++) output[i] = mapper.apply(input[i]);
        return output;
    }

    /**
     * Reverse an array in place.
     *
     * @param array The array to reverse
     * @param <T>   The type of the array
     */
    public static <T> void reverse(final T[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            T temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

}
