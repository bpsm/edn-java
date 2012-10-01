// (c) 2012 B Smith-Mannschott -- Distributed under the Eclipse Public License
package bpsm.edn.parser;

enum Token {
	END_OF_INPUT,
	BEGIN_LIST,
	END_LIST,
	BEGIN_VECTOR,
	END_VECTOR,
	BEGIN_SET,
	BEGIN_MAP,
	END_MAP_OR_SET,
	NIL,
	DISCARD;
}