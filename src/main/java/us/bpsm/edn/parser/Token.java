// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

/**
 * The members of this enum are a subset of the values that may be
 * returned by {@link Scanner#nextToken(Parseable)}.
 */
public enum Token {

    /** Signals the end of input. */
    END_OF_INPUT,

    /** A '(', which begins a list */
    BEGIN_LIST,

    /** A ')', which ends a list. */
    END_LIST,

    /** A '[', which begins a vector */
    BEGIN_VECTOR,

    /** A ']', which ends a vector */
    END_VECTOR,

    /** A '#{', which begins a set */
    BEGIN_SET,

    /** A '{', which begins a map */
    BEGIN_MAP,

    /** A '}', which ends a set or map */
    END_MAP_OR_SET,

    /** A 'nil', which a parser should interpret as a Java null. */
    NIL,

    /**
     * A '#_', which instructs the parser to ignore the next value
     * parsed from the input.
     */
    DISCARD;
}
