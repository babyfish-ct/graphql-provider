lexer grammar SqlLexer;

@header {
package org.babyfish.graphql.provider.r2dbc.lexer;
}

WS : [ \r\t\n]+ -> skip;

COMMENT: (LINE_COMMENT | BLOCK_COMMENT) -> skip;

fragment LINE_COMMENT: '--' ~[\r\n]* '\r'? '\n'?;

fragment BLOCK_COMMENT: '/*' .*? '*/';

STRING: '\'' ( ~'\'' | '\'\'' )* '\'';

IDENTIFIER: ORACLE_IDENTIFIER | MYSQL_IDENTIFIER | MSSQL_IDENTIFIER;

fragment ORACLE_IDENTIFIER: '"' .*? '"';

fragment MYSQL_IDENTIFIER: '`' .*? '`';

fragment MSSQL_IDENTIFIER: '[' .*? ']';

LEFT_BRACKET: '(';

RIGHT_BRACKET: ')';

WORD: . -> more;


