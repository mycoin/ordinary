/*
Author: ≈∑—Ùœ»Œ∞(Xianwei Ouyang)
Version 0.1.2, 2007/04/15
allskystar@hotmail.com
*/
var s = HTMLWindows.defaultStyles;
s.set("#form", "font-size:12px;");
s.set("#form #inline", "padding:0px 15px 15px 0px;");
s.set("#form #title", "display:none;");
s.set("#form #close", "position:absolute;width:14px;height:13px;top:3px;right:17px;");
s.set("#form #close #inline", "border-left:1px solid #DDD;border-top:1px solid #DDD;border-right:1px solid #333;border-bottom:1px solid #333;");
s.set("#form #area0", "background:#FFFFE0 url(" + wsclPath + "skins/windows/form111.gif);");
s.set("#form #area1", "height:17px;background:#FFFFE0 url(" + wsclPath + "skins/windows/form112.gif) repeat-x top;");
if (isIE)
{
	s.set("#form #area2", "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + wsclPath + "skins/windows/form113.png',sizingmethod=scale);");
	s.set("#form #area5", "width:15px;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + wsclPath + "skins/windows/form123.png',sizingmethod=scale);");
	s.set("#form #area6", "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + wsclPath + "skins/windows/form131.png',sizingmethod=scale);");
	s.set("#form #area7", "height:15px;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + wsclPath + "skins/windows/form132.png',sizingmethod=scale);");
	s.set("#form #area8", "filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + wsclPath + "skins/windows/form133.png',sizingmethod=scale);");
}
else
{
	s.set("#form #area2", "background:url(" + wsclPath + "skins/windows/form113.png);");
	s.set("#form #area5", "width:15px;background:url(" + wsclPath + "skins/windows/form123.png);");
	s.set("#form #area6", "background:url(" + wsclPath + "skins/windows/form131.png);");
	s.set("#form #area7", "height:15px;background:url(" + wsclPath + "skins/windows/form132.png);");
	s.set("#form #area8", "background:url(" + wsclPath + "skins/windows/form133.png);");
}
s.set("#form #area3", "width:16px;background:#FFFFE0 url(" + wsclPath + "skins/windows/form121.gif) repeat-y left;");
s.set("#form #area4", "background-color:#FFFFE0;text-align:center;");
s.set("#form #main", "");

s.set("#editNum .edit", "text-align:right;");

s.set("#buttonCal .button", "");
s.set("#buttonCal #button", "text-align:center");
s.set("#buttonCalRed .buttonCal", "color:#FF0000");
s.set("#buttonCalBlue .buttonCal", "color:#0000FF");
