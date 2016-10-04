// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package us.bpsm.edn.parser;

import java.io.Serializable;
import java.util.*;

final class DefaultListFactory implements CollectionBuilder.Factory {
    @Override
	public CollectionBuilder builder() {
        return new CollectionBuilder() {
            ArrayList<Object> list = new ArrayList<Object>();
            @Override
			public void add(Object o) {
                list.add(o);
            }
            @SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public Object build() {
                return new DelegatingList(list);
            }
        };
    }
}

@SuppressWarnings("serial")
final class DelegatingList<E> extends AbstractList<E> implements Serializable {
    final List<E> delegate;

    DelegatingList(List<E> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public E get(int index) {
        return delegate.get(index);
    }

}
