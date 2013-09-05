package com.atlassian.httpclient.api;

import com.atlassian.fugue.Functions;
import com.atlassian.fugue.Option;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class PartialFunctions
{
    public static <A, B> Function<A, Option<B>> from(Predicate<A> p, Function<? super A, ? extends B> f)
    {
        return Functions.partial(p, f);
    }

    public static <A, B> Function<A, B> compose(Iterable<Function<A, Option<B>>> it)
    {
        return new Stack<A, B>(it);
    }

    static class Stack<A, B> implements Function<A, B>
    {
        private final Iterable<Function<A, Option<B>>> partials;

        Stack(Iterable<Function<A, Option<B>>> partials)
        {
            this.partials = checkNotNull(partials);
        }

        @Override
        public B apply(A a)
        {
            for (Function<A, Option<B>> f : partials)
            {
                Option<B> option = f.apply(a);
                if (option.isDefined())
                {
                    return option.get();
                }
            }
            throw new MatchException(a);
        }
    }

    /**
     * Partial function as a composition of a Predicate and a Function.
     */
    static final class Partial<A, B> implements Function<A, Option<B>>
    {
        private final Predicate<A> p;
        private final Function<? super A, ? extends B> f;

        private Partial(Predicate<A> p, Function<? super A, ? extends B> f)
        {
            this.p = checkNotNull(p);
            this.f = checkNotNull(f);
        }

        @Override
        public Option<B> apply(A a)
        {
            if (p.apply(a))
            {
                return Option.<B>option(f.apply(a));
            }
            else
            {
                return Option.none();
            }
        }
    }
}