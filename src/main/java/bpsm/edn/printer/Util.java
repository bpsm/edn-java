package bpsm.edn.printer;

class Util {

    @SuppressWarnings("rawtypes")
    static Class getClassOrNull(Object o) {
        return o == null ? null : o.getClass();
    }
    
}
