// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn;

import java.io.Serializable;

/**
 * A Tagged value that received no specific handling because the Parser
 * was not configured with a handler for its tag.
 */
public final class TaggedValue implements Serializable {
    private final Tag tag;
    private final Object value;

    private TaggedValue(Tag tag, Object value) {
        this.tag = tag;
        this.value = value;
    }

    /**
     * Return a tagged value for the given tag and value (some edn data).
     * The tag must not be null.
     * @param tag not null.
     * @param value may be null.
     * @return a TaggedValue, never null.
     */
    public static TaggedValue newTaggedValue(Tag tag, Object value) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must not be null");
        }
        return new TaggedValue(tag, value);
    }

    /**
     * Returns this TaggedValue's tag, which is never null.
     * @return never null.
     */
    public Tag getTag() {
        return tag;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + tag.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TaggedValue other = (TaggedValue) obj;
        if (!tag.equals(other.tag)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return tag + " " + value;
    }

}
