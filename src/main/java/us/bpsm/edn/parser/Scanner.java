package us.bpsm.edn.parser;

import java.io.IOException;

interface Scanner {

    Object nextToken(Parseable pbr);

}