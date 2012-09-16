# Coding Conventions

I'm coding this using Eclipse using the "Java Conventions" with 4
spaces per indentation level.

    class Foo {
        int method() {
            if ( ... ) {
                ...
            } else if ( ... ) {
                ...
            }
            switch (...) {
            case 0:
                ...
            default:
                ...
            }
        }
    }

There should be no trailing white space. In Eclipse, configure Preferences ⇒ Java ⇒ Editor ⇒ "Save Actions" to assure this.

Unit tests are useful. Have enough of those.


