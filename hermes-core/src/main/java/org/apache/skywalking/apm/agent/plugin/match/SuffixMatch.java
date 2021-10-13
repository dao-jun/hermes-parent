package org.apache.skywalking.apm.agent.plugin.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class SuffixMatch implements IndirectMatch {
    private final String[] suffixes;

    private SuffixMatch(String... suffixes) {
        if (suffixes == null || suffixes.length == 0) {
            throw new IllegalArgumentException("prefixes argument is null or empty");
        }
        this.suffixes = suffixes;
    }

    public static SuffixMatch nameEndsWith(final String... suffixes) {
        return new SuffixMatch(suffixes);
    }

    @Override
    public ElementMatcher.Junction buildJunction() {
        ElementMatcher.Junction junction = null;

        for (String suffix : suffixes) {
            if (junction == null) {
                junction = ElementMatchers.nameEndsWith(suffix);
            } else {
                junction = junction.and(ElementMatchers.nameEndsWith(suffix));
            }
        }

        return junction;
    }

    @Override
    public boolean isMatch(TypeDescription typeDescription) {
        for (final String suffix : suffixes) {
            if (typeDescription.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
}
