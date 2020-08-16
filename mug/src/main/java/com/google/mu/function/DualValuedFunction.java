/*****************************************************************************
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 *                                                                           *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 *****************************************************************************/
package com.google.mu.function;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function with two result values.
 *
 * <p>Methods wishing to return two values should typically follow this pattern to take as parameter
 * a custom {@link BiFunction} so that the caller can choose the appropriate result type without
 * being forced to use {@code Pair} or {@code Map.Entry}.
 *
 * <p>See {@link java.util.stream.Collectors#teeing} in JDK 12 as an example.
 *
 * @param <F> the input type
 * @param <T1> the first output type
 * @param <T2> the second output type
 *
 * @since 4.6
 */
@FunctionalInterface
public interface DualValuedFunction<F, T1, T2> {
  /**
   * With {@code input}, calls this function and passes the two result values to the {@code output}
   * function (because Java has no built-in tuples).
   *
   * <p>An example is the {@link com.google.mu.util.Substring.Pattern#split(String, BiFunction)}
   * method. Its callers can typically split a string like this:
   *
   * <pre>{@code
   * first('=').split(string, (name, val) -> ...);
   * }</pre>
   *
   * The {@code split()} method (and its friend {@code splitThenTrim}) can then be
   * method-referenced as a {@code DualValuedFunction} and be used in a {@link
   * com.google.mu.util.stream.BiStream} chain, like:
   *
   * <pre>{@code
   * ImmutableSetMultimap<String, String> keyValues = lines.stream()
   *     .map(String::trim)
   *     .filter(l -> l.length() > 0)                     // not empty
   *     .filter(l -> !l.startsWith("//"))                // not comment
   *     .collect(toBiStream(first('=')::splitThenTrim))  // split each line to a key-value pair
   *     .collect(ImmutableSetMultimap::toImmutableSetMultimap);
   * }</pre>
   *
   * @throws NullPointerException if the {@code output} function is null.
   */
  // No wildcard on T1/T2 so that methods following or not following PECS can both be referenced.
  // Users should rarely need to call apply() directly.
  <R> R apply(F input, BiFunction<T1, T2, R> output);

  /**
   * Returns a composed function that first applies this function to its input,
   * and then applies the {@code after} function to the pair of results. If evaluation of either
   * function throws an exception, it is propagated to the caller of the composed function.
   *
   * @throws NullPointerException if the {@code after} function is null.
   */
  default <R> Function<F, R> andThen(BiFunction<? super T1, ? super T2, ? extends R> after) {
    @SuppressWarnings("unchecked")  // function is PECS, safe to cast.
    BiFunction<T1, T2, R> then = (BiFunction<T1, T2, R>) requireNonNull(after);
    return input -> apply(input, then);
  }
}
