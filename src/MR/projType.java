package MR;

/**
 * encapsulates all options of projection _n indicates added noise _r indicates added rotation _t translation
 * @author rohleder
 */
enum projType{
	MATERIAL, MATERIALn, MATERIALnr, MATERIALnt, MATERIALrt, MATERIALnrt,
	POLY120, POLY120n, POLY120nr, POLY120nt, POLY120rt, POLY120nrt, 
	POLY80, POLY80n, POLY80nr, POLY80nt, POLY80rt, POLY80nrt;
}