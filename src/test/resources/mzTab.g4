//header

grammar mzTab;

MTD: 'MTD';
COM: 'COM';
SMH: 'SMH';
SML: 'SML';
SFH: 'SFH';
SMF: 'SMF';
SEH: 'SEH';
SME: 'SME';
TAB: '\t'; 
CR: '\r';
NL: '\n';
PIPE: '|';

WS : [\t\r\n]+ -> skip ; // skip spaces, tabs, newlines

/*
mzTabFile: mtd+ sml smf sme;
mtd : key '\t' (
key: 
sml row+ ;
hdr : row ;
*/
smh : SMH hdr;
sml : SML row;

sfh : SFH hdr;
smf : SMF row;

seh : SEH hdr;
sme : SME row;

comment: COM (TAB field)* CR? NL;

mztabFile : mtd+ 
          | smh sml+
          | sfh smf+
          | seh sme+
          | comment
          | WS;

hdr : row ;

mtd : MTD TAB field (TAB field)* CR? NL;

row : field (TAB field)* CR? NL ;

field
    : TEXT
    | CVTERM PIPE (CVTERM)*  
    | STRING
    |
    ;

TEXT   : ~[\n\r'"]+ ;
CVTERM : '[' STRING+ ']';
STRING : [A-Za-z0-9 ,;|\[\]]+ ; // quote-quote is an escaped quote

