/*
日历窗体插件

Author: 欧阳先伟(Xianwei Ouyang)
Version Beta 0.1, 2008/02/--
allskystar@hotmail.com
*/

/*
HTMLCalendarForm - 日历窗体

属性
_aTexts - 文本标签名称组

方法
_SetYear - 设置一个年份, 必须在1950-2050年之间
_MonthChange - 选择不同的月份事件处理
_YearUpClick - 点击后退一年按钮事件处理
_YearDownClick - 点击前进一年按钮事件处理
_DayClick - 点击某一天事件处理, 将触发onApplyClick事件
show - 参数表示是否要定义确认时触发的事件, 如果不定义继续使用上次的定义, 如果要取消请传入参数null
setDate - 设置当前需要显示的年月日历
onApplyClick - 确认日期选择事件
*/
function HTMLCalendarForm(style)
{
	style = this.getStyle();

	this._oMonth = new HTMLSelect(style.getChild("#month"));
	this._oMonth.add("一月", "一月");
	this._oMonth.add("二月", "二月");
	this._oMonth.add("三月", "三月");
	this._oMonth.add("四月", "四月");
	this._oMonth.add("五月", "五月");
	this._oMonth.add("六月", "六月");
	this._oMonth.add("七月", "七月");
	this._oMonth.add("八月", "八月");
	this._oMonth.add("九月", "九月");
	this._oMonth.add("十月", "十月");
	this._oMonth.add("十一月", "十一月");
	this._oMonth.add("十二月", "十二月");
	this._oMonth.setParent(this);
	this._oMonth.onChange = this._MonthChange;

	this._oYear = new HTMLEdit(style.getChild("#year"));
	this._oYear.setParent(this);
	this._oYear.getInputElement().readOnly = true;
	this._oYear._oUp = new HTMLSymbol(style.select("#year #up"));
	this._oYear._oUp.fill(HTMLCalendarForm.UP);
	this._oYear._oUp.setParent(this);
	this._oYear._oUp.onClick = this._YearUpClick;
	this._oYear._oDown = new HTMLSymbol(style.select("#year #down"));
	this._oYear._oDown.fill(HTMLCalendarForm.DOWN);
	this._oYear._oDown.setParent(this);
	this._oYear._oDown.onClick = this._YearDownClick;

	this._oCalendar = new HTMLCalendar(style.getChild("#calendar"));
	this._oCalendar.setParent(this);
	this._oCalendar.onDayClick = this._DayClick;

	this.setTitle(HTMLCalendarForm._aTexts[0]);
}
HTMLCalendarForm.prototype.show = function(handle)
{
	_super.show();
	if (handle !== undefined)
	{
		this.onApplyClick = handle;
	}
}
HTMLCalendarForm.extendsOf(HTMLForm);
HTMLCalendarForm.UP = [[1, 0, 1], [0, 1, 3]];
HTMLCalendarForm.DOWN = [[0, 0, 3], [1, 1, 1]];
HTMLCalendarForm.prototype._SetYear = function(year)
{
	if (year >= 1950 && year <= 2050)
	{
		this._oCalendar.setDate(year, this._oCalendar.getMonth());
		this._oYear.setValue(year);
	}
}
HTMLCalendarForm.prototype._MonthChange = function(index)
{
	var form = this.getParent();
	form._oCalendar.setDate(form._oCalendar.getYear(), index + 1);
}
HTMLCalendarForm.prototype._YearUpClick = function()
{
	var form = this.getParent();
	form._SetYear(form._oYear.getValue().toInt() - 1);
}
HTMLCalendarForm.prototype._YearDownClick = function()
{
	var form = this.getParent();
	form._SetYear(form._oYear.getValue().toInt() + 1);
}
HTMLCalendarForm.prototype._DayClick = function(year, month, day)
{
	var form = this.getParent();
	if (form.onApplyClick)
	{
		form.onApplyClick(year, month, day);
	}
	form.hide();
}
HTMLCalendarForm.prototype.setDate = function(year, month)
{
	var date = new Date();
	if (!year)
	{
		year = date.getYear();
	}
	if (!month)
	{
		month = date.getMonth() + 1;
	}
	this._oYear.setValue(year);
	this._oMonth.onChange = null;
	this._oMonth.selectedIndex(month - 1);
	this._oMonth.onChange = this._MonthChange;
	this._oCalendar.setDate(year, month);
}
if (wsclLanguage == "zh-cn")
{
	HTMLCalendarForm._aTexts = ["日期选择"];
}
