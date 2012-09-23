package bpsm.edn.parser;


interface Interner<T> {
    
    public T intern(T t);
    
}
