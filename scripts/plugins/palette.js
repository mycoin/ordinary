/*
调色板插件

Author: 欧阳先伟(Xianwei Ouyang)
Version Beta 0.1, 2008/02/--
allskystar@hotmail.com
*/

/*
Color - 色彩对象, 能完成RGB与HSL之间的互相转化

属性
_nRed - 红色值(0-255)
_nGreen - 绿色值(0-255)
_nBlue - 蓝色值(0-255)
_nHue - 色调(0-1)
_nSaturation - 饱和度(0-1)
_nLight - 亮度(0-1)
_sRGB - RGB颜色字符串

方法
_Hue2RGB - 将色调转化成RGB色彩空间
getRGB - 获得RGB描述字符串, 按rrggbb的顺序
getRed - 获得红色信号值
getGreen - 获得绿色信号值
getBlue - 获得蓝色信号值
getHue - 获得色调
getSaturation - 获得饱和度
getLight - 获得亮度
setRGB - 设置RGB色彩空间
setHSL - 设置HSL色彩空间
*/
function Color(value)
{
	if (typeof value == "string")
	{
		this.setRGB(value.substring(0, 2).number(16), value.substring(2, 4).number(16), value.substring(4, 6).number(16));
	}
	else if (typeof value == "number")
	{
		this.setRGB(value, arguments[1], arguments[2]);
	}
}
Color.prototype._Hue2RGB = function(minValue, maxValue, hue)
{
	hue = hue < 0 ? hue + 1 : (hue > 1 ? hue - 1 : hue);
	hue = hue < 0.5 ? Math.min(6 * hue, 1) : Math.max(4 - 6 * hue, 0);
	return Math.round(255 * (minValue + (maxValue - minValue) * hue));
/*	return Math.round(255 * (6 * H < 1 ? a + (b - a) * 6 * H : (H < 0.5 ? b : (3 * H < 2 ? a + (b - a) * (4 - 6 * H) : a))));*/
}
Color.prototype.getRGB = function()
{
	return this._sRGB;
}
Color.prototype.getRed = function()
{
	return this._nRed;
}
Color.prototype.getGreen = function()
{
	return this._nGreen;
}
Color.prototype.getBlue = function()
{
	return this._nBlue;
}
Color.prototype.getHue = function()
{
	return this._nHue;
}
Color.prototype.getSaturation = function()
{
	return this._nSaturation;
}
Color.prototype.getLight = function()
{
	return this._nLight;
}
Color.prototype.setRGB = function(red, green, blue)
{
	this._nRed = red;
	this._nGreen = green;
	this._nBlue = blue;
	this._sRGB = (red < 16 ? '0' : '') + red.toString(16) + (green < 16 ? '0' : '') + green.toString(16) + (blue < 16 ? '0' : '') + blue.toString(16);
	red /= 255;
	green /= 255;
	blue /= 255;
	var minValue = Math.min(red, green, blue);
	var maxValue = Math.max(red, green, blue);
	var value = maxValue - minValue;
	this._nLight = (maxValue + minValue) / 2;
	if (!value)
	{
		this._nHue = 0;
		this._nSaturation = 0;
	}
	else
	{
		var h = red == maxValue ? (green - blue) / 6 / value : (green == maxValue ? 1 / 3 + (blue - red) / 6 / value : 2 / 3 + (red - green) / 6 / value);
		this._nHue = h < 0 ? h += 1 : (h > 1 ? h -= 1 : h);
		this._nSaturation = this._nLight < 0.5 ? value / (maxValue + minValue) : value / (2 - maxValue - minValue);
	}
}
Color.prototype.setHSL = function(hue, saturation, light)
{
	this._nHue = hue;
	this._nSaturation = saturation;
	this._nLight = light;
	var maxValue = light + Math.min(light, 1 - light) * saturation;
	var minValue = 2 * light - maxValue;
	this._nRed = this._Hue2RGB(minValue, maxValue, hue + 1 / 3);
	this._nGreen = this._Hue2RGB(minValue, maxValue, hue);
	this._nBlue = this._Hue2RGB(minValue, maxValue, hue - 1 / 3);
	this._sRGB = (this._nRed < 16 ? "0" : "") + this._nRed.toString(16).toUpperCase() + (this._nGreen < 16 ? "0" : "") + this._nGreen.toString(16).toUpperCase() + (this._nBlue < 16 ? "0" : "") + this._nBlue.toString(16).toUpperCase();
}

/*
HTMLPaletteForm - 调色板窗体

类属性
_CROSS - 十字交叉线
_ARROW - 左箭头
_aTexts - 文本标签名称组
_aParameters - 基本控制参数
_aBasicRGBValue - 缺省的颜色组

属性
_oPalette - 色调饱和度选择区, 其中包含_oCross属性表示色调饱和度选择区的十字线图标
_oLightbar - 亮度条选择区, 内部包含_aLines保存每一行的信息, 包含_oArrow属性表示亮度条箭头
_oBasicColors - 基本色彩区
_oCustomColors - 自定义色彩区
_oColorArea - 颜色区, 其中包含一个_oPreview属性, 保存颜色预览框控件
_oHue - 色调
_oSaturation - 饱和度
_oLight - 亮度
_oRed - 红色值
_oGreen - 绿色值
_oBlue - 蓝色值
_oApply - 确认按钮
_oCancel - 取消按钮
_oAddto - 新增按钮
_oBorder - 色彩选中框

方法
_PaletteMouseDown - 调色板鼠标点击事件, 设置十字线光标为拖动状态
_PaletteDragStart - 调色板十字线光标的拖动开始事件, 隐藏十字线光标
_PaletteDragMove - 调色板十字线光标的拖动移动事件
_PaletteDragEnd - 调色板十字线光标的拖动结束事件, 显示十字线光标
_LightbarMouseDown - 亮度条鼠标点击事件, 设置箭头为拖动状态
_LightbarDragMove - 亮度条箭头的拖动移动事件
_ColorMouseDown - 颜色块点击事件, 设置调色板的颜色
_InputKeyDown - 颜色值输入框按键事件
_ApplyClick - 确定, 触发onApplyClick事件, 这个事件在show函数中传递
_CancelClick - 不选择, 关闭选择框
_AddtoClick - 添加一个颜色进入自定义颜色组
show - 参数表示是否要定义确认时触发的事件, 如果不定义继续使用上次的定义, 如果要取消请传入参数null
getValue - 获得颜色字符串, 6字节, 按rrggbb的顺序
setColor - 设置色彩
onApplyClick - 确认颜色选择事件
*/
function HTMLPaletteForm(style)
{
	var i;
	style = this.getStyle();

	this._oPalette = new HTMLComponent(style.getChild("#palette"));
	this._oPalette.clearEffect(this.EFFECT_ALL);
	this._oPalette.setParent(this);
	this._oPalette.capture();
	this._oPalette.onMouseDown = this._PaletteMouseDown;
	this._oPalette._oCross = new HTMLIcon();
	this._oPalette._oCross.setParent(this._oPalette);
	this._oPalette._oCross.setPosition(0, 0, "absolute");
	this._oPalette._oCross.fill(HTMLPaletteForm._CROSS);
	this._oPalette._oCross.onDragStart = this._PaletteDragStart;
	this._oPalette._oCross.onDragMove = this._PaletteDragMove;
	this._oPalette._oCross.onDragEnd = this._PaletteDragEnd;

	this._oLightbar = new HTMLComponent(style.getChild("#lightbar"));
	this._oLightbar.setParent(this);
	this._oLightbar._aLines = new Array();
	var width = this._oLightbar.getWidth() - 5;
	for (i = this._oLightbar.getHeight() - 4; i >= 3; i--)
	{
		var element = this._oLightbar.getBodyElement().create("SPAN");
		element.style.overflow = "hidden";
		element.style.position = "absolute";
		element.style.width = width;
		element.style.height = 1;
		element.style.left = 0;
		element.style.top = i;
		this._oLightbar._aLines[i - 3] = element;
	}
	this._oLightbar.capture();
	this._oLightbar.onMouseDown = this._LightbarMouseDown;
	this._oLightbar._oArrow = new HTMLIcon();
	this._oLightbar._oArrow.setParent(this._oLightbar);
	this._oLightbar._oArrow.setPosition(0, 0, "absolute");
	this._oLightbar._oArrow.fill(HTMLPaletteForm._ARROW);
	this._oLightbar._oArrow.onDragMove = this._LightbarDragMove;
	this._oLightbar._oArrow.onDragEnd = this._LightbarDragEnd;

	var name, text, o;

	for (i = 0; i < 2; i++)
	{
		name = HTMLPaletteForm._aParameters[i * 2];
		text = new HTMLComponent(style.getChild("#" + name.toLowerCase()));
		text.setParent(this);
		text.getBodyElement().innerHTML = HTMLPaletteForm._aTexts[i + 1];
		var n = HTMLPaletteForm._aParameters[i * 2 + 1];
		o = new HTMLGrid(n, 8, style.getChild("#grid"));
		o.setPosition(text.getX(), text.getY() + text.getHeight(), "absolute");
		o.setParent(this);
		n *= 8;
		for (var j = 0, item; item = o.getItem(j); j++)
		{
			if (i)
			{
				item._oColor = new Color("FFFFFF");
				item.getOutLineElement().style.backgroundColor = "#FFFFFF";
			}
			else
			{
				item._oColor = new Color(HTMLPaletteForm._aBasicRGBValue[j]);
				item.getOutLineElement().style.backgroundColor = "#" + item._oColor.getRGB();
			}
		}
		o.onMouseDown = this._ColorMouseDown;
		this["_o" + name] = o;
	}
	this._oCustomColors._nIndex = 0;

	this._oColorArea = new HTMLEdit(style.select("#color #edit"));
	this._oColorArea.setParent(this);
	this._oColorArea.getInputElement().readOnly = true;
	this._oColorArea._oPreview = new HTMLControl(style.getChild("#color"));
	this._oColorArea._oPreview.setParent(this);

	for (i = 0; i < 6; i++)
	{
		name = HTMLPaletteForm._aParameters[i + 4];
		text = new HTMLComponent(style.getChild("#" + name.toLowerCase()));
		text.setParent(this);
		text.getBodyElement().innerHTML = HTMLPaletteForm._aTexts[i + 3];
		o = new HTMLEdit(text.getStyle().getChild("#edit"));
		o.setParent(this);
		o._bHSL = i < 3;
		o.onKeyDown = this._InputKeyDown;
		o.onKeyPress = HTMLWindows.cancel;
		this["_o" + name] = o;
	}

	for (i = 10; i < 13; i++)
	{
		name = HTMLPaletteForm._aParameters[i];
		o = new HTMLButton(style.getChild("#" + name.toLowerCase()));
		o.setText(HTMLPaletteForm._aTexts[i - 1]);
		o.setParent(this);
		this["_o" + name] = o;
	}
	this._oApply.onClick = this._ApplyClick;
	this._oCancel.onClick = this._CancelClick;
	this._oAddto.onClick = this._AddtoClick;

	this._oBorder = new HTMLControl(style.select("#grid #border"));
	this._oBorder.cancel();
	this._oBorder.getOuterElement().style.zIndex = -1;

	this.setTitle(HTMLPaletteForm._aTexts[0]);
	this.hide();
}
HTMLPaletteForm.prototype.show = function(handle)
{
	_super.show();
	if (handle !== undefined)
	{
		this.onApplyClick = handle;
	}
}
HTMLPaletteForm.extendsOf(HTMLForm);
HTMLPaletteForm._CROSS = [[0, -5, 1, 5], [1, 0, 5, 1], [0, 1, 1, 5], [-5, 0, 5, 1]];
HTMLPaletteForm._ARROW = [[0, 3, 1, 1], [1, 2, 1, 3], [2, 1, 1, 5], [3, 0, 1, 7]];
HTMLPaletteForm._aParameters = ["BasicColors", 6, "CustomColors", 1, "Hue", "Saturation", "Light", "Red", "Green", "Blue", "Apply", "Cancel", "Addto"];
HTMLPaletteForm._aBasicRGBValue = [
	"FF8080", "FFFF80", "80FF80", "00FF80", "80FFFF", "0080F0", "FF80C0", "FF80FF",
	"FF0000", "FFFF00", "80FF00", "00FF40", "00FFFF", "0080C0", "8080C0", "FF00FF",
	"804040", "FF8040", "00FF00", "008080", "004080", "8080FF", "800040", "FF0080",
	"800000", "FF8000", "008000", "008040", "0000FF", "0000A0", "800080", "8000FF",
	"400000", "804000", "004000", "004040", "000080", "000040", "400040", "400080",
	"000000", "808000", "808040", "808080", "408080", "C0C0C0", "400040", "FFFFFF"
];
HTMLPaletteForm._aValueNames = ["Hue", "Saturation", "Light", "Red", "Green", "Blue"];
HTMLPaletteForm.prototype._PaletteMouseDown = function(event)
{
	var pos = this.getOuterElement().getPosition();
	var form = this.getParent();
	var height = form._oLightbar.getHeight() - 7;
	form.setColor((event.pageX - pos.left) / (height + 1), 1 - (event.pageY - pos.top) / height, form._oLight.getValue() / height);
	HTMLWindows.drag(this._oCross, event);
}
HTMLPaletteForm.prototype._PaletteDragStart = function()
{
	this.hide();
}
HTMLPaletteForm.prototype._PaletteDragMove = function(event, offsetX, offsetY)
{
	var parent = this.getParent();
	var x = this.getX() + offsetX;
	var y = this.getY() + offsetY;
	var width = parent.getWidth() - 1;
	var height = parent.getHeight() - 1;
	if (x < 0)
	{
		event.setDragReturnX(event.pageX - x);
		x = 0;
	}
	else if (x >= width)
	{
		event.setDragReturnX(event.pageX - x + width);
		x = width;
	}
	if (y < 0)
	{
		event.setDragReturnY(event.pageY - y);
		y = 0;
	}
	else if (y >= height)
	{
		event.setDragReturnY(event.pageY - y + height);
		y = height;
	}
	var form = parent.getParent();
	form.setColor(x / (width + 1), 1 - y / height, form._oLight.getValue() / height);
	return false;
}
HTMLPaletteForm.prototype._PaletteDragEnd = function()
{
	this.show();
}
HTMLPaletteForm.prototype._LightbarMouseDown = function(event)
{
	var form = this.getParent();
	var height = form._oLightbar.getHeight() - 7;
	form.setColor(form._oHue.getValue() / (height + 1), form._oSaturation.getValue() / height, 1 - (event.pageY - this.getOuterElement().getPosition().top - 3) / height);
	HTMLWindows.drag(this._oArrow, event);
}
HTMLPaletteForm.prototype._LightbarDragMove = function(event, offsetX, offsetY)
{
	var parent = this.getParent();
	var y = this.getY() + offsetY;
	var height = parent.getHeight() - 7;
	if (y < 0)
	{
		event.setDragReturnY(event.pageY - y);
		y = 0;
	}
	else if (y >= height)
	{
		event.setDragReturnY(event.pageY - y + height);
		y = height;
	}
	var form = parent.getParent();
	form.setColor(form._oHue.getValue() / (height + 1), form._oSaturation.getValue() / height, 1 - y / height);
	return false;
}
HTMLPaletteForm.prototype._ColorMouseDown = function()
{
	var parent = this.getParent();
	var form = parent.getParent();
	var index = this.getIndex();
	form._oBorder.setPosition(parent.getX() + (index % 8) * this.getWidth(), parent.getY() + Math.floor(index / 8) * this.getHeight());
	if (parent === form._oCustomColors)
	{
		form._oCustomColors._nIndex = this._nIndex;
	}
	form.setColor(this._oColor);
	form._oBorder.setParent(form);
}
HTMLPaletteForm.prototype._InputKeyDown = function(event, text)
{
	var i = text.start.length;
	var value;
	while (true)
	{
		if (text.select.length > 0)
		{
			if (event.which == 46 || event.which == 8)
			{
				if (!isOP)
				{
					value = text.start + text.end;
					break;
				}
				return false;
			}
		}
		else if (event.which == 46)
		{
			if (!isOP && text.end.length > 0)
			{
				value = text.start + text.end.substring(1);
				break;
			}
			return false;
		}
		else if (event.which == 8)
		{
			if (!isOP && text.start.length > 0)
			{
				value = text.start.substring(0, i - 1) + text.end;
				i--;
				break;
			}
			return false;
		}
		if (event.which == 37)
		{
			if (!isOP)
			{
				this.setCaret(i - 1);
			}
			return false;
		}
		else if (event.which == 39)
		{
			if (!isOP)
			{
				this.setCaret(i + 1);
			}
			return false;
		}
		else if (event.which < 48 || event.which > 57)
		{
			return false;
		}
		else
		{
			value = text.start + String.fromCharCode(event.which) + text.end;
			i++;
		}
		break;
	}
	if (value === "")
	{
		value = 0;
		i = 1;
	}
	else
	{
		value = parseInt(value);
	}
	var parent = this.getParent();
	var height = parent._oLightbar.getHeight() - 7;
	if (value >= 0 && value <= 255)
	{
		this.setValue(value);
		if (this._bHSL)
		{
			parent.setColor(parent._oHue.getValue().number() / (height + 1), parent._oSaturation.getValue().number() / height, parent._oLight.getValue().number() / height);
		}
		else
		{
			parent.setColor(new Color(parent._oRed.getValue().number(), parent._oGreen.getValue().number(), parent._oBlue.getValue().number()));
		}
		this.setCaret(i);
	}
	return false;
}
HTMLPaletteForm.prototype._ApplyClick = function()
{
	var form = this.getParent();
	if (form.onApplyClick)
	{
		form.onApplyClick(form.getValue());
	}
	form.hide();
}
HTMLPaletteForm.prototype._CancelClick = function()
{
	this.getParent().hide();
}
HTMLPaletteForm.prototype._AddtoClick = function()
{
	var form = this.getParent();
	var o = form._oCustomColors.getItem(form._oCustomColors._nIndex);
	o._oColor = new Color(form._oColorArea.getValue());
	o.getOutLineElement().style.backgroundColor = "#" + o._oColor.getRGB();
	form._oCustomColors._nIndex = (form._oCustomColors._nIndex + 1) % (HTMLPaletteForm._aParameters[3] * 8);
}
HTMLPaletteForm.prototype.getValue = function()
{
	return this._oColorArea.getValue();
}
HTMLPaletteForm.prototype.setColor = function(color)
{
	var hue, saturation, light;

	if (color.constructor === Color)
	{
		hue = color.getHue();
		saturation = color.getSaturation();
		light = color.getLight();
	}
	else
	{
		hue = color;
		saturation = arguments[1];
		light = arguments[2];
		color = new Color();
		color.setHSL(hue, saturation, light);
	}
	this._oColorArea._oPreview.getBodyElement().style.backgroundColor = "#" + color.getRGB();
	this._oColorArea.setValue(color.getRGB());
	this._oRed.setValue(color.getRed());
	this._oGreen.setValue(color.getGreen());
	this._oBlue.setValue(color.getBlue());
	var height = this._oLightbar.getHeight() - 7;
	var H = Math.round(hue * 256);
	var S = Math.round(saturation * 255);
	var L = Math.round(light * 255);
	if (H !== this._oLightbar._nHue || S !== this._oLightbar._nSaturation)
	{
		for (var i = 0; i <= height; i++)
		{
			color.setHSL(hue, saturation, 1 - i / height);
			this._oLightbar._aLines[i].style.backgroundColor = color.getRGB();
		}
		this._oLightbar._nHue = H;
		this._oLightbar._nSaturation = S;
	}
	this._oHue.setValue(H);
	this._oSaturation.setValue(S);
	this._oLight.setValue(L);
	this._oPalette._oCross.setPosition(H, height - S);
	this._oLightbar._oArrow.setPosition(this._oLightbar.getWidth() - 4, height - L);
}
if (wsclLanguage == "zh-cn")
{
	HTMLPaletteForm._aTexts = ["调色板", "基本颜色", "自定义颜色", "色调", "饱和度", "亮度", "红", "绿", "蓝", "确定", "取消", "添加到自定义颜色"];
}
