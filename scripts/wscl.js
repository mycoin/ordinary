/*
Author: 欧阳先伟(Xianwei Ouyang)
Version Beta 0.1, 2008/02/--
allskystar@hotmail.com
*/

/*
命名方式：
类内部使用的变量使用_开头，之后按匈牙利命名法命名，使用字母标识(s-字符串,n-数字,f-函数,e-Element,b-布尔)类型，请不要直
接对其进行操作，提供给外部的接口函数直接使用单词开头
*/

/*
全局变量初始化
*/
isIE = !!document.all;
isFF = !!window.find;
isOP = !!window.opera;

/**
 * 将源对象的属性成员复制到目标对象
 * @public
 * 
 * @param {Object} toObj 目标对象
 * @param {Object} fromObj 源对象
 */
function _extend(toObj, fromObj) {
    for (var prop in fromObj) {
        toObj[prop] = fromObj[prop];
    }
}

/*
Service类管理数据提交以及回传处理，Service实现了提交的同步阻塞，但浏览器的异步执行。Service能自动识别并采用最佳的AJAX
提交方式，如果浏览器不支持XMLHttpRequest或者需要提交的数据有上传文件，将采用IFrame的方式来提交，框架自动生成的IFrame窗
体名称为_AJAXIFrame，请避免重名。

AJAX调用的构造函数示例：
new Service(document.data)或new Service('data')

普通调用的构造函数示例：
new Service(document.data, {ajax: false})或new Service('data', {ajax: false})

如果是简单的提交一个url并使用链接直接传入少量参数，也可以不指定form，而省略成：
new Service().submit(url)

一个完整的调用示例：
var s = new Service(document.data);
s.onReceive = function (value)
{
    alert(value);
}
s.submit();

类属性
_eIForm  - 简单提交时内置的表单数据对象
_oXHR    - XMLHttpRequest提交时的操作对象
_oIFrame - IFrame提交时的操作对象
_aWait   - 处于等待中的提交请求列表

属性
_eForm   - 表单form标签对象
_bShare  - 是否在form上进行逻辑共享
_sInfo   - 执行数据提交前的确认提示信息
_sUrl    - form的目标地址缓存，如果在submit时指定了url，将会设置这个值
_fSubmit - 用于进行提交处理操作的函数
*/

/**
 * 创建一个数据提交操作对象
 * @public
 * 
 * @param {Element|String} form 数据操作对象对应的表单元素，或者是表单名字，将自动转换
 * @param {Object} prof 数据提交管理参数，属性说明： 
 *                      ajax    - 是否启用ajax方式提交，默认值为是(ajax提交)
 *                      share   - 是否同一个form逻辑上共享Service(相同form认为是同一个Service)，默认值为是(共享)
 *                      confirm - 提交前是否产生二次确认信息，如果只是移除的二次确认，将confirm的值设置为true
 */
function Service(form, prof) {
    prof = prof || {};

    /* 得到form标签元素 */
    form
    ? form.constructor == String && (form = document.forms[form] || G(form))
    : (form = Service._eIForm || (Service._eIForm = G(document.body).create('form')));
    if (!form) {
        throw "The form can't be resolved";
    }

    /* 检查form内元素的类型 */
    for (var i = 0, textCount = 0, list = form.elements, o; o = G(list[i]); i++) {
        var type = o.type;
        /* 数据中包含上传的文件时，采用IFrame提交 */
        if (type == 'file') {
            form.enctype = 'multipart/form-data';
            form.method = 'post';
        }
        else if (type == 'text') {
            textCount++;
        }
    }
    if (textCount == 1) {
        /* 在表单中仅包含一个text时，需要动态创建一个text，阻止form能够直接使用回车键提交数据 */
        var el = G(form).create('input');
        el.css('display', 'none');
        el.disabled = true;
    }

    this._eForm = form;
    this._fSubmit =
        prof.ajax !== false
        ? form.enctype == 'multipart/form-data' ? this._getIFrameSubmit() : this._getAjaxSubmit()
        : Service._fNormalSubmit;
    this._bShare = prof.share !== false;
    this._sInfo = prof.confirm === true ? WSCLMessage.AFFIRM_REMOVE : prof.info;
}

_extend(Service, {

    /**
     * 判断系统当前是否处于提交状态
     * @public
     *
     * @return 系统是/否处于提交状态
     */
    isSubmitting: function () {
        return this._aWait.length > 0;
    },

    _aWait: [],

    prototype: {

        /**
         * 请求提交数据到服务器，如果数据对象已经处于提交状态，中止提交，如果有请求正在提交，将阻塞到列表中 
         * @public
         *
         * @param {String} url 指定特定提交的地址
         */
        submit: function (url) {
            if (Validate(this._eForm.elements)) {
                /* 数据合法性验证通过，进行二次确认 */
                var info = this._sInfo;
                if (info && !confirm(info)) {
                    return;
                }

                /* 判断当前操作对象是否已经位于提交队列中 */
                for (var i = 0, list = Service._aWait, o; o = list[i]; i++) {
                    if (o == this || o._bShare && this._bShare && o._eForm == this._eForm) {
                        return;
                    }
                }

                /* 将当前的Service放入提交队列，如果提交队列中没有其它的Service，直接提交 */
                this._sUrl = url || null;
                list.length || this._submit();
                list.push(this);
            }
        },

        /**
         * 提交数据
         * @private
         */
        _submit: function () {
            var form = this._eForm;
            var url = this._sUrl;
            if (url) {
                this._sUrl = form.action;
                form.action = url;
            }
            try {
                this._fSubmit(form);
            }
            catch (e) {
                alert(WSCLMessage.ERR_SUBMIT);
            }
        },

        /**
         * 普通form表单提交函数，直接提交数据到服务器
         * @private
         */
        _fNormalSubmit: function (form) {
            form.submit();
        },

        /**
         * 获取AJAX方式的提交函数，如果浏览器不支持XMLHttpRequest，将自动获取IFrame方式的提交函数
         * @private
         */
        _getAjaxSubmit: function () {
            if (!Service._fAJAXSubmit) {
                try {
                    var xmlHttpRequest = new ActiveXObject('Msxml2.XMLHTTP');
                }
                catch(e) {
                    try {
                        xmlHttpRequest = new ActiveXObject('Microsoft.XMLHTTP');
                    }
                    catch(e) {
                        xmlHttpRequest = new XMLHttpRequest();
                    }
                }
                Service._fAJAXSubmit = xmlHttpRequest ? this._fXHRSubmit : this._getIFrameSubmit();
                Service._oXHR = xmlHttpRequest;
            }
            return Service._fAJAXSubmit;
        },

        /**
         * XMLHttpRequest提交函数
         * @private
         */
        _fXHRSubmit: function (form) {

            /* 设置提交参数 */
            var xmlHttpRequest = Service._oXHR;
            xmlHttpRequest.open('POST', form.action, true);
            xmlHttpRequest.onreadystatechange = this._fXHRReceive;
            xmlHttpRequest.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
            
            /* 获取要提交的数据 */
            var data = [];
            for (var i = 0, list = form.elements, o; o = list[i]; i++) {
                var name = o.name;
                if (name && !o.disabled) {
                    var type = o.type;
                    if (type == 'hidden' || type == 'textarea' || type == 'text' || type == 'select-one'
                        || type == 'password' || ((type == 'radio' || type == 'checkbox') && o.checked)) {
                        data.push(name + '=' + encodeURIComponent(o.value));
                    }
                    else if (type == 'select-multiple') {
                        for (var j = 0, options = o.options, option; option = options[j]; j++) {
                            option.selected && data.push(name + '=' + encodeURIComponent(option.value));
                        }
                    }
                }
            }
            xmlHttpRequest.send(data.join('&'));
        },

        /**
         * XMLHttpRequest提交操作，对返回结果的处理函数
         * @private
         */
        _fXHRReceive: function () {
            var xmlHttpRequest = Service._oXHR;
            if (xmlHttpRequest.readyState == 4) {
                var o = Service._aWait[0];
                if (xmlHttpRequest.status == 200) {
                    try {
                        o._receive(xmlHttpRequest.responseText);
                        return;
                    }
                    catch (e) {
                    }
                }
                o._error();
            }
        },

        /**
         * 获取IFrame方式的提交函数
         * @private
         */
        _getIFrameSubmit: function () {
            if (!Service._fIFrameSubmit) {
                /* 创建IFrame */
                var frame = G(document.body).create('IFRAME');
                frame.css('display', 'none');
                frame.onload = frame.onreadystatechange = this._fIFrameReceive;

                /* 等待IFrame对应的窗体对象初始化完毕 */
                frame = frames[frames.length - 1];
                while (!frame.window);
                frame.name = '_AJAXIFrame';

                Service._fIFrameSubmit = this._fIFrameSubmit;
                Service._oIFrame = frame;
            }
            return Service._fIFrameSubmit;
        },

        /**
         * IFrame提交函数
         * @private
         */
        _fIFrameSubmit: function (form) {
            form.target = '_AJAXIFrame';
            form.submit();
        },

        /**
         * IFrame提交操作，对返回结果的处理函数
         * @private
         */
        _fIFrameReceive: function () {
            var o = Service._aWait[0], doc = Service._oIFrame.document, state = doc.readyState;
            try {
                (!state || state == 'complete') && doc.body && o._receive(doc.body.innerHTML);
            }
            catch (e) {
                o._error();
            }
        },

        /**
         * 单次数据请求操作结束，从等待队列中移除这个数据
         * @private
         */
        _end: function () {
            var url = this._sUrl;
            if (url !== null) {
                this._eForm.action = url;
                this._sUrl = null;
            }
            var list = Service._aWait;
            list.splice(0, 1);

            /* 如果队列中还有等待的提交请求，选择第一个继续提交 */
            list.length && list[0]._submit();
        },

        /**
         * 对数据请求得到的结果进行处理
         * @private
         *
         * @param {String} text 得到的文本数据
         */
        _receive: function (text) {
            this.onReceive ? this.onReceive(text) : execScript(text);
            this._end();
        },

        /**
         * 数据请求发生错误时的处理
         * @private
         */
        _error: function () {
            this.onError && this.onError.apply(this, arguments);
            this._end();
        }
    }
});

/**
 * 验证表单元素的格式，属性trim不等于no时会过滤数据两端的空白字符；属性pattern表示需要验证的正则表达式，会自动在当前正
 * 则表达式前后添加^与$表示行首与行尾的转义；属性maxValue,minValue表示数据允许的最大、最小数值；属性maxLen,minLen表示
 * 数据允许的最大与最小长度，属性char不等于false时表示长度验证基于字符，否则基于字节；属性custom表示自定义的验证处理，
 * 这里填入的是用于验证的函数名称，函数参数一是当前需要验证的值，参数二是当前验证项的名称；属性message表示数据验证失败
 * 时需要显示的错误信息。例如：
 * <input name="email" pattern="[A-Za-z][\w.]*@[A-Za-z][\w.]*" maxLen="64" trim="yes" />
 * <input name="year" minValue="1990" maxValue="2020" message="请填入1990-2020之间的年份" />
 * 如果未设置invalid属性，验证失败时提示信息在输入框中生成，如果设置了invalid属性，则会在指定的Element中显示出错信息，
 * 或者是调用指定的函数处理出错信息。
 * @public
 *
 * @param {Element|Array} e 表单元素或者是需要验证的表单元素数组
 * @return {Boolean} 是/否验证通过
 */
function Validate(e) {
    if (e.length >= 0) {
        var o = true;
        for (var i = 0, el; el = e[i]; i++) {
            /* 一组元素中存在一个元素数据验证失败，整体的返回值也是失败 */
            Validate(el) || (o = false);
        }
        return o;
    }

    if (e._sValue !== void(0)) {
        /* 在INPUT元素中直接显示的错误提示信息还没有撤消，直接返回不成功 */
        return false;
    }
    var name = e.name;
    /* INTPU元素没有名称或者处于不可用状态，将不进行验证 */
    if (name && !e.disabled) {

        e = G(e);
        var value = e.value;
        e.get('trim') != 'no' && (value = value.trim());
        var msg = 
            Validate._pattern(value, name, e.get('pattern'))
            || Validate._number(value, name, e.get('maxValue'), e.get('minValue'))
            || Validate._length(value, name, e.get('maxLen'), e.get('minLen'), e.get('char') != 'false')
            || Validate._custom(value, name, e.get('custom'));

        if (msg) {
            /* 验证中产生了错误 */
            if (!e.onInvalid) {
                /* 如果没有初始化错误处理，需要进行初始化 */
                var invalid = e.get('invalid');
                if (invalid) {
                    o = G(invalid);
                    if (o) {
                        /* 如果有同名的Element，则设置Element的innerHTML值 */
                        e._eMsg = o;
                        e.onInvalid = Validate._invalidToElement;
                        e.onValid = Validate._validToElement;
                    }
                    else {
                        /* 如果没有同名的Element，调用函数处理 */
                        e.onInvalid = window[invalid];
                        e.onValid = e.onInvalid.valid;
                    }
                }
                else {
                    /* 没有指定特殊的验证失败时的处理，直接将错误信息显示在自身 */
                    e.onInvalid = Validate._invalidToSelf;
                }
            }
            try {
                e.onInvalid(e.get('message') || msg);
            }
            catch (e) {
            }
            return false;
        }
        e.value = value;
        /* 验证成功时需要恢复状态 */
        e.onValid && e.onValid();
    }
    return true;
}

_extend(Validate, {

    /**
     * 查找一个标签下的所有表单控件
     * @public
     *
     * @param {Element} el 标签对象
     * @return {Array} 标签对象下的所有表单控件列表
     */
    getFormElements: function (el) {
        var result = [];
        for (var i = 0, list = el.getElementsByTagName('*'), o; o = list[i]; i++) {
            var name = o.tagName;
            (name == 'INPUT' || name == 'TEXTAREA' || name == 'SELECT') && result.push(o);
        }
        return result;
    },

    /**
     * 对数据进行正则表达式验证
     * @private
     *
     * @param {String} value 要验证的字符串
     * @param {String} name 字符串名称
     * @param {String} regexp 正则表达式字符串
     * @return {String} 如果有值返回的是错误提示信息
     */
    _pattern: function (value, name, regexp) {
        if (regexp && !value.match(new RegExp('^' + regexp + '$'))) {
            return WSCLMessage.ERR_INVAILD.format(name);
        }
    },

    /**
     * 对数据进行整数值表达式验证
     * @private
     *
     * @param {String} value 要验证的字符串
     * @param {String} name 字符串名称
     * @param {String} max 允许的最大数值，如果为空表示没有最大值限制
     * @param {String} min 允许的最小数值，如果为空表示没有最小值限制
     * @return {String} 如果有值返回的是错误提示信息
     */
    _number: function (value, name, max, min) {
        if (max || min) {
            /* 首先验证格式是否为纯整数的 */
            if (!value.match(/^[0-9]+$/)) {
                return WSCLMessage.ERR_INVAILD.format(name);
            }
            else {
                value = value.number();
                if (max && value > max.number()) {
                    return WSCLMessage.ERR_MAXIMUL.format(name, max);
                }
                if (min && value < min.number()) {
                    return WSCLMessage.ERR_MINIMUL.format(name, min);
                }
            }
        }
    },

    /**
     * 对数据进行长度验证
     * @private
     *
     * @param {String} value 要验证的字符串
     * @param {String} name 字符串名称
     * @param {String} max 允许的最大长度，如果为空表示没有最大长度限制
     * @param {String} min 允许的最小长度，如果为空表示没有最小长度限制
     * @param {Boolean} character 是/否以字符为基本单位，否的时候以字节为基本单位
     * @return {String} 如果有值返回的是错误提示信息
     */
    _length: function (value, name, max, min, character) {
        if (max || min) {
            var length = character ? value.length : value.count();
            if (max && length > max.number()) {
                return character
                    ? WSCLMessage.ERR_MOST_CHAR.format(name, max)
                    : WSCLMessage.ERR_MOST_LETTER.format(name, max);
            }
            if (min && length < min.number()) {
                return length 
                    ? (
                        character 
                        ? WSCLMessage.ERR_LEAST_CHAR.format(name, min)
                        : WSCLMessage.ERR_LEAST_LETTER.format(name, min)
                    )
                    : WSCLMessage.ERR_REQUIRED.format(name);
            }
        }
    },

    /**
     * 自定义验证调用
     * @private
     *
     * @param {String} value 要验证的字符串
     * @param {String} name 字符串名称
     * @param {String} funcName 自定义验证函数名
     * @return {String} 如果有值返回的是出错提示信息
     */
    _custom: function (value, name, funcName) {
        if (funcName) {
            return window[funcName](value, name);
        }
    },

    /**
     * 验证失败将错误信息写到指定的Element中
     * @private
     *
     * @param {String} msg 验证错误的信息
     */
    _invalidToElement: function (msg) {
        this._eMsg.innerHTML = msg;
    },

    /**
     * 验证成功清除指定元素的错误信息
     * @private
     */
    _validToElement: function () {
        this._eMsg.innerHTML = '';
    },

    /**
     * 验证失败将错误信息写到自身的value属性中
     * @private
     *
     * @param {String} msg 验证错误的信息
     */
    _invalidToSelf: function (msg) {
        this._sValue = this.value;
        this.addClass('invalid');
        this.value = msg;
        this.onfocus = Validate._focus;
    },

    /**
     * 鼠标移入时恢复原来输入的值
     * @private
     */
    _focus: function () {
        this.removeClass('invalid');
        var value = this._sValue;
        /* 恢复原来的值 */
        if (value !== void(0)) {
            this.value = value;
            this._sValue = void(0);
        }
    }
});

/*
在使用框架的OOP继承体系时，首先要定义类，然后执行extendsOf编译类。在类的方法中使用_super关键字引用父类，使用_this关键
字引用当前类定义的方法。所有使用了_super或_this关键字的方法必须定义在extendsOf之前，其它未使用这两个关键字的方法建议定
义在extendsOf调用之后，不需要编译，可以加快初始化的速度。例如：
function extend2()
{
    _super();
}
Extend2.prototype.setValue = function (value)
{
    _super.setValue(value);
    alert('Extend2:' + value);
}
Extend2.extendsOf(Extend1);
类继承树的根为Object。对象可以设定一个自动运行的初始化代码，名称与对象名称相同，如：
Extend2.Extend2 = function () {...}
如果对象的初始化代码不存在，将自动寻找父类的初始化代码用于初始化对象。

属性
_oSuperClass - 父类
_sClassName  - 类名
_aAbstract  - 虚构属性列表
*/

_extend(Function, {

    /**
     * 空的函数实例，不执行任何操作
     * @public
     */
    blank: new Function(),

    /**
     * 检查函数是否存在未实现的基类虚方法
     * @private
     * 
     * @return {Boolean} 函数是/否存在未实现的基类虚方法
     */
    _checkAbstract: function () {
        var list = this._aAbstract, name;
        if (list) {
            while (name = list.pop()) {
                if (!this[name]) {
                    throw 'The type ' + this.constructor._sClassName
                        + ' muse implement the inherited abstract method: ' + name;
                }
            }
        }
    },

    /**
     * 函数内容格式化，去除符号两端的空格
     * @private
     * 
     * @param {String} text 函数内容文本
     * @return {String} 格式化后的文本
     */
    _format: function (text) {
        return text.replace(/\s+/g, ' ')
            .replace(/ ?(^|$|;|:|<|>|\?|,|\.|\/|\{|\}|\[|\]|\-|\+|\=|\(|\)|\*|\^|\%|\&|\~|\!|\|) ?/g, '$1');
    }
});

_extend(Function.prototype, {

    /**
     * 指定当前类继承一个父类，在这个过程中将会编译类的所有属性，参数为父类的引用
     * @public
     * 
     * @param {Class} superClass 当前类的父类
     */
    extendsOf: function (superClass) {
        /* 检查父类是否在类继承树中，如果不在，将父类继承自Object */
        !superClass._sClassName && superClass.extendsOf(Object);
        this._oSuperClass = superClass;

        /* 编译类的全部函数 */
        this._compile();
        for (var name in this.prototype) {
            this._compile(name);
        }

        /* 设置新生成的类 */
        var me = window[this._sClassName];
        me._oSuperClass = superClass;
        var o = me.prototype;

        /* 复制父类中子类没有实现的函数和属性 */
        for (name in superClass.prototype) {
            o[name] || (o[name] = superClass.prototype[name]);
        }

        /* 复制初始化方法，形式如Object.Object */
        for (var clazz = this; ; clazz = clazz._oSuperClass) {
            o = clazz[clazz._sClassName];
            if (o) {
                me[me._sClassName] = o;
                return;
            }
        }
    },

    /**
     * 给当前类指定一个虚函数，子类必须实现这个函数，否则将不能初始化
     * @public
     * 
     * @param {String} name 虚函数的名称
     */
    declareAbstract: function (name) {
        var prototype = this.prototype;
        var list = prototype._aAbstract;
        list ? list.push(name) : (prototype._aAbstract = [name]);
    },

    /**
     * 判断对象是否为当前类的实例
     * @public
     * 
     * @param {Object} obj 待判断的对象
     * @return {Boolean} 是/否为当前类的实例
     */
    isInstance: function (obj) {
        if (obj) {
            /* 对象为空的时候永远都是false */
            for (var clazz = obj.constructor; clazz; clazz = clazz._oSuperClass) {
                if (clazz == this) {
                    return true;
                }
            }
        }
        return false;
    },

    /**
     * 获得类的名称
     * @public
     * 
     * @return {String} 类的名称
     */
    getName: function () {
        return this._sClassName;
    },

    /**
     * 获得当前类的父类
     * @public
     * 
     * @return {Function} 当前类的父类
     */
    getSuperClass: function () {
        return this._oSuperClass;
    },

    /**
     * 编译函数
     * @private
     * 
     * @param {String} name 函数属性名称，如果为空表示编译构造函数，否则编译属性函数
     */
    _compile: function (name) {
        var text;
        /* 检查是类的构造函数还是类的属性，name参数为空表示是构造函数 */
        if (name) {
            /* 类的属性不是函数实现，直接赋值到子类后退出 */
            var o = this.prototype[name];
            if (!(o instanceof Function)) {
                window[this._sClassName].prototype[name] = o;
                return;
            }
            text = o.toString();
        }
        else {
            text = this.toString();
        }
        var r = [];
        /* 过滤空白字符 */
        var i = 0, j;
        text.replace(
            / *("([^"^\\]|\\")*"|'([^'^\\]|\\')*'|\/([^\/^\\]|\\.)*\/) */g,
            function (word, $1, $2, $3, $4, index) {
                r.push(Function._format(text.substring(i, index)));
                r.push(word.trim());
                i = index + word.length;
            }
        );
        r.push(Function._format(text.substring(i)));
        text = r.join('');
        i = text.indexOf('(');
        name || (this._sClassName = text.substring(9, i));
        j = text.indexOf(')', i);
        var className = this._sClassName;
        var superClassName = this._oSuperClass._sClassName;
        var args = text.substring(i + 1, j).split(',');
        text = text.substring(j + 2, text.length - 1);

        /* 进行调用转义，将_super,_this替换为指定的方法 */
        /* j记录是否需要自动补充父类的构造函数 */
        i = 0, j = false;
        text = text.replace(
            /_(super|this)(\.[^\(]+|\[[^\]]+\])?\(([^\)]*)\)/g,
            function ($0, key, word, args, index) {
                if (word) {
                    name || i || (j = true);
                    word = '.prototype' + word;
                }
                else if (key == 'this') {
                    /* JS函数不区分参数的差异，构造函数不允许递归调用自身 */
                    throw 'Constructor call mustn\'t be "_this()" in a constructor';
                }
                else if (name || index) {
                    throw 'Constructor call must be the first statement in a constructor';
                }
                else {
                    word = '';
                }
                i++;
                return (
                    key == 'this'
                    ? className : superClassName) + word + (args ? '.call(this,' + args + ')'
                    : '.apply(this,arguments)'
                );
            }
        );
        var o = window[className].prototype;
        if (!i) {
            if (name) {
                /* 没有针对_this,_super的调用，不用编译 */
                o[name] = this.prototype[name];
                return;
            }
            j = true;
        }
        /* 没有对父类构造函数的调用时，自动添加 */
        j && (text = superClassName + '.apply(this,arguments);' + text);
        /* 编译结果赋值 */
        if (name) {
            args.push(text);
            o[name] = Function.apply(null, args);
        }
        else {
            args.push(text + 'if(this.constructor==' + className + '){Function._checkAbstract.call(this);'
                        + className + '.' + className + '.apply(this,arguments);}');
            o = Function.apply(null, args);
            o._sClassName = className;
            window[className] = o;
        }
    }
});

/*
核心类为OOP提供的支持
*/
Object._sClassName = 'Object';
Object.Object = Function.blank;

/*
设置execScript函数
*/
isIE || (execScript = eval);

/*
Number原型扩展
*/
Number.prototype.number = function () {
	return this;
};

/*
String原型扩展
*/
_extend(String.prototype, {

    /**
     * 去除字符串两端的空白字符
     * @public
     * 
     * @return {String} 处理过的字符串
     */
    trim: function () {
        return this.replace(/(^[\s\u3000\xa0]+|[\s\u3000\xa0]+$)/g, '');
    },

    /**
     * 格式化字符串，将'{数字}'串转换为对应参数，生成新的字符串
     * 例如'{1}-{0}'.format('3','4')生成的结果为'4-3'
     * @public
     * 
     * @return {String} 格式化后的字符串
     */
    format: function () {
        var args = Array.prototype.slice.call(arguments);
        return this.replace(/\{([0-9]+)\}/g, function ($0, word) {
            return args[word.number()] || '';
        });
    },

    /**
     * 将一个字符串重复拼接指定的次数
     * 例如'abc'.repeat(3)的结果为'abcabcabc'
     * @public
     *
     * @param {Number} n 需要重复的次数
     * @return {String} 拼接生成的字符串
     */
    repeat: function (n) {
        var result = [];
        while (n--) {
            result.push(this);
        }
        return result.join('');
    },

    /**
     * 将一个字符串转换成数值
     * @public
     *
     * @param {Number} radix 整数传换进制
     * @return {Number} 字符串转换成的数值，无法转换返回0
     */
    number: function (radix) {
        var result = radix ? parseInt(this, radix) : parseFloat(this);
        return isNaN(result) ? 0 : result;
    },

    /**
     * 判断字符串是否以某个字符串开始
     * @public
     *
     * @param {String} s 开始的字符串
     * @return {Boolean} 是否以s开始
     */
    startWith: function (s) {
        return !this.indexOf(s);
    },

    /**
     * 判断字符串是否以某个字符串结尾
     * @public
     *
     * @param {String} s 结尾的字符串
     * @return {Boolean} 是否以s结尾
     */
    endWith: function (s) {
        return this.lastIndexOf(s) == this.length - s.length;
    }
});

/*
Array原型扩展
*/
_extend(Array.prototype, {

    /**
     * 在数组中查找一个对象
     * @public
     *
     * @param {Object} o 需要查找的对象
     * @return {Number} 对象在数组中的序号，如果不存在，返回-1
     */
    indexOf: function (o) {
        for (var i = 0, l = this.length; i < l; i++) {
            if (o === this[i]) {
                return i;
            }
        }
        return -1;
    },

    /**
     * 在数组中删除一个对象
     * @public
     *
     * @param {Object} o 需要删除的对象
     */
    remove: function (o) {
        var i = this.length;
        while (i--) {
            o === this[i] && this.splice(i, 1);
        }
    }
});

/**
 * 延迟调用一个函数，从第4个参数起都是传入func中的变量
 * @public
 *
 * @param {Function} func 函数引用
 * @param {Number} delay 延迟的毫秒数
 * @param {Object} me 在func被执行时，this指针指向的对象
 * @return {Object} 延迟调用句柄，可以使用clearTimeout中止这个调用
 */
function Timeout(func, delay, me) {
    var args = Array.prototype.slice.call(arguments, 3);
    return setTimeout(function () {
        func.apply(me, args);
    }, delay);
}

/**
 * 事件包装类
 * @public
 *
 * @param {Event} event 接收事件对象的event参数
 * @return {Event} 标准化的事件对象
 */
function Event(event) {
    if (isIE) {
        event = window.event;
        var body = document.body;
        var html = body.parentNode;
        event.pageX = html.scrollLeft + body.scrollLeft + event.clientX - body.clientLeft;
        event.pageY = html.scrollTop + body.scrollTop + event.clientY - body.clientTop;
        event.target = event.srcElement;
        event.which = event.keyCode;
    }
    event.stop = Event.stop;
    return event;
}

/**
 * 事件停止冒泡
 * @public
 *
 * @param {Event} event 接收事件对象的event参数
 */
Event.stop = isIE ? function (event) {
    (event || this).cancelBubble = true;
} : function (event) {
    (event || this).stopPropagation();
};

/**
 * 获得dom对象的包装
 * @public
 * 
 * @param {String|Element} el element或element的id
 * @return {Element} dom元素
 */
function G(e) {
    if (e) {
        e.constructor == String && (e = document.getElementById(e));
        if (e) {
            if (!e._bWrap) {
                var prototype = G.prototype;
                for (var prop in prototype) {
                    e[prop] = prototype[prop];
                }
                e.get = e.getAttribute;
                e._bWrap = true;
            }
            return e;
        }
    }
}

G.prototype = {

    /**
     * 为Element添加class
     * @public
     *
     * @param {String} name class的名称
     */
    addClass: function (name) {
        var classes = this.className.split(/\s+/);
		if (classes.indexOf(name) < 0) {
			classes.push(name);
			this.className = classes.join(' ').trim();
		}
        return this;
    },

    /**
     * 判断Element是否包含class
     * @public
     * 
     * @param {String} name class的名称
     * @return 是/否包含class
     */
    containClass: function (name) {
        return this.className.split(/\s+/).indexOf(name) >= 0;
    },

    /**
     * 删除Element的class
     * 
     * @public
     * @param {String} name class的名称
     */
    removeClass: function (name) {
        var classes = this.className.split(/\s+/);
        classes.remove(name);
        this.className = classes.join(' ');
        return this;
    },

    /**
     * 为Element添加事件
     * @public
     *
     * @param {String} eventType 事件的类型
     * @param {Function} listener 事件的处理函数
     */
    addEvent: function(eventType, listener) {
        var oldEvent = this[eventType];
        if (oldEvent) {
            var listeners = oldEvent._aListener;
            if (listeners) {
                /* 已经存在监听器队列，在监听器队列中添加新的监听器 */
                listeners.push(listener);
                return;
            }
            else {
                /* 如果只有一个监听器，生成监听器队列 */
                listeners = [oldEvent, listener];
                listener = function (event) {
                    for (var i = 0, result, listener; listener = listeners[i]; i++) {
                        result = listener.call(this, event, result);
                    }
                    return result;
                };
                listener._aListener = listeners;
            }
        }
        this[eventType] = listener;
    },

    /**
     * 为Element删除事件
     * @public
     *
     * @param {String} eventType 事件的类型
     * @param {Function} listener 事件的处理函数，如果不指定函数将移除整个事件队列
     */
    removeEvent: function(eventType, listener) {
        var oldEvent = this[eventType];
        if (oldEvent) {
            var listeners = oldEvent._aListener;
            if (listeners) {
                listeners.remove(listener);
                /* 检查监听器队列，如果没有了要释放，如果只有1个需要解除队列 */
                listeners.length <= 1 && (this[eventType] = listeners[0]);
            }
            else if (oldEvent == listener) {
                this[eventType] = null;
            }
        }
    },

    /**
     * 建立子Element
     * @public
     * 
     * @return {Element} 建立的子Element对象
     */
    create: function (name) {
        return this.appendChild(G(document.createElement(name || 'div')));
    },

    /**
     * 从页面中移除Element
     * @public
     */
    remove: function () {
        var parent = this.parentNode;
        parent && parent.removeChild(this);
        return this;
    },

    /**
     * 在当前元素与它的子元素之间插入一个指定的元素
     * @public
     *
     * @param {String} name 待插入的元素名称，默认为div
     */
    insert: function (name) {
        var el = this.create(name);
        while (this.firstChild != el) {
            el.appendChild(this.removeChild(this.firstChild));
        }
        return el;
    },

    /**
     * 移除Element内部的空文本节点
     * @public
     */
    clearEmptyNode: function () {
        for (var e = this.lastChild; e; e = e.previousSibling) {
            e.nodeType != 1 && (e.nodeType != 3 || !e.nodeValue.trim()) && this.removeChild(e);
        }
    },

    /**
     * 获得Element的绝对位置
     * @public
     * 
     * @return {Object} 属性left表示X轴坐标，属性top表示Y轴坐标 
     */
    getPosition: function () {
        var e = this, left = e.offsetLeft, top = e.offsetTop;
        while (e = G(e.offsetParent)) {
            if (e.tagName == 'HTML') {
                break;
            }
            if (e.css('position') == 'absolute') {
                left += e.nCss('borderLeftWidth');
                top += e.nCss('borderTopWidth');
            }
            left += e.offsetLeft;
            top += e.offsetTop;
        }
        return  {'left': left, 'top': top};
    },

    /**
     * 获得Element所有指定名称的子Element
     * @public
     * 
     * @param {String} name 子Element名称，如果为空表示所有
     * @return {Array} 子Element列表
     */
    getChildren: function (name) {
        name = name && name.toUpperCase();
        var result = [];
        for (var el = this.firstChild; el; el = el.nextSibling) {
            var tagName = el.tagName;
            tagName && (!name || tagName == name) && result.push(el);
        }
        return result;
    },

    /**
     * Element的样式操作
     * <pre>
     * <b>设置css：</b>
     * .css('width', '500px')
     * .css({width : '500px', backgroundColor : '#ccc'})
     * .css('width', '500px', 'backgroundColor', '#ccc')
     * <b>获取css：</b>
     * .css('width')
     * </pre>
     * @public
     * 
     * @return {Object} css对象或者是指定的属性值
     */
    css: function (arg) {
        var argLen = arguments.length;
        if (argLen < 2) {
            /* 在IE下，Element没有在文档树上时，没有currentStyle属性 */
            var style = this.currentStyle || (isIE ? this.style : getComputedStyle(this, null));
            /* 没有参数，返回当前样式 */
            if (!argLen) {
                return style;
            }
            /* 一个字符串参数，取出样式 */
            if (arg.constructor == String) {
                arg = this._css[arg] || arg;
                /* 提供针对某些名称的特殊取出方式，在_css中定义 */
                return arg.constructor == String ? style[arg] : arg.get.call(style, this);
            }
            else {
                /* 对象参数，将对象的值设置到style中 */
                for (var k in arg) {
                    this.css(k, arg[k]);
                }
            }
        }
        else if (argLen == 2) {
            /* 两个参数，第一个参数为样式名称，第二个参数为样式值，设置的扩展方式与获取的扩展方式类似 */
            arg = this._css[arg] || arg;
            arg.constructor == String ? this.style[arg] = arguments[1] : arg.set.call(this.style, arguments[1]);
        }
        else {
            /* 多个参数，两个一组进行设置 */
            for (var i = 1; i < argLen; i += 2) {
                this.css(arguments[i - 1], arguments[i]);
            }
        }
        return this;
    },

    /**
     * Element的样式表示的数值
     * @public
     * 
     * @return {Number} 指定的样式属性数值
     */
    nCss: function (name) {
        return this.css(name).number();
    },

    /**
     * 获得边框样式的左右宽度和
     * @public
     *
     * @return {Number} 边框样式的左右宽度和
     */
    getBorderWidth: function () {
        return this.nCss('borderLeftWidth') + this.nCss('borderRightWidth');
    },

    /**
     * 获得边框样式的上下高度和
     * @public
     *
     * @return {Number} 边框样式的上下高度和
     */
    getBorderHeight: function () {
        return this.nCss('borderTopWidth') + this.nCss('borderBottomWidth');
    },

    /**
     * 获得内填充样式的左右宽度和
     * @public
     *
     * @return {Number} 内填充样式的左右宽度和
     */
    getPaddingWidth: function () {
        return this.nCss('paddingLeft') + this.nCss('paddingRight');
    },

    /**
     * 获得内填充样式的上下高度和
     * @public
     *
     * @return {Number} 内填充样式的上下高度和
     */
    getPaddingHeight: function () {
        return this.nCss('paddingTop') + this.nCss('paddingBottom');
    },

    /*
     * Element的样式操作详细设定，键名表示需要操作的CSS名称，如果键值是字符串，表示CSS名称的别名如果不是字符串, 表示定
     * 义了具体的set与get操作函数
     */
    _css: {
        'float': isIE ? 'styleFloat' : 'cssFloat',
        'opacity': isIE ? {
            set: function (value) {
                this.visibility = value < 0.001 ? 'hidden' : 'visible';
                this.filter = (value == 1) ? '' : 'alpha(opacity=' + value * 100 + ')';
            },
            get: function (e) {
                return e.filters[0].Opacity / 100;
            }
        } : {
            set: function (value) {
                this.visibility = value < 0.001 ? 'hidden' : 'visible';
                this.opacity = value;
            },
            get: function () {
                return this.opacity;
            }
        }
    }
};

/*
加载框架
*/
var WSCLPath, WSCLCharset, WSCLLanguage;
(function () {
    for (var i = 0, list = document.getElementsByTagName('script'), o; o = G(list[i]); i++) {
        var path = o.src;
        var index = path.lastIndexOf('/') + 1;
        if (path.substring(index) == 'wscl.js') {
            WSCLPath = o.get('WSCLPath') || path.substring(0, index);
            WSCLCharset = o.get('WSCLCharset') || (isIE ? document.charset : document.characterSet).toLowerCase();
            WSCLLanguage = o.get('WSCLLanguage') || (isIE ? navigator.userLanguage : navigator.language).toLowerCase();
            return;
        }
    }
})();
