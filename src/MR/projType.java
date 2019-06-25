package MR;

/**
 * encapsulates all options of projection _n indicates added noise _r indicates added rotation _t translation
 * @author rohleder
 */
enum projType{
	MATERIAL,
	POLY120, POLY120n, // polychromatic projection with or w/out noise 
	POLY80, POLY80n;
}