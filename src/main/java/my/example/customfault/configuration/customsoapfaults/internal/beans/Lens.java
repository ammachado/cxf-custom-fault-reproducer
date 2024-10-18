package my.example.customfault.configuration.customsoapfaults.internal.beans;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import lombok.RequiredArgsConstructor;

/**
 * Represents a lens, which is a functional abstraction that allows
 * accessing and modifying a part of an object with immutability.
 * <p>
 * Used as a replacement of <a href="https://commons.apache.org/proper/commons-beanutils/">Apache Commons BeanUtils</a>
 * <p>
 * Adapted from <a href="https://medium.com/expedia-group-tech/lenses-in-java-2b18c7d24366">this</a>.
 *
 * @param <A> The type of the original object.
 * @param <B> The type of the value to access or modify within the original object.
 */
@RequiredArgsConstructor(staticName = "of")
public class Lens<A, B> {

    private final Function<A, B> getter;
    private final BiFunction<A, B, A> setter;

    /**
     * Returns the corresponding value of type B for the given input of type A using the provided getter function.
     *
     * @param a the input value of type A
     * @return the corresponding value of type B
     */
    public B get(final A a) {
        return getter.apply(a);
    }

    /**
     * Sets the value of type B for the corresponding input of type A using the provided setter function.
     *
     * @param a the input value of type A
     * @param b the value of type B to be set
     * @return the modified value of type A
     */
    public A set(final A a, final B b) {
        return setter.apply(a, b);
    }

    /**
     * Modifies the value of type A using the provided unary operator function.
     *
     * @param a the input value of type A
     * @param unaryOperator the unary operator function to apply to the value of type B
     * @return the modified value of type A
     */
    public A mod(final A a, final UnaryOperator<B> unaryOperator) {
        return set(a, unaryOperator.apply(get(a)));
    }

    /**
     * Composes the current lens with another lens, creating a new lens that operates on a different type.
     *
     * @param <C> the type of the outer object
     * @param that the lens to compose with
     * @return the composed lens
     */
    public <C> Lens<C, B> compose(final Lens<C, A> that) {
        return new Lens<>(
                c -> get(that.get(c)),
                (c, b) -> that.mod(c, a -> set(a, b))
        );
    }

    /**
     * Composes this lens with another lens to create a new lens.
     *
     * @param that The lens to compose with.
     * @param <C>  The type of the composed lens.
     * @return The composed lens.
     */
    public <C> Lens<A, C> andThen(final Lens<B, C> that) {
        return that.compose(this);
    }
}
