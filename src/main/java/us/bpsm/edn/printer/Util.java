// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.printer;

class Util {

    @SuppressWarnings("rawtypes")
    static Class getClassOrNull(Object o) {
        return o == null ? null : o.getClass();
    }

}
