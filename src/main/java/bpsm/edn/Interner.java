package bpsm.edn;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Interner<K,V> {

    private final ConcurrentHashMap<K, Reference<V>> table = 
            new ConcurrentHashMap<K, Reference<V>>();
    private final ReferenceQueue<V> refQueue = new ReferenceQueue<V>();
    
    public V intern(K key, V value) {
        while (true) {
            clearDeadEntries();
            WeakReference<V> newRef = new WeakReference<V>(value, refQueue);
            Reference<V> existingRef = table.putIfAbsent(key, newRef);
            if (existingRef == null) {
                // newRef has been entered into the cache
                return value;
            } 
            
            // existingRef was found in the cache; newRef is garbage
            V existingValue = existingRef.get();
            if (existingValue != null) {
                return existingValue;
            }
            
            // existingRef's referent has been collected out from under us
            table.remove(key, existingRef);
        }
    }

    private void clearDeadEntries() {
        if (refQueue.poll() == null) {
            // empty queue indicates that there's nothing to clear
            return;
        }
        while (refQueue.poll() != null) {
            // wait until there's nothing more in flight in the queue
        }
        for (Map.Entry<K, Reference<V>> me: table.entrySet()) {
            Reference<V> ref = me.getValue();
            if (ref != null && ref.get() == null) {
                table.remove(me.getKey(), ref);
            }
        }
    }
    
}
