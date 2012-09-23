package bpsm.edn.parser;

import java.util.HashMap;
import java.util.Map;

import bpsm.edn.model.Keyword;
import bpsm.edn.model.Symbol;

class Interners {

    static Interner<Keyword> newKeywordInterner(boolean shouldIntern) {
        if (shouldIntern) {
            return new Hashed<Keyword>();
        } else {
            return new NoOp<Keyword>();
        }
     }
    
    static Interner<Symbol> newSymbolInterner(boolean shouldIntern) {
        if (shouldIntern) {
            return new Hashed<Symbol>();
        } else {
            return new NoOp<Symbol>();
        }
    }
    
    static Interner<String> newStringInterner(int maxInternedStringLength) {
        if (maxInternedStringLength == 0) {
            return new EmptyString();
        } else if (maxInternedStringLength > 0) {
            return new StringsMaxLength(maxInternedStringLength);
        } else {
            return new NoOp<String>();
        }
    }
    
    static class NoOp<T> implements Interner<T> {
        public T intern(T t) {
               return t;
        }
    }
    
    static class EmptyString implements Interner<String> {
        private static final String EMPTY_STRING = "".intern();
 
        public String intern(String s) {
            return s.length() == 0 ? EMPTY_STRING : s;
        }
    }

    static class Hashed<T> implements Interner<T> {
        private final Map<T,T> m = new HashMap<T,T>();
   
        public T intern(T t) {
            T interned = m.get(t);
            if (interned != null) {
                return interned;
            }
            m.put(t, t);
            return t;
        }
    }
    
    static class StringsMaxLength extends Hashed<String> {
        private final int maxInternedStringLength;

        StringsMaxLength(int maxInternedStringLength) {
            this.maxInternedStringLength = maxInternedStringLength;
        }
        
        public String intern(String s) {
            return maxInternedStringLength < s.length() ? s : super.intern(s); 
        }
    }
    
}
