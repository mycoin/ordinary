/*
UTF-8字符集支持

Author: 欧阳先伟(Xianwei Ouyang)
Version Beta 0.1, 2008/02/--
allskystar@hotmail.com
*/
String.prototype.encode = function()
{
	return encodeURIComponent(this);
}
String.prototype.count = function()
{
	var value = 0;
	var i = this.length;
	while (i--)
	{
		var c = this.charCodeAt(i);
		value += c < 128 ? 1 : (c < 2048 ? 2 : 3);
	}
	return value;
}
