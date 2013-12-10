/*
性能优化历史：
1.使用innerHTML代替分次生成(从15秒降至5秒)
2.使用fastcreate代替create(从5秒降至3.5秒)
3.去除G函数的for in操作(从3.5秒到3秒)
4.单行使用一个控件(从3秒到2秒)
5.初始化时传递一个数组给_getHTML方法(从2秒到1.95秒)
6.取消所有的G函数调用(从1.95秒到1.5秒)
7.不要一次所有节点都可用，使用display:none让一些节点默认不可见(未实现)

TODO list:
1.Tab,EUI日历,双日历,调色板支持,进度条
2.布局器支持
3.设置需要考虑实际控件大小最小值的问题，防止越界的错误
4.表格支持部分列锁定的功能，左右都需要提供锁定
5.与七巧板兼容
6.增加radio与checkbox的label
7.省略函数需要支持子标签的情况
8.Input支持输入限制，验证模块
9.日历需要支持今天的设置
10.ie下动态input不能改变name
11.日历需要提供选择功能的接口
.装饰器的百分比与具体数值设置的兼容处理(Done)
.提供后期关联功能(例如ECCheckbox中的superior:parent，如果parent不存在，能在parent建立时自动关联)(Done)
.交换框支持多选，自动上下滚动(Done)
*/

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
(function () {
    var userAgent = navigator.userAgent.toLowerCase(), index = userAgent.indexOf('msie');
    isOP = !!window.opera;
    isIE = index > 0 && !isOP;
    isIE && (IEVer = parseInt(userAgent.substring(index + 5)));
    isFF = userAgent.indexOf('firefox') > 0;
    isGG = userAgent.indexOf('chrome') > 0;
    isSF = !isFF && !isGG && userAgent.indexOf('safari') > 0;
})();

/**
 * 将源对象的属性成员复制到目标对象
 * @public
 * 
 * @param {Object} target 目标对象
 * @param {Object} source 源对象
 */
function copy(target, source) {
    for (var prop in source) {
        target[prop] = source[prop];
    }
}

/**
 * JS中的原型链类继承操作
 * @public
 *
 * @param {Function} subClass 子类
 * @param {Function} superClass 父类
 */
function extend(subClass, superClass) {
    function s() {};
    s.prototype = superClass.prototype;
    var prototype = new s();
    prototype.constructor = subClass;
    subClass.prototype = prototype;
    subClass.superclass = superClass;
}

/**
 * 判断一个对象是否为字符串类型
 * @public
 * 
 * @param {Object} obj 需要判断的对象
 * @return {Boolean} 是/否为字符串
 */
function isString(obj) {
    return typeof obj == 'string' || obj instanceof String;
}

/**
 * 判断一个对象是否为数值类型
 * @public
 * 
 * @param {Object} obj 需要判断的对象
 * @return {Boolean} 是/否为数值
 */
function isNumber(obj) {
    return typeof obj == 'number' || obj instanceof Number;
}

/*
Number原型扩展
*/
/**
 * 对象数值化，返回自己，为了与String的number()方法兼容
 * @public
 * 
 * @return {Number} 数值
 */
Number.prototype.number = function () {
    return this;
};

/*
String原型扩展
*/
copy(String.prototype, {

    /**
     * 去除字符串两端的空白字符
     * @public
     * 
     * @return {String} 过滤两端空格后的字符串
     */
    trim: function () {
        return this.replace(/(^[\s\u3000\xa0]+|[\s\u3000\xa0]+$)/g, '');
    },

    /**
     * 格式化字符串，将'{数字}'串转换为对应参数，生成新的字符串。例如'{1}-{0}'.format('3','4')生成的结果为'4-3'
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
     * 将一个字符串转换成数值，如果不指定radix得到的可能是浮点数
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
    },

    /**
     * 对一个字符串进行驼峰命名法格式化，即将-后的字符大写，同时去掉-符号
     * @public
     *
     * @return {String} 名字格式化的结果
     */
    toCamelCase: function () {
        return this.replace(/\-./g, function ($0) {
            return $0.substring(1).toUpperCase();
        });
    }
});

/*
Array原型扩展
*/
copy(Array.prototype, {

    /**
     * 在数组中查找一个对象，与字符串的indexOf操作类似
     * @public
     *
     * @param {Object} obj 需要查找的对象
     * @return {Number} 对象在数组中的序号，如果不存在，返回-1
     */
    indexOf: function (obj) {
        for (var i = 0, l = this.length; i < l; i++) {
            if (obj === this[i]) {
                return i;
            }
        }
        return -1;
    },

    /**
     * 在数组中删除一个对象，如果对象存在多次，将全部被删除
     * @public
     *
     * @param {Object} obj 需要删除的对象
     * @return {Object} 实际删除的对象，如果需要删除的对象不属于数组，返回void(0)
     */
    remove: function (obj) {
        var me = this, i = me.length, remove;

        while (i--) {
            if (obj === me[i]) {
                me.splice(i, 1);
                remove = obj;
            }
        }

        return remove;
    }
});

/**
 * 创建一个定时器，从第4个参数起都是传入func中的变量
 * @public
 *
 * @param {Function} func 定时器需要调用的函数
 * @param {Number} delay 定时器延迟调用的毫秒数
 * @param {Object} caller 调用者，在func被执行时，this指针指向的对象，可以为空
 */
function Timer(func, delay, caller) {
    var args = Array.prototype.slice.call(arguments, 3);
    this._nID = setTimeout(function () {
        func.apply(caller, args);
        args = null;
        caller = null;
    }, delay);
}

/**
 * 中止延迟调用操作
 * @public
 */
Timer.prototype.stop = function () {
    clearTimeout(this._nID);
};

/**
 * 事件包装类，模拟W3C标准填充事件的属性
 * @public
 *
 * @param {Event} event 事件对象
 * @return {Event} 标准化的事件对象
 */
function Event(event) {
    if (isIE) {
        event = window.event;
        var body = document.body, html = body.parentNode;
        event.pageX = html.scrollLeft + body.scrollLeft + event.clientX - body.clientLeft - html.clientLeft;
        event.pageY = html.scrollTop + body.scrollTop + event.clientY - body.clientTop - html.clientTop;
        event.target = event.srcElement;
        event.which = event.keyCode;
    }
    event.stop = Event.stop;
    event.cancel = Event.cancel;
    return event;
}

/**
 * 事件停止冒泡
 * @public
 *
 * @param {Event} event 事件对象
 */
Event.stop = function (event) {
    event = event || this;
    event.stopPropagation ? event.stopPropagation() : (event.cancelBubble = true);
};

/**
 * 事件停止默认处理
 * @public
 *
 * @param {Event} event 事件对象
 */
Event.cancel = function (event) {
    event = event || this;
    event.preventDefault ? event.preventDefault() : (event.returnValue = false);
};

/**
 * 包装浏览器相关参数，top,right,bottom,left分别表示当前可视区域上,右,下,左四个方向的坐标，width,height分别表示页面的
 * 实际宽度与高度
 * @public
 */
function Brower() {
    var doc = document, body = doc.body, html = body.parentNode,
        client = doc.compatMode == 'BackCompat' ? body : doc.documentElement,
        scrollTop = html.scrollTop + body.scrollTop, scrollLeft = html.scrollLeft + body.scrollLeft;

    return {
        top: scrollTop,
        right: client.clientWidth + scrollLeft,
        bottom: client.clientHeight + scrollTop,
        left: scrollLeft,
        width: Math.max(html.scrollWidth, body.scrollWidth, client.clientWidth),
        height: Math.max(html.scrollHeight, body.scrollHeight, client.clientHeight)
    };
}

/**
 * 建立一个Element
 * @public
 *
 * @param {Element} parent 父Element对象，可以为空不指定
 * @param {String} name Element的名称，默认为div
 * @return {Element} 创建的Element对象
 */
function E(parent, name) {
    name = document.createElement(name || 'div');
    parent && parent.appendChild(name);
    return name;
}

copy(E, {

    _eEmpty: E(),

    Cache: {},

    /**
     * 生成一个或者一组Element对象
     * @public
     *
     * @param {String} innerHTML Element对象的HTML内容
     * @return {Element|Array} Element对象或者Element对象数组
     */
    create: function (innerHTML) {
        var el = E._eEmpty, result = [];
        el.innerHTML = innerHTML;

        for (el = el.firstChild; el; el = el.nextSibling) {
            result.push(el);
        }

        return result.length < 2 ? result[0] : result;
    },

    /**
     * 为Element添加class
     * @public
     *
     * @param {Element} el Element对象
     * @param {String} name class的名称
     * @return {Element} Element对象
     */
    addClass: function (el, name) {
        var classes = el.className.split(/\s+/), list = name.split(/\s+/), i = list.length - 1;
        for (; i >= 0; i--) {
            name = list[i];
            classes.indexOf(name) < 0 && classes.push(name);
        }
        el.className = classes.join(' ').trim();
        return el;
    },

    /**
     * 删除Element的class
     * @public
     *
     * @param {Element} el Element对象
     * @param {String} name class的名称
     * @return {Element} Element对象
     */
    removeClass: function (el, name) {
        var classes = el.className.split(/\s+/), name = name.split(/\s+/), i = name.length - 1;
        for (; i >= 0; i--) {
            classes.remove(name[i]);
        }
        el.className = classes.join(' ');
        return el;
    },

    /**
     * 获取Element指定的属性
     * @public
     *
     * @param {Element} el Element对象
     * @param {String} name 属性名称
     * @return {Element} 属性的值
     */
    getAttr: function (el, name) {
        return el.getAttribute(name);
    },

    /**
     * 设置Element指定的属性
     * @public
     *
     * @param {Element} el Element对象
     * @param {String} name 属性名称
     * @param {String} value 属性的值
     * @return {Element} Element对象
     */
    setAttr: function (el, name, value) {
        el.setAttribute(name, value);
        return el;
    },

    /**
     * 获取Element的文本内容
     * @public
     *
     * @param {Element} el Element对象
     * @return {String} 文本内容
     */
    getText: function (el) {
        return isFF ? el.textContent : el.innerText;
    },

    /**
     * 设置Element的文本内容
     * @public
     *
     * @param {Element} el Element对象
     * @param {String} text 文本内容
     * @return {Element} Element对象
     */
    setText: function (el, text) {
        el[isFF ? 'textContent' : 'innerText'] = text;
        return el;
    },

    /**
     * 改变Element的父Element，如果不指定父Element，将Element从页面中移除
     * @public
     *
     * @param {Element} el Element对象
     * @param {Element} parent 父Element对象
     * @return {Element} Element对象
     */
    appendTo: function (el, parent) {
        var oldParent = el.parentNode;
        if (oldParent != parent) {
            oldParent && oldParent.removeChild(el);
            parent && parent.appendChild(el);
        }
        return el;
    },

    /**
     * 将Element插入另一个Element之后
     * @public
     *
     * @param {Element} el 需要插入的Element对象
     * @param {Element} target 目标Element对象
     * @return {Element} Element对象
     */
    insertAfter: function (el, target) {
        var nextSibling = target.nextSibling, parentNode = target.parentNode;
        nextSibling ? parentNode.insertBefore(el, nextSibling) : parentNode.appendChild(el);
        return el;
    },

    /**
     * 在当前元素与它的子元素之间插入一个div元素
     * @public
     *
     * @param {Element} el 原始的Element对象
     * @return {Element} 插入的Element对象
     */
    insert: function (el) {
        var newEl = E();
        while (el.firstChild) {
            newEl.appendChild(el.removeChild(el.firstChild));
        }
        return el.appendChild(newEl);
    },

    /**
     * 获得Element的绝对位置
     * @public
     * 
     * @param {Element} el 原始的Element对象
     * @return {Object} 属性left表示X轴坐标，属性top表示Y轴坐标 
     */
    getPosition: function (el) {
        var left = el.offsetLeft, top = el.offsetTop, style;
        while (el = el.offsetParent) {
            if (el.tagName == 'HTML') {
                break;
            }

            if (E.get(el, 'position') == 'absolute') {
                left += E.ncss(el, 'borderLeftWidth');
                top += E.ncss(el, 'borderTopWidth');
            }
            left += el.offsetLeft;
            top += el.offsetTop;
        }
        return  {left: left, top: top};
    },

    /**
     * 获得Element所有的下级Element
     * @public
     * 
     * @param {Element} el Element对象
     * @return {Array} 下级Element列表
     */
    getElements: function (el) {
        if (document.all) {
            for (var i = 0, result = [el], elements = el.getElementsByTagName('*'); el = elements[i]; i++) {
                el.nodeType == 1 && result.push(el);
            }
        }
        else {
            result = [];

            /* 深度优先算法遍历并填充所有Element节点 */
            (function find(el, result) {
                while (el) {
                    if (el.nodeType == 1) {
                        result.push(el);
                        find(el.firstChild, result);
                    }
                    el = el.nextSibling;
                }
            })(el, result);
        }

        return result;
    },

    /**
     * 获得Element中所有直接子Element对象
     * @public
     * 
     * @param {Element} el Element对象
     * @return {Array} 子Element序列
     */
    getChildren: function (el) {
        for (var result = [], el = el.firstChild; el; el = el.nextSibling) {
            el.nodeType == 1 && result.push(el);
        }
        return result;
    },

    /**
     * 获得Element中指定名称的第一个直接子Element对象
     * @public
     * 
     * @param {Element} el Element对象
     * @param {String} name 子Element名称
     * @return {Element} 指定名称的Element对象，如果找不到返回undefined
     */
    getFirstChild: function (el, name) {
        name = name.toUpperCase();
        for (var el = el.firstChild; el; el = el.nextSibling) {
            if (el.tagName == name) {
                return el;
            }
        }
    },

    /**
     * 获取Element的样式
     * @public
     * 
     * @param {Element} el Element对象
     * @param {String} name 样式的CSS名称，如果没有值，将返回整个样式对象
     * @return {Object} css对象或者是指定的属性值
     */
    get: function (el, name) {
        /* 快速处理已经在style节点上的属性 */
        if (name) {
            var value = el.style[name];
            if (value) {
                return value;
            }
            name = E._css[name] || name;
        }

        /* 在IE下，Element没有在文档树上时，没有currentStyle属性 */
        var style = el.currentStyle || (isIE ? el.style : getComputedStyle(el, null));
        return name ? isString(name) ? style[name] : name.get(el) : style;
    },

    /**
     * 设置Element的样式
     * @public
     * 
     * @param {Element} el Element对象
     * @param {String|Object} prop 样式的CSS名称，或者是CSS属性组，也可以是cssText的值(当value没有值的时候)
     * @param {String} value 样式的值，如果prop是一个属性组对象，这个值无效，否则表示属性的值
     * @return {Element} Element对象
     */
    set: function (el, prop, value) {
        if (isString(prop)) {
            if (value !== void(0)) {
                /* 两个参数，指定属性的值 */
                prop = E._css[prop] || prop;
                isString(prop) ? (el.style[prop] = value) : prop.set(el, value);
            }
            else {
                /* 一个参数，直接指定样式 */
                el.style.cssText = prop;
            }
        }
        else {
            /* prop指定了一组属性初始化，如果某个属性没有值，直接跳过 */
            for (var name in prop) {
                value = prop[name];
                value !== void(0) && E.set(el, name, value);
            }
        }
        return el;
    },

    /**
     * Element的样式表示的数值
     * @public
     * 
     * @param {Element} el Element对象
     * @return {Number} 指定的样式属性数值
     */
    ncss: function (el, name) {
        return E.get(el, name).number();
    },

    /**
     * 设置Element能作为绝对定位的offsetElement
     * @public
     * 
     * @param {Element} el Element对象
     * @return {Element} Element对象
     */
    offset: function (el) {
        E.get(el, 'position') != 'absolute' && E.set(el, 'position', 'relative');
    },

    /**
     * 获得边框样式的宽度修正值
     * @public
     *
     * @param {Element} el Element对象
     * @return {Number} 边框宽度修正值
     */
    getBorderWidth: function (el) {
        return E.ncss(el, 'borderLeftWidth') + E.ncss(el, 'borderRightWidth');
    },

    /**
     * 获得边框样式的高度修正值
     * @public
     *
     * @param {Element} el Element对象
     * @return {Number} 边框高度修正值
     */
    getBorderHeight: function (el) {
        return E.ncss(el, 'borderTopWidth') + E.ncss(el, 'borderBottomWidth');
    },

    /**
     * 获得内填充样式的宽度修正值
     * @public
     *
     * @param {Element} el Element对象
     * @return {Number} 内填充宽度修正值
     */
    getPaddingWidth: function (el) {
        return E.ncss(el, 'paddingLeft') + E.ncss(el, 'paddingRight');
    },

    /**
     * 获得内填充样式的高度修正值
     * @public
     *
     * @param {Element} el Element对象
     * @return {Number} 内填充高度修正值
     */
    getPaddingHeight: function (el) {
        return E.ncss(el, 'paddingTop') + E.ncss(el, 'paddingBottom');
    },

    /**
     * 获得样式内外区域的宽度修正值
     * @public
     *
     * @param {Element} el Element对象
     * @return {Number} 样式内外区域宽度修正值
     */
    getWidthRevise: function (el) {
        return E.getBorderWidth(el) + E.getPaddingWidth(el);
    },

    /**
     * 获得样式内外区域的高度修正值
     * @public
     *
     * @param {Element} el Element对象
     * @return {Number} 样式内外区域高度修正值
     */
    getHeightRevise: function (el) {
        return E.getBorderHeight(el) + E.getPaddingHeight(el);
    },

    /**
     * 计算textOverflow样式需要选择的字符
     * @private
     *
     * @param {Element} el Element对象
     * @param {Number} width 容器宽度
     * @param {Boolean} ellipsis 是否一定要出现省略号
     */
    _count: function (el, width, ellipsis) {
        /* 计算cache的名称 */
        var o = E.get(el), fontWeight = o.fontWeight, cacheName =
                'font-family:' + o.fontFamily + ';font-size:' + o.fontSize
                + ';word-spacing:' + o.wordSpacing + ';font-weight:' + (fontWeight.number() == 401 ? 700 : fontWeight)
                + ';font-style:' + o.fontStyle + ';font-variant:' + o.fontVariant,
            cache = E.Cache[cacheName];

        if (!cache) {
            o = E.appendTo(E._eEmpty, el);

            E.set(o, 'float:left;' + cacheName);
            cache = E.Cache[cacheName] = [];

            /* 计算ASCII字符的宽度cache */
            for (i = 0; i < 256; i++) {
                E.setText(o, String.fromCharCode(i));
                cache[i] = o.offsetWidth;
            }

            /* 计算非ASCII字符的宽度、字符间距、省略号的宽度 */
            E.setText(o, '一');
            cache[256] = o.offsetWidth;
            E.setText(o, '一一');
            cache[257] = o.offsetWidth - cache[256] * 2;
            cache[258] = cache['.'.charCodeAt(0)] * 3 + cache[257] * 3;

            E.set(o, '');
            o.innerHTML = '';
            E.appendTo(o);
        }

        for (
            /* wordWidth是每个字符或子节点计算之前的宽度序列 */
            var node = el.firstChild, charWidth = cache[256], wordSpacing = cache[257], ellipsisWidth = cache[258],
                wordWidth = [], ellipsis = ellipsis ? ellipsisWidth : 0;
            node;
            node = node.nextSibling
        ) {
            if (width < ellipsis) {
                E.appendTo(node);
            }
            else if (node.nodeType == 3) {
                for (var i = 0, text = node.nodeValue, length = text.length; i < length; i++) {
                    o = text.charCodeAt(i);
                    /* 计算增加字符后剩余的长度 */
                    wordWidth.push([width, node, i]);
                    width -= (i ? wordSpacing : 0) + (o < 256 ? cache[o] : charWidth);
                    if (width < ellipsis) {
                        break;
                    }
                }
            }
            else {
                o = node.tagName;
                if (o == 'IMG' || o == 'TABLE') {
                    /* 特殊元素直接删除 */
                    o = node;
                    node = node.previousSibling;
                    E.appendTo(o);
                }
                else {
                    wordWidth.push([width, node]);
                    width -= node.offsetWidth;
                }
            }
        }

        if (width < ellipsis) {
            /* 过滤直到能得到大于省略号宽度的位置 */
            while (o = wordWidth.pop()) {
                width = o[0];
                node = o[1];
                o = o[2];
                if (node.nodeType == 3) {
                    if (width >= ellipsisWidth) {
                        node.nodeValue = node.nodeValue.substring(0, o) + '...';
                        return true;
                    }
                    else if (!o) {
                        E.appendTo(node);
                    }
                }
                else if (E._count(node, width, true)) {
                    return true;
                }
                else {
                    E.appendTo(node);
                }
            }

            /* 能显示的宽度小于省略号的宽度，直接不显示 */
            el.innerHTML = '';
        }
    },

    /*
     * Element的样式操作详细设定，键名表示需要操作的CSS名称，如果键值是字符串，表示CSS名称的别名。如果不是字符串, 表示
     * 定义了具体的set与get操作函数集合。IE下对opacity的操作会清空其它的filter，请谨慎使用set(el, 'opacity', ...)
     */
    _css: {
        'float': isIE ? 'styleFloat' : 'cssFloat',

        'textOverflow': {
            set: function (el, value) {
                if (el.tagName == 'TD' || isFF) {
                    el._sHTML && (el.innerHTML = el._sHTML);

                    if (value == 'ellipsis') {
                        el._sHTML = el.innerHTML;
                        E._count(el, E.appendTo(E._eEmpty, el).offsetWidth);
                    }
                    else {
                        el._sHTML = '';
                    }
                }

                o = el.style;
                isOP ? (o.OTextOverflow = value) : isFF ? (el._sTextOverflow = value) : (o.textOverflow = value);
            },

            get: function (el) {
                var style = el.style;
                return (isOP ? style.OTextOverflow : isFF ? el._sTextOverflow: style.textOverflow) || 'clip';
            }
        },

        'opacity': isIE ? {
            set: function (el, value) {
                el.style.filter = (value == 1) ? '' : 'alpha(opacity=' + value * 100 + ')';
            },

            get: function (el) {
                return el.filters[0].Opacity / 100;
            }
        } : null
    }
});

/**
 * 获取Element对象
 * @public
 * 
 * @param {Element|String} el Element对象或Element对象的id
 * @return {Element} Element对象
 */
function G(el) {
    return isString(el) ? document.getElementById(el) : el;
}

/*
Author: 欧阳先伟(Xianwei Ouyang)
Version Beta 0.1, 2008/02/--
allskystar@hotmail.com
*/

/*
EC是控件库的环境初始化函数，它将网页初始化，遍历Element自动生成控件，并接管事件操作，被接管的事件包括：document对象的
onmousedown,onmouseover,onmousemove,onmouseout,onmouseup,onkeydown,onkeypress,onkeyup，以及document.body对象的unload，
请注意与其它框架集成时，因为事件接管引发的问题

类属性
_bFixPadding - 在计算宽度与高度时，是否需要修正内填充样式的影响
_bFixBorder  - 在计算宽度与高度时，是否需要修正边框样式的影响
_bFixOffset  - 在计算相对位置时，是否需要修正边框样式的影响
_nX          - 当前鼠标光标的X轴坐标
_nY          - 当前鼠标光标的Y轴坐标
_nKey        - 当前键盘按下的键值，解决keypress与keyup中得不到特殊按键的keyCode的问题
_eTarget     - 事件发生时对应的标签对象
_eMask       - 用于遮罩的层
_oOver       - 当前鼠标移入的对象
_oFocus      - 当前环境下拥有焦点的控件
_oPress      - 当前环境下被按压的控件
_oOutCache   - 鼠标移出事件对应的对象缓存，要在移入事件发生时才真正触发当前环境的移出事件
_oSelect     - 在zoom操作被触发时，如果不指定一个控件，则自动使用这个控件
_oNamed      - 所有被命名的控件的集合
_aControl    - 全部生成的控件，供释放控件占用的内存使用
_aStack      - 高优先级事件调用时，保存上一个事件环境的栈
_aConnect    - 控件初始化关联
*/

/**
 * 初始化浏览器，所有的控件必须在初始化后才能正常工作
 * @public
 *
 * @param {String} id 如果已经初始化，返回id指定的控件
 * @return {ECControl} id指定的控件
 */
function EC(id) {
    var ec = EC, el = document, body = el.body, named = ec._oNamed;
    if (!named) {
        ec._oNamed = named = {};

        /* 接管事件处理 */
        copy(el, ec.Document);

        isFF ? el.addEventListener('DOMMouseScroll', ec._mousewheel, false) : (el.onmousewheel = ec._mousewheel);

        body.onunload = ec._unload;
        window.onresize = ec._resize;

        /* 检测Element宽度与高度的计算方式 */
        el = E.set(E(body), 'width:8px;padding:1px;border:0px;position:relative');
        ec._bFixPadding = el.offsetWidth != 8;
        E.set(el, {padding: '0px', border: '1px solid'});
        ec._bFixBorder = el.offsetWidth != 8;
        ec._bFixOffset = E.set(E(el), 'left:0px;position:relative').offsetLeft;
        E.appendTo(el);

        /* 自动初始化所有节点 */
        ec.init(body);
    }
    return named[id];
}

copy(EC, {

    WEEKNAMES: ['日', '一', '二', '三', '四', '五', '六'],

    _aControl: [],

    _aStack: [],

    _aConnect: {},

    /**
     * 快速创建一个控件，省略判断等过程，用于框架内部快速生成控件对象。
     * @protected
     *
     * @param {Function} type 控件的构造函数
     * @param {Element} el 关联的Element对象或者Element对象的名称
     * @param {String} params 控件的初始化参数
     * @return {ECControl} 创建的控件对象
     */
    $fastCreate: function (type, el, params) {
        var controlList = EC._aControl, control;
        params.id = 'EC-' + controlList.length;

        control = new type(el.className.split(/\s+/)[0], el, params);
        control.onpaint = EC.cancel;
        control.$setParent(EC.findParent(el.parentNode));
        controlList.push(control);
        return control;
    },

    /**
     * 对一个Element及它的所有子Element自动初始化，即根据ec属性自动生成控件
     * @public
     *
     * @param {Element} el 需要初始化的Element
     */
    init: function (el) {
        /* 自动初始化控件 */
        for (var i = 0, elements = E.getElements(el), ec; el = elements[i]; i++) {
            ec = E.getAttr(el, 'eui');
            if (ec) {
                ec = ec.split(';');
                for (var params = {}, l = ec.length - 1; l >= 0; l--) {
                    var item = ec[l].split(':'), value = item[1];
                    if (value && (value = value.trim())) {
                        params[item[0].trim().toCamelCase()] = /^\d+$/.test(value)
                            ? value.number()
                            : value == 'true' ? true : value == 'false' ? false : value;
                    }
                }
                EC.create(params.type, null, null, el, params, params.id);
            }
        }
    },

    /**
     * 创建一个控件，控件自动注册，以便在刷新IE浏器时释放内存空间
     * @public
     *
     * @param {String} type 控件的类型
     * @param {ECControl|Element|Boolean} parent 父控件或父Element，如果有值或者这个值为true，会取消onpaint操作
     * @param {String} baseClass 控件使用的基本样式名称
     * @param {Element|String} el 关联的Element对象或者Element对象的名称
     * @param {Object} params 控件初始化参数
     * @param {String} id 控件的标识符
     * @return {ECControl} 创建的控件对象
     */
    create: function (type, parent, baseClass, el, params, id) {
        el = G(el);

        params = params || {};
        var list = EC._aControl, defaultClass = params.type, control, i = 0, connects = EC._aConnect;
        (!defaultClass || defaultClass == type) && (defaultClass = params.type = 'ec-' + type.toLowerCase());
        params.id = 'EC-' + list.length;

        /* 如果没有指定初始化控件，需要自己生成一个 */
        if (el) {
            if (el._oControl) {
                return el._oControl;
            }
        }
        else {
            el = E();
        }

        E.addClass(el, defaultClass);

        /* 如果没有指定基本样式，使用控件的样式作为基本样式 */
        baseClass ? E.addClass(el, baseClass) : (baseClass = el.className);
        baseClass = baseClass.split(/\s+/)[0];

        /* 生成并注册控件，调用创建控件的处理函数 */
        control = new window[('EC-' + type).toCamelCase()](baseClass, el, params);

        /* 指定了父控件的元素都是不需要自动刷新的 */
        parent && (control.onpaint = EC.cancel) && parent !== true
            ? control.setParent(parent)
            : control.$setParent(EC.findParent(control.getOuter().parentNode));
        control.$create(baseClass, el, params);
        id && (EC._oNamed[id] = control);

        list.push(control);

        /* 处理所有的关联操作 */
        if (list = connects[id]) {
            connects[id] = null;
            for (; id = list[i]; i++) {
                id.func.call(id.caller, control);
            }
        }

        return control;
    },

    /**
     * 关联两个控件，关联操作函数只支持一个参数，参数为被关联的控件
     * @public
     *
     * @param {ECControl} caller 关联操作的发起对象
     * @param {Function} func 关联操作函数
     * @param {String} targetId 目标控件的ID
     */
    connect: function (caller, func, targetId) {
        var target = EC._oNamed[targetId], connects = EC._aConnect;
        target
            ? func.call(caller, target)
            : (connects[targetId] || (connects[targetId] = [])).push({func: func, caller: caller});
    },

    /**
     * 获取宽度修正值，即计算padding,border对width样式的影响
     * @public
     *
     * @param {Element} el 需要修正的Element
     * @return {Number} 宽度修正值
     */
    getWidthRevise: function (el) {
        var width = 0;
        EC._bFixPadding && (width += E.getPaddingWidth(el));
        EC._bFixBorder && (width += E.getBorderWidth(el));
        return width;
    },

    /**
     * 获取高度修正值，即计算padding,border对height样式的影响
     * @public
     *
     * @param {Element} el 需要修正的Element
     * @return {Number} 高度修正值
     */
    getHeightRevise: function (el) {
        var height = 0;
        EC._bFixPadding && (height += E.getPaddingHeight(el));
        EC._bFixBorder && (height += E.getBorderHeight(el));
        return height;
    },

    /**
     * 获取X轴位置修正值，即计算border对offsetLeft属性的影响
     * @public
     *
     * @param {Element} el 需要修正的Element
     * @return {Number} X轴方向位置修正值
     */
    getXRevise: function (el) {
        el = E.get(el.offsetParent);
        return EC._bFixOffset && el.position != 'static' ? el.borderLeftWidth.number() : 0;
    },

    /**
     * 获取Y轴位置修正值，即计算border对offsetTop属性的影响
     * @public
     *
     * @param {Element} el 需要修正的Element
     * @return {Number} Y轴方向位置修正值
     */
    getYRevise: function (el) {
        el = E.get(el.offsetParent);
        return EC._bFixOffset && el.position != 'static' ? el.borderTopWidth.number() : 0;
    },

    /**
     * 获取框架的事件对象
     * @public
     *
     * @param {Event} event 事件函数的第一个参数，如果是IE，这里实际上没有值
     * @return {Event} 框架的事件对象，添加函数getTarget(获取触发事件的控件对象)
     */
    Event: function (event) {
        event = Event(event);
        var ec = EC, target = ec._eTarget;
        if (target) {
            /* 目标Element被取出来后，清除原来的目标 */
            event._oTarget = target._oControl;
            ec._eTarget = null;
        }

        ec._nX = event.pageX;
        ec._nY = event.pageY;
        ec._nKey = event.which || ec._nKey;
        event.getTarget = ec._getTarget;
        return event;
    },

    /**
     * 获取触发本次事件的控件对象，在Event上调用getTarget方法
     * @private
     *
     * @return {ECControl} 事件的控件对象
     */
    _getTarget: function () {
        return this._oTarget;
    },

    /**
     * 获取鼠标光标的X轴坐标
     * @public
     *
     * @return {Number} X轴坐标值
     */
    getMouseX: function () {
        return EC._nX;
    },

    /**
     * 获取鼠标光标的Y轴坐标
     * @public
     *
     * @return {Number} Y轴坐标值
     */
    getMouseY: function () {
        return EC._nY;
    },

    /**
     * 获取键盘的按键值
     * @public
     *
     * @return {Number} 按键值
     */
    getKey: function () {
        return EC._nKey;
    },

    /**
     * 获取当前拥有焦点的控件对象
     * @public
     *
     * @return {ECControl} 控件对象
     */
    getFocused: function () {
        return EC._oFocus;
    },

    /**
     * 获得当前处于按压状态的控件对象
     * @public
     *
     * @return {ECControl} 控件对象
     */
    getPressed: function () {
        return EC._oPress;
    },

    /**
     * 使控件获得焦点，如果需要获得焦点的控件与当前拥有焦点的控件在父对象上存在重合，则重合的部分不会产生事件，例如：
     * 层控件内部存在一个基本控件，如果当前层控件拥有焦点，然后点击基本控件，层控件不会产生任何事件。
     * @public
     *
     * @param {ECControl} control 控件对象，如果不指定，当前拥有焦点的控件将失去焦点
     */
    focus: function (control) {
        var focusList = [control], blurList = [EC._oFocus];
        EC._oddsParents(focusList, blurList);

        /* 对不重复的部分进行获得或失去焦点操作 */
        for (i = blurList.length - 1; i >= 0; i--) {
            blurList[i].blur();
        }
        for (var i = 0, o; o = focusList[i]; i++) {
            o.focus();
        }
        EC._oFocus = control;
    },

    /**
     * 使控件失去焦点，如果控件没有焦点不进行任何操作，否则将焦点设置给指定控件的父控件
     * @public
     *
     * @param {ECControl} control 控件对象
     */
    blur: function (control) {
        control.contain(EC._oFocus) && EC.focus(control.getParent())
    },

    /**
     * 将Element与控件绑定。元素需要先被绑定才能被框架捕获事件
     * @public
     *
     * @param {Element} el 待绑定的Element
     * @param {ECControl} control 绑定的控件对象
     */
    bind: function (el, control) {
        el._oControl = control;
        el.getControl = EC._getControl;
    },

    /**
     * 获取与当前Element绑定的控件，在Element上调用getControl方法
     * @private
     *
     * @return {ECControl} 与Element绑定的控件对象
     */
    _getControl: function () {
        return this._oControl;
    },

    /**
     * 查找指定Element相关联的最近的父控件，即指定的标签无法找到就查找它的父标签
     * @public
     *
     * @param {Element} el 待查找的Element
     * @return {ECControl} Element关联的父控件
     */
    findParent: function (el) {
        while (el && el.nodeType == 1) {
            var control = el._oControl;
            if (control) {
                return control;
            }
            el = el.parentNode;
        }
    },

    /*
     * 空函数，不执行任何处理
     * @public
     */
    blank: function () {
    },

    /*
     * 返回false，如果附着在事件上事件将不会执行本身的缺省处理方法，一般的使用方式同capture
     * @public
     *
     * @return {Boolean} false
     */
    cancel: function () {
        return false;
    },

    /**
     * 遮罩body区域
     * @public
     *
     * @param {Number} opacity 透明度，如果是void(0)表示取消
     */
    mask: function (opacity) {
        var mask = EC._eMask, brower = Brower();

        if (!mask) {
            mask = E(document.body);
            E.set(
                mask,
                'position:absolute;top:0px;left:0px;z-index:65535;background-color:#000;width:'
                + brower.width + 'px;height:' + brower.height + 'px'
            );
            EC._eMask = mask;
        }

        E.set(mask, opacity !== void(0) ? {display: '', opacity: opacity} : 'display', 'none');
    },

    /**
     * 标准的事件捕获处理函数，如果捕获成功返回true(捕获成功需要之前没有别的标签拦截这一次捕获，否则什么也不返回)，应
     * 该将它直接赋给实际的事件处理函数，如*.onmousedown = EC.capture，表示捕获这个标签上的鼠标按下事件，提交给可视化
     * 控件库的事件处理机制响应。(只有使用EC.bind绑定控件后的标签才能设置成捕获，否则将产生不可预知的异常，绑定后使用
     * getControl()获取需要触发事件的控件对象)
     * @public
     *
     * @return {Boolean} 如果捕获成功，返回true
     */
    capture: function () {
        if (!EC._eTarget) {
            EC._eTarget = this;
            return true;
        }
    },

    /**
     * 设置EC环境信息到一个对象中
     * @public
     *
     * @param {Object} data 环境描述对象，保存当前的鼠标光标位置与document上的鼠标事件等
     * @return {Object} 环境描述对象
     */
    _setEnvironment: function (data) {
        var doc = document;
        copy(data, {
            x: EC._nX,
            y: EC._nY,
            mouse: {
                onmousedown: doc.onmousedown,
                onmouseover: doc.onmouseover,
                onmousemove: doc.onmousemove,
                onmouseout: doc.onmouseout,
                onmouseup: doc.onmouseup
            }
        });
        return data;
    },

    /**
     * 自定义鼠标点击事件捕获，在不改变当前环境信息的前提下(不改变当前拥有焦点的控件)，捕获onmousedown事件。例如滚动条
     * 强制滚动时不能清空上级控件的控制信息，通过定义*.onmousedown = EC.custom来使用
     * @public
     */
    custom: function () {
        var doc = document, ec = EC, control = this._oControl, focus = ec._oFocus;
        /* 需要检查_oPress，判断之前是否有未正常释放的mouseup事件 */
        if (!ec._oPress && ec.capture.call(this)) {
            /* 保存调用前的环境 */
            ec._aStack.push(ec._setEnvironment({type: ec.custom}));
            doc.onmousedown = ec._customdown;
            doc.onmouseup = ec._customup;
        }
    },

    /**
     * 自定义的鼠标按键按下事件处理函数
     * @private
     *
     * @param {Event} event 浏览器事件对象(IE下为undefined)
     */
    _customdown: function (event) {
        event = EC.Event(event);

        var target = event._oTarget;
        EC._oPress = target;
        target.mousedown(event);
        target.pressstart(event);
        return false;
    },

    /**
     * 自定义的鼠标按键弹起事件处理函数，恢复标准的事件处理状态
     * @private
     *
     * @param {Event} event 浏览器事件对象(IE下为undefined)
     */
    _customup: function (event) {
        EC.restore();
        document.onmouseup(event);
    },

    Select: {

        /**
         * 改变选择框的大小
         * @private
         *
         * @param {Number} width 选择框的宽度
         * @param {Number} height 选择框的高度
         */
        $setSize: function (width, height) {
            ECControl.prototype.$setSize.call(this, width, height);
            var el = this.getOuter().firstChild;
            E.set(el, {
                width: Math.max(1, width - EC.getWidthRevise(el)) + 'px',
                height: Math.max(1, height - EC.getHeightRevise(el)) + 'px'
            });
        },

        /**
         * 选择框缩放开始事件，调用原始控件的onselectstart事件
         * @private
         *
         * @param {Event} event 事件对象
         */
        onzoomstart: function (event) {
            var press = this._oPress;
            return press.onselectstart && press.onselectstart(event);
        },

        /**
         * 选择框缩放事件，调用原始控件的onselect事件
         * @private
         *
         * @param {Event} event 事件对象
         */
        onzoommove: function (event) {
            var press = this._oPress;
            return press.onselect && press.onselect(event);
        },

        /**
         * 选择框缩放结束事件，调用原始控件的onselectend事件
         * @private
         *
         * @param {Event} event 事件对象
         */
        onzoomend: function (event) {
            var press = this._oPress;
            return press.onselectend && press.onselectend(event);
        }
    },

    /**
     * 将控件设置为缩放状态。调用它会触发控件对象的onzoomstart事件，在整个drag的周期中，还将触发onzoommove与onzoomend
     * 事件，在释放鼠标按键时缩放周期结束。这里所指的缩放，是指调用setSize操作。
     * @public
     *
     * @param {ECControl|String} control 需要被缩放的对象，或选择框的样式名称
     * @param {Event} event 鼠标点击事件
     * @param {Object} range 控件允许的缩放范围。minWidth, maxWidth, minHeight, maxHeight
     */
    zoom: function (control, event, range) {
        if (event.type == 'mousedown') {
            var ec = EC, doc = document, press = ec._oPress, data = {}, baseClass = control;

            if (!(control instanceof ECControl)) {
                control = ec._oSelect;
                if (!control) {
                    control = ec._oSelect = ec.create('Control', document.body);
                    copy(control, ec.Select);
                    E.set(
                        control.setHTML('<div class="select-box"></div>').getOuter(),
                        {overflow: 'hidden', position: 'absolute'}
                    );
                }

                /* 设置选择框的初始信息 */
                control.setPosition(ec._nX, ec._nY);
                control.setSize(1, 1);
                control.show();
                baseClass && control.setClass(baseClass);

                /* 保存对原始按压控件的引用 */
                control._oPress = press;
            }
            else {
                E.set(control.getOuter(), 'position', 'absolute');
            }

            /* 保存现场环境 */
            range && copy(data, range);
            copy(ec._setEnvironment(data), {
                type: ec.zoom,
                press: press,
                left: control.getX(),
                top: control.getY(),
                width: control.getWidth(),
                height: control.getHeight(),
                resize: window.onresize
            });
            ec._aStack.push(data);

            doc.onmousemove = ec._zoommove;
            doc.onmouseup = ec._zoomup;
            doc.onmouseover = doc.onmouseout = window.onresize = null;

            ec._oPress = control;
            isIE && doc.body.setCapture();
            control.onzoomstart && control.onzoomstart(event);
        }
    },

    /**
     * 缩放状态下的鼠标移动事件处理
     * @private
     *
     * @param {Event} event 鼠标移动事件
     */
    _zoommove: function (event) {
        event = EC.Event(event);
        var press = EC._oPress, undefined = void(0), math = Math, data = EC._aStack, data = data[data.length - 1],
            x = EC._nX, width = data.width, expectWidth = data.width = x - data.x + width,
            y = EC._nY, height = data.height, expectHeight = data.height = y - data.y + height,
            minWidth = data.minWidth, maxWidth = data.maxWidth,
            minHeight = data.minHeight, maxHeight = data.maxHeight;

        data.x = x;
        data.y = y;
        x = data.left;
        y = data.top;

        width = minWidth !== undefined ? math.max(minWidth, expectWidth) : expectWidth;
        height = minHeight !== undefined ? math.max(minHeight, expectHeight) : expectHeight;
        width = maxWidth !== undefined ? math.min(maxWidth, width) : width;
        height = maxHeight !== undefined ? math.min(maxHeight, height) : height;

        /* 如果宽度或高度是负数，需要重新计算定位 */
        press.setPosition(width < 0 ? x + width : x, height < 0 ? y + height : y);
        press.onzoommove && press.onzoommove(event) === false || press.setSize(math.abs(width), math.abs(height));
    },

    /**
     * 缩放状态下的鼠标弹起事件处理
     * @private
     *
     * @param {Event} event 鼠标弹起事件
     */
    _zoomup: function (event) {
        var press = EC._oPress, doc = document;
        press.onzoomend && press.onzoomend(EC.Event(event));
        EC.restore();
        isIE && doc.body.releaseCapture(false);

        /* 如果是选择框需要关闭 */
        press == EC._oSelect ? press.hide() : window.onresize();
        doc.onmouseup(event);
    },

    /**
     * 将控件设置为拖动状态。调用它会触发控件对象的ondragstart事件，在整个drag的周期中，还将触发ondragmove与ondragend
     * 事件，在释放鼠标按键时拖动周期结束。
     * @public
     *
     * @param {ECControl} control 需要被拖动的对象
     * @param {Event} event 鼠标点击事件
     * @param {Object} range 控件允许的移动范围。top,right,bottom,left分别表示允许移动的最上,最右,最下,最左位置
     */
    drag: function (control, event, range) {
        if (event.type == 'mousedown') {
            var ec = EC, doc = document, el = control.getOuter(), offsetParent = el.offsetParent,
                tagName = offsetParent.tagName,
                data = tagName == 'BODY' || tagName == 'HTML' ? Brower() : {
                    top: 0,
                    right: offsetParent.offsetWidth - E.getBorderWidth(offsetParent),
                    bottom: offsetParent.offsetHeight - E.getBorderHeight(offsetParent),
                    left: 0
                };
            copy(data, range);
            copy(ec._setEnvironment(data), {
                type: ec.drag,
                press: ec._oPress,
                right: Math.max(data.right - control.getWidth(), data.left),
                bottom: Math.max(data.bottom - control.getHeight(), data.top),
                resize: window.onresize
            });
            ec._aStack.push(data);

            /* 设置样式为absolute，才能拖动 */
            E.set(el, 'position', 'absolute');

            doc.onmousemove = ec._dragmove;
            doc.onmouseup = ec._dragup;
            /* 拖动状态下不处理按压移入与移出的操作 */
            doc.onmouseover = doc.onmouseout = window.onresize = null;

            ec._oPress = control;
            isIE && doc.body.setCapture();
            control.ondragstart && control.ondragstart(event);
        }

        event.cancel();
    },

    /**
     * 拖动时document中鼠标移动事件处理函数
     * @private
     *
     * @param {Event} event 浏览器事件对象(IE下为undefined)
     */
    _dragmove: function (event) {
        event = EC.Event(event);
        press = EC._oPress;
        if (press) {
            /* 计算限制拖动的范围 */
            var press, math = Math, data = EC._aStack, data = data[data.length - 1],
                expectX = press.getX() + EC._nX - data.x, expectY = press.getY() + EC._nY - data.y,
                x = math.min(math.max(expectX, data.left), data.right),
                y = math.min(math.max(expectY, data.top), data.bottom);

            press.ondragmove && press.ondragmove(event, x, y) === false || press.setPosition(x, y);

            data.x = EC._nX + x - expectX;
            data.y = EC._nY + y - expectY;
        }
        return false;
    },

    /**
     * 拖动时鼠标按键弹起事件处理函数，恢复拖动前的状态
     * @private
     *
     * @param {Event} event 浏览器事件对象(IE下为undefined)
     */
    _dragup: function (event) {
        var press = EC._oPress, doc = document;
        delete press.$pressover;
        delete press.$pressout;
        press.ondragend && press.ondragend(EC.Event(event));
        EC.restore();
        isIE && doc.body.releaseCapture(false);
        doc.onmouseup(event);
    },

    /**
     * 强制拦截之后的一次鼠标点击，例如弹出菜单、多选下拉框显示时，屏幕上点击任何一个地方都要能隐藏，且可能需要执行额
     * 外的自定义处理。函数执行后，被指定的控件会临时处于拥有焦点状态(并不调用onfocus事件)，鼠标点击都将产生onforcibly
     * 事件，如果该事件不返回false值，将调用框架原来的onmousedown方法。
     * @public
     *
     * @param {ECControl} control 需要设置为拥有焦点状态的控件
     */
    forcibly: function (control) {
        EC._aStack.push(EC._setEnvironment({type: EC.forcibly, target: EC._oFocus}));
        document.onmousedown = EC._forciblydown;
        EC._oFocus = control;
    },

    /**
     * 强制拦截鼠标按下事件处理函数
     * @private
     *
     * @param {Event} event 浏览器事件对象(IE下为undefined)
     */
    _forciblydown: function (event) {
        event = EC.Event(event);
        for (var focus = EC._oFocus; focus; focus = focus.getParent()) {
        /* 依次向父节点上查找强制拦截事件 */
            if (focus.onforcibly) {
                focus.onforcibly(event) === false || EC.restore();
                break;
            }
        }
        return false;
    },

    /**
     * 特殊操作恢复。特殊操作包括EC.zoom、EC.drag、EC.forcibly与EC.custom调用。
     * @public
     */
    restore: function () {
        var ec = EC, doc = document, data = ec._aStack.pop(), type = data.type;
        copy(doc, data.mouse);
        if (type == ec.drag || type == ec.zoom) {
            window.onresize = data.resize;
            ec._oPress = data.press;
        }
        else {
            type == ec.forcibly && ec.focus(data.target);
        }
    },

    /**
     * 获取两个控件的父控件序列中不同的部分
     * @private
     *
     * @param {Array} list1 包含一个控件的序列1
     * @param {Array} list2 包含一个控件的序列2
     */
    _oddsParents: function (list1, list2) {
        var control1 = list1.pop(), control2 = list2.pop(), i = 0;

        if (control1 != control2) {
            /* 向序列中填充父控件 */
            while (control1) {
                list1.splice(0, 0, control1);
                control1 = control1.getParent();
            }
            while (control2) {
                list2.splice(0, 0, control2);
                control2 = control2.getParent();
            }

            /* 过滤父控件序列中重复的部分 */
            for (; list1[i] == list2[i]; i++);
            list1.splice(0, i);
            list2.splice(0, i);
        }
    },

    Document: {

        /**
         * 鼠标按键按下事件处理函数，重新改变当前拥有焦点的控件，如果当前仍然有处于按压状态的控件，直接忽略
         * @private
         *
         * @param {Event} event 浏览器事件对象(IE下为undefined)
         */
        onmousedown: function (event) {
            if (EC._oPress) {
                /* 还有上次未释放的mouseup操作，直接返回 */
                return false;
            }
            event = EC.Event(event);

            var target = event._oTarget;
            EC.focus(target);
            if (target) {
                EC._oPress = target;
                target.mousedown(event);
                target.pressstart(event);
            }
        },

        /**
         * 鼠标移动事件处理函数
         * @private
         *
         * @param {Event} event 浏览器事件对象(IE下为undefined)
         */
        onmouseover: function (event) {
            /* 鼠标移入的处理，首先需要计算是不是位于之前移出的控件之外，如果是需要触发之前的移出事件 */
            event = EC.Event(event);
            var target = EC._oOver = event._oTarget, press = EC._oPress, overList = [target], outList = [EC._oOutCache];
            EC._oddsParents(overList, outList);

            /* 对不重复的部分进行移出或移入操作 */
            for (var i = outList.length - 1, o; i >= 0; i--) {
                o = outList[i];
                o.mouseout(event);
                press == o && o.pressout(event);
            }
            for (i = 0; o = overList[i]; i++) {
                o.mouseover(event);
                press == o && o.pressover(event);
            }
            EC._oOutCache = null;
        },

        /**
         * 鼠标移动事件处理函数
         * @private
         *
         * @param {Event} event 浏览器事件对象(IE下为undefined)
         */
        onmousemove: function (event) {
            event = EC.Event(event);
            for (var target = event._oTarget; target; target = target.getParent()) {
                target.mousemove(event);
                EC._oPress == target && target.pressmove(event);
            }
        },

        /**
         * 鼠标移出事件处理函数，记录移出的控件，真正的移出事件将在onmouseover中触发
         * @private
         *
         * @param {Event} event 浏览器事件对象(IE下为undefined)
         */
        onmouseout: function (event) {
            event = EC.Event(event);
            var target = event.target, control = target._oControl;
            /* 如果是输入框的移出，强制触发 */
            EC._oOutCache = target.tagName == 'INPUT' && control ? control : event._oTarget;
        },

        /**
         * 鼠标按键弹起事件处理函数，同时解除控件按压事件
         * @private
         *
         * @param {Event} event 浏览器事件对象(IE下为undefined)
         */
        onmouseup: function (event) {
            event = EC.Event(event);

            var press = EC._oPress, target = event._oTarget;
            target && target.mouseup(event);

            if (press) {
                press.pressend(event);
                /* 点击事件只在鼠标按下与弹起发生在同一个控件上时触发 */
                press.contain(target) && press.click(event);
                EC._oPress = null;
            }
        },

        /**
         * 鼠标双击操作，处理IE下的问题
         * @private
         *
         * @param {Event} event 浏览器事件对象(IE下为undefined)
         */
        ondblclick: isIE ? function (event) {
            var doc = document, target = EC._eTarget;
            if (target) {
                /* 模拟控件Element正常的捕获，这个捕获可能是custom或者forcibly */
                EC._eTarget = null;
                /* 请注意绑定了ondblclick事件的Element，一定要同步绑定onmousedown */
                target.onmousedown(event);
            }
            doc.onmousedown(event);
            doc.onmouseup(event);
            return false;
        } : null
    },

    /**
     * 鼠标滚轮滚动事件处理函数
     * @private
     *
     * @param {Event} event 浏览器事件对象(IE下为undefined)
     */
    _mousewheel: function (event) {
        event = EC.Event(event);
        event.wheelDelta || (event.wheelDelta = event.detail * -40);

        var focus = EC._oFocus, over = EC._oOver;
        /* 拖动状态下，不允许滚动 */
        if (document.onmousemove == EC._dragmove
            || (focus && focus.mousewheel(event) === false || over && over.mousewheel(event) === false)) {
            event.cancel();
            return false;
        }
    },

    /**
     * 窗体改变大小，需要自动刷新控件大小
     * @private
     */
    _resize: function () {
        var ec = EC, i = 0, list = ec._aControl, o, mask = ec._eMask, brower;
        mask && mask.offsetWidth ? E.set(mask, 'display', 'none') : (mask = null);
        for (; o = list[i]; i++) {
            o.paint();
        }

        brower = Brower();
        mask && E.set(mask, {width: brower.width + 'px', height: brower.height + 'px', display: ''});
        /* 改变窗体大小需要清空拖动状态 */
        document.onmousemove == ec._dragmove && ec._dragup();
    },

    /**
     * 页面关闭时释放占用的空间，防止内存泄漏
     * @private
     */
    _unload: function () {
        for (var i = 0, list = EC._aControl, o; o = list[i]; i++) {
            o.dispose();
        }
    }
});

/*
 * 初始化键盘事件处理函数，相关的事件包括以下3个：
 * keydown  - 键盘按下事件
 * keypress - 键盘按压事件
 * keyup    - 键盘弹起事件
 */
(function () {
    for (var i = 0, list = ['keydown', 'keypress', 'keyup'], events = EC.Document, o; o = list[i]; i++) {
        events['on' + o] = new Function(
            'e',
            'e=EC.Event(e);for(var o=EC._oFocus;o;o=o.getParent())if(o.isEnabled()&&(o.on' + o + '&&o.on'
            + o + '(e)===false||o.$' + o + '(e)===false))return false'
        );
    }
})();

/*
ECControl - 基本控件，对一个区域进行事件操作的封装，定义基本的事件以及缺省的处理。基本控件会针对标签基本样式，在不同的
状态下自动附加样式。例如<div class="box">，在拥有焦点时会自动附加成<div class="box box-focus">。状态的目前有四种: 得到
焦点(focus)、鼠标移入(over)、按压时鼠标移入(press)与失效(disabled)。getX与getY返回的是相对坐标，如果需要返回绝对坐标，
请使用control.getOuter().getPosition()方法。

基本控件直接HTML初始化的例子，id指定名称，可以通过EC(id)的方式访问控件:
<div eui="type:control;id:test">
    <!-- 这里控件包含的内容 -->
    ...
</div>

属性
_bEnabled          - 控件的状态，为false时控件不处理任何事件
_nWidth            - 控件的宽度缓存
_nHeight           - 控件的高度缓存
_sID               - 控件的ID
_sBaseClass        - 控件定义时的基本样式
_sClass            - 控件当前使用的样式
_sType             - 控件的类型样式，通常是ec-控件类型
_sWidth            - 控件的基本宽度值，可能是百分比或者空字符串
_sHeight           - 控件的基本高度值，可能是百分比或者空字符串
_sDisplay          - 控件的布局方式，在hide时保存，在show时恢复
_eBase             - 控件的基本标签对象
_eBody             - 控件用于承载子控件的载体标签，通过setBodyElement函数设置这个值，绑定当前控件
_oParent           - 父控件对象

事件
ondispose          - 页面卸载时销毁控件事件，释放自定义的属性变量。
onappend           - 添加一个子控件时触发事件，缺省不会向更上级的父控件产生递归，返回false可以阻止子控件被添加
onremove           - 移除一个子控件时触发事件，缺省不会向更上级的父控件产生递归
onpaint            - 控件刷新事件，返回false可以阻止控件缺省的刷新行为。
onalterclassbefore - 控件样式改变前事件
onalterclassend    - 控件样式改变后事件
onfocus            - 控件获得焦点事件。
onblur             - 控件失去焦点事件。
onshow             - 控件显示事件。
onhide             - 控件隐藏事件。
onclick            - 控件被鼠标左键点击事件，参数是事件对象。要触发点击事件，鼠标必须在同一个控件区域按下并且弹起。
onmousedown        - 鼠标左键在控件区域按下事件，参数是事件对象。
onmouseover        - 鼠标光标移入控件区域事件，参数是事件对象。
onmousemove        - 鼠标光标在控件区域移动事件，参数是事件对象。
onmouseout         - 鼠标光标移出控件区域事件，参数是事件对象。
onmouseup          - 鼠标左键在控件区域弹起事件，参数是事件对象。
onmousewheel       - 滚轮事件，参数是事件对象
onpressstart       - 控件被鼠标左键按压开始事件，参数是事件对象。
onpressover        - 控件被鼠标左键按压状态中，鼠标光标移入控件区域事件，参数是事件对象。
onpressmove        - 控件被鼠标左键按压状态中，鼠标光标在控件区域移动事件，参数是事件对象。
onpressout         - 控件被鼠标左键按压状态中，鼠标光标移出控件区域事件，参数是事件对象。
onpressend         - 控件被鼠标左键按压结束事件，参数是事件对象。
onkeydown          - 键盘按下事件，参数是事件对象。
onkeypress         - 键盘按压事件，参数是事件对象。
onkeyup            - 键盘弹起事件，参数是事件对象。
onforcibly         - 强制拦截鼠标点击事件，需要先使用EC.forcibly方法获得焦点，参数是事件对象
ondragstart        - 拖动开始事件，在调用EC.drag过程中获得焦点，参数是事件对象。
ondragmove         - 拖动时的移动事件，需要先调用EC.drag方法。参数一是事件对象，参数二、三是X,Y轴的坐标
ondragend          - 拖动结束事件，在释放鼠标左键时调用，参数是事件对象。
*/

/**
 * 初始化基础控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params 参数，type--控件的类型(ec-开头，全小写)，capture--是否需要取消事件捕获，默认捕获
 */
function ECControl(baseClass, el, params) {
    var me = this, i = 0, o = el.style, undefined = void(0),
        names = ['onmousedown', 'onmouseover', 'onmousemove', 'onmouseout', 'onmouseup', 'onclick'];
    me._sID = params.id;
    me._sBaseClass = me._sClass = baseClass;
    me._sType = params.type;

    me._sWidth = o.width.endWith('px') ? undefined : o.width;
    me._sHeight = o.height.endWith('px') ? undefined : o.height;

    me._eBase = me._eBody = el;
    me._bEnabled = true;

    /* 绑定Element原有的事件函数 */
    for (; o = names[i]; i++) {
        me[o] = el[o];
    }
    el.onclick = null;

    params.capture !== false && me.capture();
    EC.bind(el, me);
}

copy(ECControl.prototype, {

    /**
     * 建立控件完成后的处理，参数与构造函数相同，通常使用它来为某一类控件添加装饰器等公共的初始化操作
     * @protected
     */
    $create: function () {
        this.paint();
    },

    /**
     * 页面卸载时销毁控件的缺省处理，释放循环引用，防止内存泄漏
     * @protected
     */
    $dispose: function () {
        this._eBase = this._eBody = null;
    },

    /**
     * 控件获得焦点事件的缺省处理，给控件添加新样式***-focus。
     * @protected
     */
    $focus: function () {
        this.alterClass('focus');
    },

    /**
     * 控件失去焦点事件的缺省处理，去除控件在focus事件中添加的样式***-focus。
     * @protected
     */
    $blur: function () {
        this.alterClass('focus', true);
    },

    /**
     * 鼠标光标移入控件区域事件的缺省处理，给控件添加新样式***-over。
     * @protected
     *
     * @param {Event} event 浏览器事件对象
     */
    $mouseover: function () {
        this.alterClass('over');
    },

    /**
     * 鼠标光标移出控件区域事件的缺省处理，去除控件在mouseover事件中添加的样式***-over。
     * @protected
     *
     * @param {Event} event 浏览器事件对象
     */
    $mouseout: function () {
        this.alterClass('over', true);
    },

    /**
     * 控件被鼠标左键按压开始事件的缺省处理，给控件添加新样式***-press。
     * @protected
     *
     * @param {Event} event 浏览器事件对象
     */
    $pressstart: function () {
        this.alterClass('press');
    },

    /**
     * 控件被鼠标左键按压开始事件的缺省处理，给控件添加新样式***-press。
     * @protected
     *
     * @param {Event} event 浏览器事件对象
     */
    $pressstart: function () {
        this.alterClass('press');
    },

    /**
     * 控件被鼠标左键按压结束事件的缺省处理，去除控件在pressstart事件中添加的样式***-press。
     * @protected
     *
     * @param {Event} event 浏览器事件对象
     */
    $pressend: function () {
        this.alterClass('press', true);
    },

    /**
     * 直接设置父对象的值，与setParent函数最大的不同，它仅仅只是设置控件对象逻辑上的父对象，用于某些特殊情况下的设定，
     * 如ECSelect对象中的选项框子控件。建议只有子类调用这个方法
     * @protected
     *
     * @param {ECControl} parent 父控件
     */
    $setParent: function (parent) {
        this._oParent = parent;
    },

    /**
     * 设置控件最内层的Element对象
     * @protected
     *
     * @param {Element} el 内层Element对象
     */
    $setBody: function (el) {
        this._eBody = el;
    },

    /**
     * 设置控件的大小，不进行与当前实际大小的比对与参数的检查，而是直接设置
     * @protected
     *
     * @param {Number} width 控件的宽度
     * @param {Number} height 控件的高度
     */
    $setSize: function (width, height) {
        var me = this, el = me._eBase;

        if (width) {
            E.set(el, 'width', width - EC.getWidthRevise(el) + 'px');
            me._nWidth = width;
        }

        if (height) {
            E.set(el, 'height', height - EC.getHeightRevise(el) + 'px');
            me._nHeight = height;
        }

        me.$paint();
    },

    /**
     * 为控件增加/删除一个扩展样式
     * @protected
     *
     * @param {String} className 样式(以ec-开头)或者扩展样式的尾缀
     * @param {Boolean} remove true--删除样式，否则新增样式
     */
    $alterClass: function (className, remove) {
        var me = this;
        me.onalterclassbefore && me.onalterclassbefore(className, remove);

        E[remove ? 'removeClass' : 'addClass'](
            me._eBase,
            className.startWith('ec-') ? className : me._sClass + '-' + className + ' ' + me._sType + '-' + className
        );

        me.onalterclassend && me.onalterclassend(className, remove);
    },

    /**
     * 页面卸载时销毁控件，解除JS-Object与Element之间的关联，请修改$dispose函数来实现不同的缺省处理方式
     * @public
     */
    dispose: function () {
        this.ondispose && this.ondispose();
        this.$dispose();
    },

    /**
     * 控件刷新
     * @public
     */
    paint: function () {
        var me = this, el = me.getOuter(), base = me._eBase, baseWidth = me._sWidth, baseHeight = me._sHeight,
            undefined = void(0);

        if (!me.onpaint || me.onpaint() !== false) {
            me.clearCache();
            E.set(base, {width: baseWidth, height: baseHeight});

            var width = me.getWidth(), height = me.getHeight();

            if (base != el && (baseWidth !== undefined || baseHeight !== undefined)) {
                /* 如果基本Element与外框Element不同，宽度需要重新计算 */
                var nextSibling = base.nextSibling, parentNode = base.parentNode;

                if (isIE) {
                    absWidth = E.get(base, 'width').endWith('px');
                    absHeight = E.get(base, 'height').endWith('px');
                }
                else {
                    var absWidth = base.offsetWidth, absHeight = base.offsetHeight;
                    E.set(parentNode, {width: absWidth + 100 + 'px', height: absHeight + 100 + 'px'});
                    absWidth = absWidth == base.offsetWidth;
                    absHeight = absHeight == base.offsetHeight;
                }

                E.appendTo(base, el.parentNode);

                baseWidth !== undefined && !absWidth && (width = base.offsetWidth);
                baseHeight !== undefined && !absHeight && (height = base.offsetHeight);

                nextSibling ? parentNode.insertBefore(base, nextSibling) : parentNode.appendChild(base);
            }

            me.$setSize(width, height);
        }
    },

    /**
     * 鼠标滚轮
     * @public
     *
     * @param {Event} event 事件对象
     * @return {Boolean} false--停止向上冒泡
     */
    mousewheel: function (event) {
        for (var o = this; o; o = o._oParent) {
            if (o.isEnabled()
                && (o.onmousewheel && o.onmousewheel(event) === false || o.$mousewheel(event) === false)) {
                return false;
            }
        }
    },

    /**
     * 控件改变(由子类进行管理)
     * @public
     */
    change: function () {
        this.onchange && this.onchange() === false || this.$change();
    },

    /**
     * 获取控件的基本样式
     * @public
     *
     * @return {String} 控件的基本样式
     */
    getBaseClass: function () {
        return this._sBaseClass;
    },

    /**
     * 获取控件的当前样式
     * @public
     *
     * @return {String} 控件的样式
     */
    getClass: function () {
        return this._sClass;
    },

    /**
     * 获取控件的类型样式(缺省样式)
     * @public
     *
     * @return {String} 控件的类型样式
     */
    getType: function () {
        return this._sType;
    },

    /**
     * 设置控件的样式
     * @public
     *
     * @param {String} name 控件的样式
     */
    setClass: function (baseClass) {
        var me = this, el = me._eBase, classes = el.className.split(/\s+/), i = 0, defaultClass = me._sType,
            oldBaseClass = me._sClass, length = oldBaseClass.length, oldPrefixClass = oldBaseClass + '-', s;

        if (baseClass != oldBaseClass) {
            /* 如果基本样式没有改变不需要执行 */
            for (; s = classes[i]; i++) {
                if (s.startWith(oldPrefixClass) || s == oldBaseClass) {
                    classes[i] = baseClass + s.substring(length);
                }
            }

            /* 必须拥有缺省样式 */
            classes.remove(defaultClass);
            classes.push(defaultClass);

            el.className = classes.join(' ').trim();
            me._sClass = baseClass;
        }
    },

    /**
     * 获取控件基本的Element对象
     * @public
     *
     * @return {Element} 基本Element对象
     */
    getBase: function () {
        return this._eBase;
    },

    /**
     * 获取控件最外层的Element对象
     * @public
     *
     * @return {Element} 外层Element对象
     */
    getOuter: function () {
        return this._eBase;
    },

    /**
     * 获取控件最内层的Element对象
     * @public
     *
     * @return {Element} 内层Element对象
     */
    getBody: function () {
        return this._eBody;
    },

    /**
     * 获取控件最内层Element对象的内部HTML
     * @public
     *
     * @return {String} 内层标签对象的内部HTML
     */
    getHTML: function () {
        return this._eBody.innerHTML;
    },

    /**
     * 设置控件最内层Element对象的内部HTML
     * @public
     *
     * @param {String} innerHTML 内层标签对象的内部HTML
     * @return {Element} 控件对象
     */
    setHTML: function (innerHTML) {
        this._eBody.innerHTML = innerHTML;
        return this;
    },

    /**
     * 获取当前控件的父控件
     * @public
     *
     * @return {ECControl} 父控件
     */
    getParent: function () {
        return this._oParent;
    },

    /**
     * 设置当前控件的父控件，会调用父控件的onappend与onremove方法，如果onappend返回false，等效于移除操作
     * @public
     *
     * @param {ECControl|String|Element} parent 父控件/父Element的ID/父Element对象
     */
    setParent: function (parent) {
        var me = this, oldParent = me._oParent, el = me.getOuter(), parentNode;

        /* 识别父对象类型 */
        if (parent) {
            if (parent instanceof ECControl) {
                parentNode = parent._eBody;
            }
            else {
                parentNode = parent;
                parent = EC.findParent(parent);
            }
        }

        /* 触发原来父控件的移除子控件事件 */
        if (parent != oldParent) {
            oldParent && oldParent.onremove && oldParent.onremove(me);
            if (parent && parent.onappend && parent.onappend(me) === false) {
                parent = parentNode = null;
            }
        }

        E.appendTo(el, parentNode);
        me._oParent = parent;
    },

    /**
     * 获取控件内外区域的宽度差
     * @public
     *
     * @return {Number} 控件内外区域的宽度差
     */
    getWidthRevise: function () {
        return E.getWidthRevise(this._eBase);
    },

    /**
     * 获取控件内外区域的高度差
     * @public
     *
     * @return {Number} 控件内外区域的高度差
     */
    getHeightRevise: function () {
        return E.getHeightRevise(this._eBase);
    },

    /**
     * 获取控件区域的宽度
     * @public
     *
     * @return {Number} 控件区域的宽度
     */
    getWidth: function () {
        var el = this._eBase;
        return this._nWidth = this._nWidth || el.offsetWidth || E.ncss(el, 'width') + EC.getWidthRevise(el);
    },

    /**
     * 获取控件区域的高度
     * @public
     *
     * @return {Number} 控件区域的高度
     */
    getHeight: function () {
        var el = this._eBase;
        return this._nHeight = this._nHeight || el.offsetHeight || E.ncss(el, 'height') + EC.getHeightRevise(el);
    },

    /**
     * 设置控件的大小
     * @public
     *
     * @param {Number} width 控件区域的宽度
     * @param {Number} height 控件区域的高度
     */
    setSize: function (width, height) {
        var me = this, undefined = void(0);

        (width && width != me.getWidth() || height && height != me.getHeight()) && me.$setSize(width, height);

        width && (me._sWidth = undefined);
        height && (me._sHeight = undefined);
    },

    /**
     * 获取控件内层可使用区域的宽度
     * @public
     *
     * @return {Number} 控件内层可使用区域的宽度
     */
    getBodyWidth: function () {
        return this.getWidth() - this.getWidthRevise();
    },

    /**
     * 获取控件内层可使用区域的高度
     * @public
     *
     * @return {Number} 控件内层可使用区域的宽度
     */
    getBodyHeight: function () {
        return this.getHeight() - this.getHeightRevise();
    },

    /**
     * 设置控件内层可使用区域的大小
     * @public
     *
     * @param {Number} width 控件内层可使用区域的宽度
     * @param {Number} height 控件内层可使用区域的高度
     */
    setBodySize: function (width, height) {
        this.setSize(width && width + this.getWidthRevise(), height && height + this.getHeightRevise());
    },

    /**
     * 获取鼠标光标相对于控件内部定位区域的坐标(需要减去border的值)
     * @public
     *
     * @return {Object} x--X轴相对坐标，y--Y轴相对坐标
     */
    getMouse: function () {
        var el = this._eBase, pos = E.getPosition(el);
        return {
            x: EC.getMouseX() - pos.left - E.ncss(el, 'borderLeftWidth'),
            y: EC.getMouseY() - pos.top - E.ncss(el, 'borderTopWidth')
        };
    },

    /**
     * 获取控件的相对X轴坐标，即外层标签的offsetLeft与offsetTop
     * @public
     *
     * @return {Number} 控件的相对X轴坐标
     */
    getX: function () {
        if (this.isShow()) {
            var el = this.getOuter(), value = el.style.left;
            return value.endWith('px') ? value.number() : el.offsetLeft - EC.getXRevise(el);
        }
    },

    /**
     * 获取控件的相对Y轴坐标，即外层标签的offsetLeft与offsetTop
     * @public
     *
     * @return {Number} 控件的相对Y轴坐标
     */
    getY: function () {
        if (this.isShow()) {
            var el = this.getOuter(), value = el.style.top;
            return value.endWith('px') ? value.number() : el.offsetTop - EC.getYRevise(el);
        }
    },

    /**
     * 设置控件的坐标
     * @public
     *
     * @param {Number} x 控件的X轴坐标
     * @param {Number} y 控件的Y轴坐标
     */
    setPosition: function (x, y) {
        E.set(this.getOuter(), {left: x + 'px', top: y + 'px'});
    },

    /**
     * 检查控件是否处于可操作状态，在不可操作状态下，控件将不进行任何事件的处理。检查过程中需要遍历所有的父控件，如果
     * 任何一个父控件的状态为不可操作，则控件就是不可操作的。
     * @public
     *
     * @return {Boolean} 控件是否可操作
     */
    isEnabled: function () {
        var parent = this._oParent;
        /* 当控件处于可操作状态时，查询父控件是否可用 */
        return this._bEnabled && (!parent || parent.isEnabled());
    },

    /**
     * 设置控件的可操作状态，如果设置为不可操作，给控件添加新样式***-disabled，同时如果控件处于拥有焦点状态将触发失去
     * 焦点事件；如果设置为可操作，去除控件的样式***-disabled
     * @public
     *
     * @param {Boolean} status 控件是否可操作，默认为true可操作
     */
    setEnabled: function (status) {
        var me = this;
        status = status !== false;

        /* 检查与控件当前状态是否一致 */
        if (me._bEnabled != status) {
            me.alterClass('disabled', status);
            /* 如果控件拥有焦点，设置成不可用状态时需要失去焦点 */
            status || EC.blur(me);
            me._bEnabled = status;
        }
    },

    /**
     * 检查控件是否处于显示状态
     * @public
     *
     * @return {Boolean} 控件是否处于显示状态
     */
    isShow: function () {
        var el = this.getOuter();
        return E.get(el, 'display') != 'none' && !!el.offsetWidth;
    },

    /**
     * 显示控件，如果控件包含onShow方法，将在控件状态由隐藏变为显示时调用，如果控件显示状态改变，返回true
     * @public
     *
     * @return {Boolean} 控件显示状态是否改变，即调用函数前是否隐藏
     */
    show: function () {
        var me = this, func = me.onshow;
        if (!me.isShow()) {
            E.set(me.getOuter(), 'display', me._sDisplay || '');
            me.onshow && me.onshow();
            return true;
        }
    },

    /**
     * 隐藏控件，如果控件包含onHide方法，将在控件状态由显示变为隐藏时调用，如果控件显示状态改变，返回true
     * @public
     *
     * @return {Boolean} 控件显示状态是否改变，即调用函数前是否显示
     */
    hide: function () {
        var me = this, el = me.getOuter();
        if (me.isShow()) {
            me._sDisplay = el.style.display;
            E.set(el, 'display', 'none');
            me.onhide && me.onhide();
            /* 如果控件拥有焦点，设置成隐藏状态时需要失去焦点 */
            EC.blur(me);
            return true;
        }
    },

    /**
     * 检测一个控件是否属于当前控件内部
     * @public
     *
     * @param {ECControl} control 待检测的控件
     * @return {Boolean} 指定的控件是否位于当前控件的内部
     */
    contain: function (control) {
        for (var o = control; o; o = o._oParent) {
            if (o == this) {
                return true;
            }
        }
    },

    /**
     * 清除控件大小的缓存
     * @public
     */
    clearCache: function () {
        this._nWidth = this._nHeight = void(0);
    },

    /**
     * 为控件增加/删除一个扩展样式，操作结束会清除原来与大小相关的缓存
     * @public
     *
     * @param {String} className 样式(以ec-开头)或者扩展样式的尾缀
     * @param {Boolean} remove true--删除样式，否则新增样式
     * @return {Element} 控件对象
     */
    alterClass: function (className, remove) {
        this.$alterClass(className, remove);
        this.clearCache();
        return this;
    },

    /**
     * 控件设置成捕获鼠标事件状态
     * @public
     *
     * @param {Boolean} status 是否捕获鼠标事件，默认为true进行捕获
     */
    capture: function (status) {
        var el = this._eBase;
        el.onmousedown = el.onmouseover = el.onmousemove = el.onmouseout = el.onmouseup
            = status !== false ? EC.capture : null;
        isIE && (el.ondblclick = el.onmousedown);
    },

    /**
     * 获取控件的ID，结果是EC-[控件生成时的序号]组成的字符串
     * @public
     *
     * @return {String} 控件所对应的字符串
     */
    getID: function () {
        return this._sID;
    }
});

/*
 * 初始化事件处理函数，以事件名命名，这些函数行为均是判断控件是否可操作/是否需要调用事件/是否需要执行缺省的事件处理，
 * 对应的缺省事件处理函数名以$开头后接事件名，处理函数以及缺省事件处理函数参数均为事件对象，仅执行一次。相关的事件包括
 * 以下13个：
 * focus      - 控件获得焦点事件
 * blur       - 控件失去焦点事件
 * click      - 控件鼠标左键点击事件(即鼠标左键在控件区域按下再弹起)
 * mousedown  - 鼠标左键在控件区域按下事件
 * mouseover  - 鼠标光标移入控件区域事件
 * mousemove  - 鼠标光标在控件区域移动事件
 * mouseout   - 鼠标光标移出控件区域事件
 * mouseup    - 鼠标左键在控件区域弹起事件
 * pressstart - 控件被鼠标左键按压开始事件
 * pressover  - 控件被鼠标左键按压状态中，鼠标光标移入控件区域事件
 * pressmove  - 控件被鼠标左键按压状态中，鼠标光标在控件区域移动事件
 * pressout   - 控件被鼠标左键按压状态中，鼠标光标移出控件区域事件
 * pressend   - 控件被鼠标左键按压结束事件(不一定触发鼠标左键在控件区域弹起时的处理，此时鼠标光标可能不在控件区域)
 * 此外，设置$pressover与$pressout函数
 */
(function () {
    for (var prototype = ECControl.prototype, i = 0, list = [
        'focus', 'blur', 'click',
        'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
        'pressstart', 'pressover', 'pressmove', 'pressout', 'pressend'
    ], o; o = list[i]; i++) {
        prototype[o] = new Function(
            'e',
            'var o=this;o.isEnabled()&&(o.on' + o + '&&o.on' + o + '(e)===false||o.$' + o + '(e))'
        );
    }
    /* 控件被鼠标左键按压状态中，鼠标光标移入控件区域事件的缺省处理，缺省与PressStart事件处理相同 */
    prototype.$pressover = prototype.$pressstart;
    /* 控件被鼠标左键按压状态中，鼠标光标移出控件区域事件的缺省处理，缺省与PressEnd事件处理相同 */
    prototype.$pressout = prototype.$pressend;

    /* 初始化空操作的一些缺省处理 */
    prototype.$paint = prototype.$change = prototype.$click = prototype.$mousewheel
        = prototype.$mousedown = prototype.$mousemove = prototype.$mouseup = prototype.$pressmove
        = prototype.$keydown = prototype.$keypress = prototype.$keyup = EC.blank;
})();

/*
ECDecorator - 装饰器基类，使用inline-block附着在控件外围，在控件改变状态时，装饰器同步改变状态。控件最外层装饰器的引用
              通过访问ECDecorator的属性来得到，属性名为控件对象

属性
_nWidth     - 装饰器宽度
_nHeight    - 装饰器高度
_sClass     - 装饰器样式
_eOuter     - 装饰器外框Element
_oInner     - 内层装饰器或者控件对象
*/

/**
 * 初始化装饰器，将其附着在控件外围
 * @public
 *
 * @param {ECControl} control 需要装饰的控件
 * @param {String} baseClass 装饰器的基本样式
 */
function ECDecorator(control, baseClass) {
    var me = this, constructor = ECDecorator, id = control.getID(),
        oldEl = (me._oInner = constructor[id] || control).getOuter(), position = E.get(oldEl, 'position'),
        el = me._eOuter = E.set(
            E.addClass(E(), me._sClass = baseClass || control.getBaseClass() + '-decorator'),
            'position:' + (position == 'absolute' ? position : 'relative')
                + ';top:' + E.get(oldEl, 'top') + ';left:' + E.get(oldEl, 'left')
        );

    E.set(oldEl, 'position', 'relative');
    oldEl.parentNode.insertBefore(el, oldEl);
    E.appendTo(oldEl, el);

    constructor[id] = me;

    /* 给控件的方法设置代理访问 */
    copy(control, me.Proxy);
}

copy(ECDecorator.prototype, {

    /**
     * 释放装饰器的循环引用
     * @protected
     */
    $dispose: function () {
        this._eOuter = null;
    },

    /**
     * 为装饰器增加/删除一个扩展样式，样式的名称为基础样式加上尾缀
     * @protected
     *
     * @param {String} className 扩展样式的尾缀，如果以ec-开头取不进行操作
     * @param {Boolean} remove true--删除样式，否则新增样式
     */
    $alterClass: function (className, remove) {
        var me = this;
        className.startWith('ec-')
            || E[remove ? 'removeClass' : 'addClass'](me._eOuter, me._sClass + '-' + className);
        me._oInner.$alterClass(className, remove, true);
        me.paint();
    },

    /**
     * 获取内层装饰器或控件
     * @public
     *
     * @return {ECDecorator|ECControl} 内层装饰器或控件
     */
    getInner: function () {
        return this._oInner;
    },

    /**
     * 获取装饰器的基本样式名称
     * @public
     *
     * @return {String} 装饰器的基本样式名称
     */
    getClass: function () {
        return this._sClass;
    },

    /**
     * 获取装饰器的外框Element
     * @public
     *
     * @return {Element} 外框Element
     */
    getOuter: function () {
        return this._eOuter;
    },

    /**
     * 获取装饰器内外区域的宽度差
     * @public
     *
     * @return {Number} 装饰器内外区域的宽度差
     */
    getWidthRevise: function () {
        return this._oInner.getWidthRevise(true) + E.getWidthRevise(this._eOuter);
    },

    /**
     * 获取装饰器内外区域的高度差
     * @public
     *
     * @return {Number} 装饰器内外区域的高度差
     */
    getHeightRevise: function () {
        return this._oInner.getHeightRevise(true) + E.getHeightRevise(this._eOuter);
    },

    /**
     * 获取装饰器区域的宽度
     * @public
     *
     * @return {Number} 装饰器区域的宽度
     */
    getWidth: function () {
        var me = this;
        return me._nWidth = me._nWidth || me._oInner.getWidth(true) + E.getWidthRevise(me._eOuter);
    },

    /**
     * 获取装饰器区域的高度
     * @public
     *
     * @return {Number} 装饰器区域的高度
     */
    getHeight: function () {
        var me = this;
        return me._nHeight = me._nHeight || me._oInner.getHeight(true) + E.getHeightRevise(me._eOuter);
    },

    /**
     * 设置装饰器区域的大小
     * @public
     *
     * @param {Number} width 装饰器区域的宽度
     * @param {Number} height 装饰器区域的高度
     */
    $setSize: function (width, height) {
        var me = this, el = me._eOuter;

        width && E.set(el, 'width', (width = (me._nWidth = width) - E.getWidthRevise(el)) + 'px');
        height && E.set(el, 'height', (height = (me._nHeight = height) - E.getHeightRevise(el)) + 'px');

        me._oInner.$setSize(width, height, true);
        me.paint();
    },

    /**
     * 装饰器刷新，在改变控件大小与样式的时候调用
     * @public
     */
    paint: EC.blank
});

(function () {

    var proxy = ECDecorator.prototype.Proxy = {

        /**
         * 释放对象时需要先释放装饰器
         * @protected
         */
        $dispose: function () {
            this.clear();
            this.$dispose();
        },

        /**
         * 获取控件最外层的Element对象，实际得到的是最外层装饰器的外框
         * @public
         *
         * @return {Element} 外层Element对象
         */
        getOuter: function () {
            return ECDecorator[this.getID()].getOuter();
        },

        /**
         * 清除所有的装饰器效果，同时清除所有的代理函数
         * @public
         */
        clear: function () {
            var me = this, constructor = ECDecorator;
            /* 清除所有的代理函数 */
            for (o in constructor.prototype.Proxy) {
                eval('delete me.' + o);
            }

            var id = me.getID(), o = constructor[id], el = o._eOuter;
            el.parentNode.insertBefore(me.getOuter(), el);
            E.appendTo(el);
            for (; o != me; o = o._oInner) {
                o.$dispose();
            }
            constructor[id] = null;
        }
    }, list = [
        ['$setSize', 2], ['$alterClass', 2], ['getWidthRevise', 0], ['getHeightRevise', 0],
        ['getWidth', 0], ['getHeight', 0]
    ], i = 0, o, name;

    /* 这里批量生成函数代理 */
    for (; o = list[i]; i++) {
        /* 如果是代理进入的，会多出来一个参数作为标志位 */
        name = o[0];
        proxy[name] = new Function(
            'var o=this,d=ECDecorator[o.getID()],r=arguments;return r[' + o[1] + ']?o.constructor.prototype.'
            + name + '.apply(o,r):d.' + name + '.apply(d,r)'
        );
    }
})();

/*
ECLRDecorator - 左右扩展装饰器，将区域分为"左-控件-右"三部分，使用paddingLeft与paddingRight作为左右区域的宽度

属性
_eLeft  - 装饰器左边区域Element对象
_eRight - 装饰器右边区域Element对象
*/

/**
 * 初始化左右扩展装饰器，将其附着在控件外围
 * @public
 *
 * @param {ECControl} control 需要装饰的控件
 * @param {String} baseClass 装饰器的基本样式
 */
function ECLRDecorator(control, baseClass) {
    ECDecorator.call(this, control, baseClass);

    var me = this, el = me.getOuter();
    baseClass = me.getClass();

    me._eLeft = E.set(E.addClass(E(el), baseClass + '-left'), 'position:absolute;left:0px');
    me._eRight = E.set(E.addClass(E(el), baseClass + '-right'), 'position:absolute');
    me.paint();
}

extend(ECLRDecorator, ECDecorator);

copy(ECLRDecorator.prototype, {

    /**
     * 释放装饰器的循环引用
     * @protected
     */
    $dispose: function () {
        this._eLeft = this._eRight = null;
        ECDecorator.prototype.$dispose.call(this);
    },

    /**
     * 装饰器刷新，在改变控件大小与样式的时候调用
     * @public
     */
    paint: function () {
        var me = this, style = E.get(me.getOuter()),
            paddingTop = style.paddingTop, paddingLeft = style.paddingLeft,
            inner = me.getInner(), height = inner.getHeight(true) + 'px';

        E.set(me._eLeft, {top: paddingTop, width: paddingLeft, height: height});
        E.set(me._eRight, {
            top: paddingTop,
            left: paddingLeft.number() + inner.getWidth(true) + 'px',
            width: style.paddingRight,
            height: height
        });
    }
});

/*
ECTBDecorator - 上下扩展装饰器，将区域分为"上-控件-下"三部分，使用paddingTop与paddingBottom作为上下区域的高度

属性
_eTop    - 装饰器上边区域Element对象
_eBottom - 装饰器下边区域Element对象
*/

/**
 * 初始化上下扩展装饰器，将其附着在控件外围
 * @public
 *
 * @param {ECControl} control 需要装饰的控件
 * @param {String} baseClass 装饰器的基本样式
 */
function ECTBDecorator(control, baseClass) {
    ECDecorator.call(this, control, baseClass);

    var me = this, el = me.getOuter();
    baseClass = me.getClass();

    me._eTop = E.set(E.addClass(E(el), baseClass + '-top'), 'position:absolute;top:0px');
    me._eBottom = E.set(E.addClass(E(el), baseClass + '-bottom'), 'position:absolute');
    me.paint();
}

extend(ECTBDecorator, ECDecorator);

copy(ECTBDecorator.prototype, {

    /**
     * 释放装饰器的循环引用
     * @protected
     */
    $dispose: function () {
        this._eTop = this._eBottom = null;
        ECDecorator.prototype.$dispose.call(this);
    },

    /**
     * 装饰器刷新，在改变控件大小与样式的时候调用
     * @public
     */
    paint: function () {
        var me = this, style = E.get(me.getOuter()),
            paddingLeft = style.paddingLeft, paddingTop = style.paddingTop,
            inner = me.getInner(), width = inner.getWidth(true) + 'px';

        E.set(me._eTop, {left: paddingLeft, width: width, height: paddingTop});
        E.set(me._eBottom, {
            top: paddingTop.number() + inner.getHeight(true) + 'px',
            left: paddingLeft,
            width: width,
            height: style.paddingBottom
        });
    }
});

/*
ECMagicDecorator - 九宫格扩展装饰器，将区域分为"左上-上-右上-左-控件-右-左下-下-右下"九部分，使用padding定义宽度与高度

属性
_aWidget - 装饰器九个区域的Element对象序列，使用序号0-8
*/

/**
 * 初始化九宫格扩展装饰器，将其附着在控件外围
 * @public
 *
 * @param {ECControl} control 需要装饰的控件
 * @param {String} baseClass 装饰器的基本样式
 */
function ECMagicDecorator(control, baseClass) {
    ECDecorator.call(this, control, baseClass);

    var me = this, el = me.getOuter(), i = 0, widget = me._aWidget = [];

    baseClass = me.getClass();

    for (; i < 9; i++) {
        if (i != 4) {
            widget[i] = E.set(E.addClass(E(el), baseClass + '-widget' + i), 'position:absolute');
        }
    }

    me.paint();
}

extend(ECMagicDecorator, ECDecorator);

copy(ECMagicDecorator.prototype, {

    /**
     * 释放装饰器的循环引用
     * @protected
     */
    $dispose: function () {
        this._aWidget = null;
        ECDecorator.prototype.$dispose.call(this);
    },

    /**
     * 刷新装饰器区域
     * @public
     */
    paint: function () {
        var me = this, style = E.get(me.getOuter()), inner = me.getInner(), i = 0, widget = me._aWidget,
            paddingLeft = style.paddingLeft, paddingTop = style.paddingTop,
            width = inner.getWidth(true), height = inner.getHeight(true),
            top = ['0px', paddingTop, (paddingTop.number() + height) + 'px'],
            left = ['0px', paddingLeft, (paddingLeft.number() + width) + 'px'];

        width = [paddingLeft, width + 'px', style.paddingRight];
        height = [paddingTop, height + 'px', style.paddingBottom];

        for (; i < 9; i++) {
            if (i != 4) {
                var row = Math.floor(i / 3), col = i % 3;
                E.set(widget[i], {top: top[row], left: left[col], width: width[col], height: height[row]});
            }
        }
    }
});

/*
ECScroll - 基本滚动条控件，继承自ECControl，内部包含三个ECControl，分别对应向前(滚动条的当前值变小)滚动按钮，向后(滚动
条的当前值变大)滚动按钮与滑动块。滚动条控件是滚动行为的虚拟实现，它不允许直接初始化，它的子类通常情况下也不会被直接初
始化，而是作为控件的一部分用于控制父控件的行为，当滚动条的状态改变时，触发父控件的onscroll事件与$scroll函数。向前滚动
按钮基本样式为滚动条控件基本样式附加prev后缀且额外拥有样式ec-scroll-prev，向后滚动按钮基本样式为滚动条控件基本样式附加
next后缀且额外拥有样式ec-scroll-next，滑动块基本样式为滚动条控件基本样式附加block后缀且另外拥有样式ec-scroll-block，在
基本样式之外，控件将被添加ec-scroll样式。例如滚动条控件基本样式为scroll，向前滚动按钮基本样式为scroll-prev，向后滚动按
钮基本样式为scroll-next，滑动块的基本样式为scroll-block。

属性
_nTotal          - 滚动条区域允许设置的最大值
_nStep           - 滚动条移动一次时的基本步长
_nValue          - 滚动条当前设置的值
_oTimer          - 定时器的句柄，用于连续滚动处理
_oPrev           - 向前滚动按钮
_oPrev._fAction  - 向前滚动函数
_oNext           - 向后滚动按钮
_oNext._fAction  - 和后滚动函数
_oBlock          - 滑动块
_oBlock.setRange - 设置滑动块的合法滑动区间
_oBlock.getRange - 获取滑动块的合法滑动区间
_oBlock._oRange  - 滑动块的合法滑动区间，属性top表示允许移动的最上位置，属性right表示允许移动的最右位置，属性bottom表
                   示允许移动的最下位置，属性left表示允许移动的最左位置
_fAction         - 当前正在执行的动作函数，用于连续滚动的控制

事件
onchange         - 数据重置事件，在执行setValue时如果控件当前值被改变时触发。
*/

/**
 * 初始化滚动条控件，滚动条控件定义了虚方法，因此不能直接初始化，需要子类中实现虚方法后才能初始化子类
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params 参数
 */
function ECScroll(baseClass, el, params) {
    var me = this, constructor = ECScroll, o = constructor.create, defaultClass = params.type;

    ECControl.call(me, baseClass, el, params);

    /* 创建向前与向后滚动按钮 */
    me._oPrev = o.call(me, baseClass, defaultClass, 'prev');
    me._oNext = o.call(me, baseClass, defaultClass, 'next');

    /* 创建滑动块 */
    o = me._oBlock = EC.create('Control', me, baseClass + '-block', null, {type: defaultClass + '-block'});
    copy(o, constructor.Block);
    o.ondragend = constructor.end;
    o.ondragmove = me.$blockmove;
    o.getBase().onmousedown = EC.custom;
    E.set(o.getOuter(), 'position', 'absolute');

    /* 初始化滚动条控件 */
    me._nValue = me._nTotal = 0;
    me._nStep = 1;
    el.onmousedown = EC.custom;
}

extend(ECScroll, ECControl);

/* 定义ECScroll的私有属性与函数 */
copy(ECScroll, {

    /**
     * 建立向前(向后)滚动按钮控件
     * @private
     *
     * @param {String} baseClass 基本的样式名称
     * @param {String} defaultClass 控件缺省的样式
     * @param {String} type 按钮的类型，prev或者next
     * @return {ECControl} 向前(向后)滚动按钮控件
     */
    create: function (baseClass, defaultClass, type) {
        var constructor = ECScroll,
            o = EC.create('Control', this, baseClass + '-' + type, null, {type: defaultClass + '-' + type});
        o._fAction = this[type];
        o.onpressover = o.onpressstart = constructor.buttonpress;
        o.onpressout = o.onpressend = constructor.end;
        o.getBase().onmousedown = EC.custom;
        E.set(o.getOuter(), {position: 'absolute', top: '0px', left: '0px'});
        return o;
    },

    /**
     * 设置父控件获得焦点
     * @private
     */
    focus: function () {
        var parent = this.getParent();
        parent && !parent.contain(EC.getFocused()) && EC.focus(parent);
    },

    /**
     * 滑动块/按钮触发的自动滚动停止，恢复状态
     * @private
     */
    end: function () {
        this.getParent().stop();
    },

    Block: {

        /**
         * 获取滑动块的合法滑动区间
         * @pravite
         */
        getRange: function (top, right, bottom, left) {
            return this._oRange;
        },

        /**
         * 设置滑动块的合法滑动区间
         * @pravite
         */
        setRange: function (top, right, bottom, left) {
            this._oRange = {top: top, right: right, bottom: bottom, left: left};
        },

        /**
         * 滑动块鼠标按下事件处理，触发滑动块拖动功能
         * @pravite
         *
         * @param {Event} event 事件对象
         */
        onmousedown: function (event) {
            ECScroll.focus.call(this.getParent());
            EC.drag(this, event, this._oRange);
        }
    },

    /**
     * 滚动按钮按压开始/移入事件处理，设置滚动的方向
     * @private
     */
    buttonpress: function () {
        var parent = this.getParent();
        ECScroll.focus.call(parent);
        this._fAction.call(parent, Math.max(parent._nStep, 5));
    }
});

copy(ECScroll.prototype, {

    /**
     * 内部区域按压时计算滑动块自动滚动的方向，并触发第一次移动
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $pressstart: function (event) {
        var me = this;
        (me._fAction = me.$allowPrev() ? me.prev : me.next).call(me, me.$getPageStep());
        ECScroll.focus.call(me);
        ECControl.prototype.$pressstart.call(me, event);
    },

    /**
     * 内部区域按压时移入，滑动块需要继续自动滚动
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $pressover: function (event) {
        this._fAction(this.$getPageStep());
        ECControl.prototype.$pressover.call(this, event);
    },

    /**
     * 内部区域按压时移出，需要停止滑动块的自动滚动
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $pressout: function (event) {
        this.stop();
        ECControl.prototype.$pressout.call(this, event);
    },

    /**
     * 内部区域按压结束时，需要停止滑动块的自动滚动并恢复控件的状态
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $pressend: function (event) {
        this.stop();
        ECControl.prototype.$pressend.call(this, event);
    },

    /**
     * 隐藏控件，滚动条的值需要归位
     * @public
     */
    hide: function () {
        this.setValue(0);
        return ECControl.prototype.hide.call(this);
    },

    /**
     * 滚动条当前值改变事件处理，将触发onchange事件，如果滚动条定义了父控件，将触发父控件的onscroll事件，如果返回值不
     * 是false，最后将调用父控件的$scroll完成处理。
     * @public
     */
    scroll: function () {
        var me = this, parent = me.getParent();
        me.change();
        parent && (parent.onscroll && parent.onscroll(me) === false || parent.$scroll(me));
    },

    /**
     * 直接设置控件的当前值，不执行任何的相关操作，只建议在子类中使用(子类滚动时设置滚动条的值)，请谨慎使用
     * @protected
     *
     * @param {Number} value 新的当前控件值
     */
    $setValue: function (value) {
        this._nValue = value;
    },

    /**
     * 获取向前滚动按钮
     * @public
     *
     * @return {ECControl} 向前滚动按钮
     */
    getPrev: function () {
        return this._oPrev;
    },

    /**
     * 获取向后滚动按钮
     * @public
     *
     * @return {ECControl} 向后滚动按钮
     */
    getNext: function () {
        return this._oNext;
    },

    /**
     * 获取滑动块
     * @public
     *
     * @return {ECControl} 滑动块
     */
    getBlock: function () {
        return this._oBlock;
    },

    /**
     * 获取滚动条控件的最大值
     * @public
     *
     * @return {Number} 滚动条控件的最大值
     */
    getTotal: function () {
        return this._nTotal;
    },

    /**
     * 设置滚动条控件的最大值
     * @public
     *
     * @param {Number} value 滚动条控件的最大值，不能为负数，如果最大值发生改变将导致滚动条刷新
     */
    setTotal: function (value) {
        var me = this;
        if (value >= 0 && me._nTotal != value) {
            me._nTotal = value;
            /* 检查滚动条控件的当前值是否已经越界 */
            if (me._nValue > value) {
                /* 值发生改变时触发相应的事件 */
                me._nValue = value;
                me.scroll();
            }
            me.$paint();
        }
    },

    /**
     * 获取滚动条控件的滚动步长，即滚动一次的最小像素点
     * @public
     *
     * @return {Number} 滚动条控件的滚动步长
     */
    getStep: function () {
        return this._nStep;
    },

    /**
     * 设置滚动条控件的滚动步长，必须大于0
     * @public
     *
     * @param {Number} value 滚动条控件的滚动步长，必须为正数
     */
    setStep: function (value) {
        value > 0 && (this._nStep = value);
    },

    /**
     * 获取滚动条控件的当前值
     * @public
     *
     * @return {Number} 滚动条控件的当前值
     */
    getValue: function () {
        return this._nValue;
    },

    /**
     * 设置滚动条控件的当前值，如果值发生改变将导致滚动条刷新
     * @public
     *
     * @param {Number} value 滚动条控件的当前值，不能为负数
     */
    setValue: function (value) {
        var me = this;
        value = Math.max(Math.min(value, me._nTotal), 0);
        if (me._nValue != value) {
            /* 值发生改变时触发相应的事件 */
            me._nValue = value;
            me.scroll();
            me.$paint();
        }
    },

    /**
     * 控件自动向前滚动，响应在200ms后触发，直至当前值为0或其它原因停止
     * @public
     *
     * @param {Number} step 每次向前滚动的步长
     */
    prev: function (step) {
        var me = this, value = me._nValue;
        me.stop();
        if (value) {
            EC.getPressed() == me && !me.$allowPrev() || me.setValue(value - step);
            me._oTimer = new Timer(me.prev, 200, me, step);
        }
    },

    /**
     * 控件自动向后滚动，响应在200ms后触发，直至当前值为最大值或其它原因停止
     * @public
     *
     * @param {Number} step 每次向前滚动的步长
     */
    next: function (step) {
        var me = this, value = me._nValue;
        me.stop();
        if (value < me._nTotal) {
            EC.getPressed() == me && !me.$allowNext() || me.setValue(value + step);
            me._oTimer = new Timer(me.next, 200, me, step);
        }
    },

    /**
     * 滚动条移动指定的步长次数，每次移动均是完整的步长单位
     * @public
     */
    move: function (n) {
        var me = this;
        me.isShow() && me.setValue(me._nValue + n * me._nStep);
    },

    /**
     * 停止prev或next函数的自动滚动
     * @public
     */
    stop: function () {
        var timer = this._oTimer;
        timer && timer.stop();
    }
});

/*
ECVScroll - 垂直滚动条控件，继承自ECScroll
*/

/**
 * 初始化垂直滚动条控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params 参数
 */
function ECVScroll(baseClass, el, params) {
    ECScroll.call(this, baseClass, el, params);
}

extend(ECVScroll, ECScroll);

copy(ECVScroll.prototype, {

    /**
     * 垂直滚动条控件刷新
     * @protected
     */
    $paint: function () {
        /* 计算滑动块高度与位置 */
        var me = this, total = me.getTotal(), block = me.getBlock(), height = me.getHeight(),
            prevHeight = me.getPrev().getHeight(), bodyHeight = me.getBodyHeight(),
            blockHeight = Math.max(Math.floor(bodyHeight * height / (height + total)), block.getHeightRevise() + 5);

        if (total) {
            block.setSize(0, blockHeight);
            block.setPosition(0, prevHeight + Math.floor((me.getValue() / total) * (bodyHeight - blockHeight)));
            block.setRange(prevHeight, 0, bodyHeight + prevHeight, 0);
        }
    },

    /**
     * 设置垂直滚动条控件的大小
     * @public
     *
     * @param {Number} width 控件区域的宽度
     * @param {Number} height 控件区域的高度
     */
    $setSize: function (width, height) {
        var me = this, style = E.get(me.getBody()), bodyWidth,
            next = me.getNext(), prevHeight = style.paddingTop.number();
        ECScroll.prototype.$setSize.call(me, width, height);
        bodyWidth = me.getBodyWidth();

        /* 设置滚动按钮与滑动块的信息 */
        me.getPrev().setSize(bodyWidth, prevHeight);
        next.setSize(bodyWidth, style.paddingBottom.number());
        me.getBlock().setSize(bodyWidth);
        next.setPosition(0, me.getBodyHeight() + prevHeight);

        me.$paint();
    },

    /**
     * 检测是否允许滑动块向前滚动
     * @protected
     *
     * @return {Boolean} 是/否允许向前滚动
     */
    $allowPrev: function () {
        return this.getMouse().y < this.getBlock().getY();
    },

    /**
     * 检测是否允许滑动块向后滚动
     * @protected
     *
     * @return {Boolean} 是/否允许向后滚动
     */
    $allowNext: function () {
        var block = this.getBlock();
        return this.getMouse().y > block.getY() + block.getHeight();
    },

    /**
     * 获取一页的步长，是单步长的整数倍
     * @protected
     *
     * @return {Number} 一页的步长
     */
    $getPageStep: function () {
        var height = this.getHeight();
        return height - height % this.getStep();
    },

    /**
     * 滑动块拖动事件处理
     * @public
     *
     * @param {Event} event 事件对象
     * @param {Number} x 实际移动到的X轴位置
     * @param {Number} y 实际移动到的Y轴位置
     */
    $blockmove: function (event, x, y) {
        var parent = this.getParent(), step = parent.getStep(), range = this.getRange(),
            value = (y - range.top)
                / (range.bottom - parent.getPrev().getHeight() - parent.getBlock().getHeight())
                * parent.getTotal();

        /* 应该滚动step的整倍数 */
        parent.$setValue(value - value % step);
        parent.scroll();
    }
});

/*
ECHScroll - 水平滚动条控件，继承自ECScroll
*/

/**
 * 初始化水平滚动条控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params 参数
 */
function ECHScroll(baseClass, el, params) {
    ECScroll.call(this, baseClass, el, params);
}

extend(ECHScroll, ECScroll);

copy(ECHScroll.prototype, {

    /**
     * 水平滚动条控件刷新
     * @protected
     */
    $paint: function () {
        /* 计算滑动块高度与位置 */
        var me = this, total = me.getTotal(), block = me.getBlock(), width = me.getWidth(),
            prevWidth = me.getPrev().getWidth(), bodyWidth = me.getBodyWidth(),
            blockWidth = Math.max(Math.floor(bodyWidth * width / (width + total)), block.getWidthRevise() + 5);

        if (total) {
            block.setSize(blockWidth);
            block.setPosition(prevWidth + Math.floor((me.getValue() / total) * (bodyWidth - blockWidth)), 0);
            block.setRange(0, bodyWidth + prevWidth, 0, prevWidth);
        }
    },

    /**
     * 设置水平滚动条控件的大小
     * @public
     *
     * @param {Number} width 控件区域的宽度
     * @param {Number} height 控件区域的高度
     */
    $setSize: function (width, height) {
        var me = this, style = E.get(me.getBody()), bodyHeight,
            next = me.getNext(), prevWidth = style.paddingLeft.number();
        ECScroll.prototype.$setSize.call(me, width, height);
        bodyHeight = me.getBodyHeight();

        /* 设置滚动按钮与滑动块的信息 */
        me.getPrev().setSize(prevWidth, bodyHeight);
        next.setSize(style.paddingRight.number(), bodyHeight);
        me.getBlock().setSize(0, bodyHeight);
        next.setPosition(me.getBodyWidth() + prevWidth, 0);

        me.$paint();
    },

    /**
     * 检测是否允许滑动块向前滚动
     * @protected
     *
     * @return {Boolean} 是/否允许向前滚动
     */
    $allowPrev: function () {
        return this.getMouse().x < this.getBlock().getX();
    },

    /**
     * 检测是否允许滑动块向后滚动
     * @protected
     *
     * @return {Boolean} 是/否允许向后滚动
     */
    $allowNext: function () {
        var block = this.getBlock();
        return this.getMouse().x > block.getX() + block.getWidth();
    },

    /**
     * 获取一页的步长，是单步长的整数倍
     * @protected
     *
     * @return {Number} 一页的步长
     */
    $getPageStep: function () {
        var width = this.getWidth();
        return width - width % this.getStep();
    },

    /**
     * 滑动块拖动事件处理
     * @public
     *
     * @param {Event} event 事件对象
     * @param {Number} x 实际移动到的X轴位置
     * @param {Number} y 实际移动到的Y轴位置
     */
    $blockmove: function (event, x, y) {
        var parent = this.getParent(), step = parent.getStep(), range = this.getRange(),
            value = (x - range.left)
                / (range.right - parent.getPrev().getWidth() - parent.getBlock().getWidth())
                * parent.getTotal()

        /* 应该滚动step的整倍数 */
        parent.$setValue(value - value % step);
        parent.scroll();
    }
});

/*
ECPanel - 层控件，继承自ECControl，内部包含ECVScroll、ECHScroll、ECControl三个控件，分别对应垂直滚动条、水平滚动条与夹
角控件(两个滚动条的连接部)。层控件中心区域可以超过控件实际大小，通过拖动滚动条显示完整的信息，在不使用滚动条时，层控件
与基本控件功能相同。垂直滚动条基本样式为层控件基本样式附加vscroll后缀；水平滚动条基本样式为层控件基本样式附加hscroll后
缀；夹角控件基本样式为层控件基本样式附加corner后缀。例如层控件基本样式为panel，垂直滚动条基本样式为panel-vscroll，水平
滚动条基本样式为panel-hscroll，夹角控件基本样式为panel-corner

层控件直接HTML初始化的例子:
<div eui="type:panel;vscroll:true;hscroll:true;wheel-delta:20;absolute:true">
    <!-- 这里放内容 -->
    ...
</div>

属性
_bAbsolute   - 是否包含绝对定位的Element
_nWheelDelta - 鼠标滚轮滚动一次的差值
_oVScroll    - 垂直滚动条控件
_oHScroll    - 水平滚动条控件
_oCorner     - 夹角控件

事件
onscroll     - 滚动条滚动事件，控件包含有滚动条控件并且被滚动时触发。
*/

/**
 * 初始化层控件，层控件支持自动展现滚动条控件，允许指定需要自动展现的垂直或水平滚动条
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params vscroll--是否自动展现垂直滚动条，hscroll--是否自动展现水平滚动条，缺省均为展现，absolute--是
 *                        否包含绝对定位的Element，默认不包含，wheelDelta--鼠标滚轮滚动一次的差值，相当于移动了多少个
 *                        最小步长单位，默认为总步长(差值*步长)为不大于20像素的最大值
 */
function ECPanel(baseClass, el, params) {
    var me = this, vscroll = params.vscroll !== false, hscroll = params.hscroll !== false;
    ECControl.call(me, baseClass, el, params);
    me._bAbsolute = !!params.absolute;
    me._nWheelDelta = params.wheelDelta;
    E.offset(el);

    me.$setBody(E.set(E.insert(el), (vscroll ? 'white-space:nowrap;' : '') + 'position:absolute;top:0px;left:0px'));
    E.set(E.addClass(E.insert(el), baseClass + '-layout'), 'overflow:hidden;position:relative');

    /* 生成中心区域的Element层容器，滚动是通过改变容器的left与top属性实现 */
    vscroll && E.set(
        (me._oVScroll = EC.create('VScroll', el, baseClass + '-vscroll')).getOuter(), 'position', 'absolute'
    );
    hscroll && E.set(
        (me._oHScroll = EC.create('HScroll', el, baseClass + '-hscroll')).getOuter(), 'position', 'absolute'
    );

    /* 只有垂直与水平滚动条控件都存在的情况下才生成夹角控件 */
    vscroll && hscroll && E.set(
        (me._oCorner
            = EC.create('Control', el, baseClass + '-corner', null, {type: params.type + '-corner'})).getOuter(),
        'position',
        'absolute'
    );
}

extend(ECPanel, ECControl);

copy(ECPanel.prototype, {

    /**
     * 层控件刷新，重新计算滚动条控件的信息，当层控件添加了子控件时需要显示的调用此方法
     * @protected
     */
    $paint: function () {
        var me = this, body = me.getBody(), base = me.getBase(),
            paddingWidth = E.getPaddingWidth(base), paddingHeight = E.getPaddingHeight(base),
            mainWidth = body.offsetWidth, mainHeight = body.offsetHeight,
            bodyWidth = me.getBodyWidth(), bodyHeight = me.getBodyHeight(),
            vscroll = me._oVScroll, hscroll = me._oHScroll, corner = me._oCorner,
            vsWidth = vscroll && vscroll.getWidth(), hsHeight = hscroll && hscroll.getHeight(),
            innerWidth = bodyWidth - vsWidth, innerHeight = bodyHeight - hsHeight,
            hsWidth = innerWidth + paddingWidth, vsHeight = innerHeight + paddingHeight;

        /* 考虑到内部Element绝对定位的问题，中心区域的宽度与高度修正 */
        if (me._bAbsolute) {
            for (
                var i = 0, list = E.getElements(body), pos = E.getPosition(body), x = pos.left, y = pos.top, el;
                el = list[i];
                i++
            ) {
                if (el.offsetWidth && E.get(el, 'position') != 'static') {
                    pos = E.getPosition(el);
                    mainWidth = Math.max(mainWidth, pos.left - x + el.offsetWidth);
                    mainHeight = Math.max(mainHeight, pos.top - y + el.offsetHeight);
                }
            }
        }

        /* 设置垂直与水平滚动条与夹角控件的位置 */
        vscroll && vscroll.setPosition(hsWidth, 0);
        hscroll && hscroll.setPosition(0, vsHeight);
        corner && corner.setPosition(hsWidth, vsHeight);

        if (mainWidth <= bodyWidth && mainHeight <= bodyHeight) {
            /* 宽度与高度都没有超过层控件的宽度与高度，不需要显示滚动条 */
            vscroll && vscroll.hide();
            hscroll && hscroll.hide();
            corner && corner.hide();
        }
        else {
            while (true) {
                if (corner) {
                    /* 宽度与高度都超出了显示滚动条后余下的宽度与高度，垂直与水平滚动条同时显示 */
                    if (mainWidth > innerWidth && mainHeight > innerHeight) {
                        hscroll.show();
                        hscroll.setTotal(mainWidth - innerWidth);
                        hscroll.setSize(hsWidth);
                        vscroll.show();
                        vscroll.setTotal(mainHeight - innerHeight);
                        vscroll.setSize(0, vsHeight);
                        corner.show();
                        corner.setSize(vsWidth, hsHeight);
                        bodyWidth = innerWidth;
                        bodyHeight = innerHeight;
                        break;
                    }
                    corner.hide();
                }
                if (hscroll && mainWidth > bodyWidth) {
                    /* 宽度超出控件的宽度，高度没有超出显示水平滚动条后余下的高度，只显示水平滚动条 */
                    hscroll.show();
                    hscroll.setTotal(mainWidth - bodyWidth);
                    hscroll.setSize(bodyWidth + paddingWidth);
                    vscroll && vscroll.hide();
                    bodyHeight = innerHeight;
                }
                else if (vscroll && mainHeight > bodyHeight) {
                    /* 高度超出控件的高度，宽度没有超出显示水平滚动条后余下的宽度，只显示水平滚动条 */
                    hscroll && hscroll.hide();
                    vscroll.show();
                    vscroll.setTotal(mainHeight - bodyHeight);
                    vscroll.setSize(0, bodyHeight + paddingHeight);
                    bodyWidth = innerWidth;
                }
                break;
            }
        }

        /* 设置内部定位器的大小 */
        el = body.parentNode;
        E.set(el, {
            width: bodyWidth - EC.getWidthRevise(el) + 'px',
            height: bodyHeight - EC.getHeightRevise(el) + 'px'
        });
    },

    /**
     * 滚轮事件默认处理
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $mousewheel: function (event) {
        var me = this, scroll = me._oVScroll;
        if (!scroll || !scroll.isShow()) {
            /* 如果垂直滚动条没有显示，使用水平滚动条 */
            scroll = me._oHScroll;
        }

        if (scroll && scroll.isShow()) {
            /* 计算滚动的次数，至少要滚动一次 */
            var value = scroll.getValue(), delta = me._nWheelDelta || Math.floor(20 / scroll.getStep()) || 1;
            scroll.move(event.wheelDelta < 0 ? delta : -delta);
            return value == scroll.getValue();
        }
    },

    /**
     * 键盘按下默认处理，支持上下左右键进行滚动
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $keydown: function (event) {
        var which = event.which, mod = which % 2;
        if (which >= 37 && which <= 40) {
            (mod ? this._oHScroll : this._oVScroll).move(which + mod - 39);
            return false;
        }
    },

    /**
     * Opera下仅用keydown不能屏蔽事件，还需要在press中屏蔽
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $keypress: function (event) {
        var which = EC.getKey();
        return which < 37 || which > 40;
    },

    /**
     * 控件内的滚动条被滚动时的缺省事件处理
     * @protected
     */
    $scroll: function () {
        var me = this, scroll;
        E.set(me.getBody(), {
            top: (scroll = me._oVScroll) ? -scroll.getValue() + 'px' : void(0),
            left: (scroll = me._oHScroll) ? -scroll.getValue() + 'px' : void(0)
        });
    },

    /**
     * 获取垂直滚动条
     * @public
     *
     * @return {ECControl} 垂直滚动条
     */
    getVScroll: function () {
        return this._oVScroll;
    },

    /**
     * 获取水平滚动条
     * @public
     *
     * @return {ECControl} 水平滚动条
     */
    getHScroll: function () {
        return this._oHScroll;
    },

    /**
     * 获取垂直滚动条的滚动值
     * @public
     *
     * @return {Number} 垂直滚动条的滚动值，如果没有垂直滚动条返回-1
     */
    getScrollTop: function () {
        var scroll = this._oVScroll;
        return scroll ? scroll.getValue() : -1;
    },

    /**
     * 获取水平滚动条的滚动值
     * @public
     *
     * @return {Number} 水平滚动条的滚动值
     */
    getScrollLeft: function () {
        var scroll = this._oHScroll;
        return scroll ? scroll.getValue() : -1;
    }
});

/*
ECItem - 选项控件，用于弹出菜单，下拉框，交换框里的单个选项等。选项控件必须用于ECItems包装过的控件中，选项控件支持移入
操作的缓存，不会因为鼠标移出而改变状态，因此可以通过函数调用来改变移入移出状态。

属性
_fType - 父控件的类型，在第一次setParent时设置，之后setParent时不能改变父控件的类型
*/

/**
 * 创建选项控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {String|Object} params 参数
 */
function ECItem(baseClass, el, params) {
    ECControl.call(this, baseClass, el, params);
    E.set(this.getBody(), 'overflow', 'hidden');
}

extend(ECItem, ECControl);

copy(ECItem.prototype, {

    /**
     * 控件移入事件处理函数，解除同一组选项中其它选项的移入状态
     * @protected
     *
     * @param {Event} 事件对象
     */
    $mouseover: function (event) {
        var me = this, items = me.getParent(), over = items.getOvered();
        /* 新控件移入，处于移入状态的控件将移出 */
        if (me != over) {
            over && ECControl.prototype.$mouseout.call(over);
            ECControl.prototype.$mouseover.call(me);
            items.$setOvered(me);
        }
    },

    /**
     * 控件移出事件处理函数，一般情况下不进行处理
     * @protected
     */
    $mouseout: EC.blank,

    /**
     * 设置当前控件的父控件，第一次设置后，将不能再设置与父控件不是同一类型的另一个父控件
     * @public
     *
     * @param {ECControl|String|Element} parent 父控件/父Element的ID/父Element对象
     */
    setParent: function (parent) {
        var me = this, constructor = me._fType, oldParent = me.getParent(), func = ECControl.prototype.setParent;

        if (parent && ECItems[parent.getID()]) {
            /* 父控件必须被ECItems包装过 */
            if (!constructor || parent instanceof constructor) {
                /* 父控件必须与之前选项控件的父控件是同一种类型 */
                func.call(me, parent);

                parent = me.getParent();
                constructor || parent && (me._fType = parent.constructor);

                /* 刷新父控件的值 */
                oldParent && oldParent.$paint();
                parent && parent.$paint();
                return;
            }

            parent = null;
        }
        func.call(me, parent);
    },

    /**
     * 选项控件只允许在刷新时改变大小，不允许直接改变
     * @public
     *
     * @param {Number} width 控件区域的宽度
     * @param {Number} height 控件区域的高度
     * @param {Boolean} status 是否改变大小，正常的调用是没有这个参数的
     */
    setSize: function (width, height, status) {
        status && ECControl.prototype.setSize.call(this, width, height);
    },

    /**
     * 为控件增加/删除一个扩展样式，如果大小发生改变需要重新设置大小，选项控件改变样式后大小不能变化
     * @public
     *
     * @param {String} className 样式(以ec-开头)或者扩展样式的尾缀
     * @param {Boolean} remove true--删除样式，否则新增样式
     * @return {Element} 控件对象
     */
    alterClass: function (className, remove) {
        var me = this, width = me.getWidth(), height = me.getHeight();
        me.$alterClass(className, remove);
        me.setSize(width, height);
        return me;
    }
});

/*
 * 选项组控件操作函数集合，控件增加$itemcreate函数，用于选项控件初始化
 */
ECItems = {

    /**
     * 选项组控件内部元素初始化
     * @protected
     */
    init: function (control) {
        control.$paint = EC.blank;

        var id = control.getID(), i = 0, elements = E.getChildren(control.getBody()), el;

        /* 防止对一个控件进行两次包装操作 */
        ECItems[id] = [];

        /* 初始化选项控件 */
        for (; el = elements[i]; i++) {
            control.append(el);
        }

        delete control.$paint;
    },

    Extend: {

        /**
         * 设置当前处于移入状态的控件
         * @protected
         *
         * @param {ECControl} item 处于移入状态的控件
         */
        $setOvered: function (item) {
            return this.getItems()._oOver = item;
        },

        /**
         * 获取所有的选项控件
         * @public
         *
         * @return {Array} 选项控件数组
         */
        getItems: function () {
            return ECItems[this.getID()];
        },

        /**
         * 获取当前处于移入状态的控件
         * @public
         *
         * @return {ECControl} 处于移入状态的控件
         */
        getOvered: function () {
            return this.getItems()._oOver;
        },

        /**
         * 设置控件内所有子控件的大小
         * @public
         *
         * @param {Number} itemWidth 子控件的宽度
         * @param {Number} itemHeight 子控件的高度
         */
        setItemSize: function (itemWidth, itemHeight) {
            for (var i = 0, items = this.getItems(), item; item = items[i]; i++) {
                item.setSize(itemWidth, itemHeight, true);
            }
        },

        /**
         * 新增选项控件
         * @public
         *
         * @param {String|Element|ECItem} o 控件内部的内容或者控件对应的标签元素或者是选项控件
         * @return {ECControl} 增加的选项控件
         */
        append: function (item) {
            var me = this, func = me.$itemcreate, baseClass = me.getClass() + '-item';

            if (item instanceof ECItem) {
                /* 选项控件，直接添加 */
                item.setParent(me);
            }
            else {
                /* 根据是字符串还是Element对象选择不同的初始化方式 */
                me.$paint = EC.blank;
                item = isString(item)
                    ? EC.create('Item', me, baseClass).setHTML(item)
                    : EC.create('Item', me, baseClass, item);
                delete me.$paint;
                me.$paint();
                func && func.apply(me, arguments);
            }

            return item;
        },

        /**
         * 移除一个控件
         * @public
         *
         * @param {Number|ECControl} item 控件的序号或者是控件对象
         */
        remove: function (item) {
            isNumber(item) && (item = this.getItems()[item]);
            item.setParent();
        },

        /**
         * 新增选项控件事件
         * @public
         *
         * @param {ECItem} item 选项控件
         */
        onappend: function (item) {
            /* 检查待新增的控件是否为选项控件 */
            if (!(item instanceof ECItem)) {
                return false;
            }
            this.getItems().push(item);
        },

        /**
         * 移除选项控件事件
         * @public
         *
         * @param {ECItem} item 选项控件
         */
        onremove: function (item) {
            this.getItems().remove(item);
        }
    }
};

/*
ECEdit - 输入框控件，接管了INPUT对象的onkeydown,onkeypress,onkeyup,onfocus,onblur事件。INPUT对象的样式为层控件基本样式
附加input后缀，例如输入框控件基本样式为text，它的input对象样式为text-input

输入框控件直接HTML初始化的例子:
<input eui="type:edit" name="test" value="test" />
或:
<div ec="type:edit;name:test;value:test">
    <!-- 如果ec中不指定name,value，也可以在input中指定 -->
    <input name="test" value="test" />
</div>

属性
_eInput  - INPUT对象

事件
onchange - 值改变事件
*/

/**
 * 初始化基本输入框控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params name--输入框的名称，value--输入框的默认值
 */
function ECEdit(baseClass, el, params) {
    /* 检查是否存在Input，如果没有生成一个Input */
    var me = this, constructor = ECEdit, input;

    if (isString(el.value)) {
        E.appendTo(input = el, el = E.insertAfter(E(), input));
        el.className = input.className;
    }
    else {
        input = E.getFirstChild(el, 'input') || E(el, 'input');
        /* 初始化输入框的值 */
        input.name = params.name || '';
        input.value = params.value || '';
    }
    me._eInput = input;

    ECControl.call(me, baseClass, el, params);

    /* 设置Element与Input的基本属性 */
    E.set(el, 'overflow', 'hidden');
    E.set(input, 'border', 'none').className = baseClass + '-input';
    EC.bind(input, me);

    /* 绑定Element原有的事件函数 */
    me.onkeydown = input.onkeydown;
    me.onkeypress = input.onkeypress;
    me.onkeyup = input.onkeyup;
    me.onfocus = input.onfocus;
    me.onblur = input.onblur;
    me.onchange = input.onchange;
    input.onchange = null;

    /* 设置自定义的事件处理 */
    copy(input, constructor.Input);
    isIE ? me.stopChange(false) : input.addEventListener('input', constructor.change, false);
}

extend(ECEdit, ECControl);

copy(ECEdit, {

    Input: {
        /**
         * 获得焦点事件处理函数
         * @private
         */
        onfocus: function () {
            var control = this.getControl();
            /* 设置默认获得焦点事件，阻止在focus事件中再次回调 */
            control.$focus = ECControl.prototype.$focus;
            /* 如果控件处于不可操作状态，不允许获得焦点 */
            control.isEnabled() ? EC.focus(control) : this.blur();
            delete control.$focus;
        },

        /**
         * 失去焦点事件处理函数
         * @private
         */
        onblur: function () {
            var control = this.getControl();
            /* 设置默认失去焦点事件，阻止在blur事件中再次回调 */
            control.$blur = ECControl.prototype.$blur;
            control.isEnabled() && EC.blur(control);
            delete control.$blur;
        }
    },

    /**
     * 输入框值改变事件处理函数
     * @private
     */
    change: isIE ? function () {
        if (event.propertyName == 'value') {
            var control = this.getControl();
            control.stopChange();
            control.change();
            control.stopChange(false);
        }
    } : function () {
        this.getControl().change();
    }
});

/*
 * 初始化键盘事件处理函数，包括：
 * keydown  - 键盘按下事件
 * keypress - 键盘敲击事件
 * keyup    - 键盘弹起事件
 */
(function () {
    for (var input = ECEdit.Input, i = 0, list = ['keydown', 'keypress', 'keyup'], o; o = list[i]; i++) {
        input['on' + o] = new Function(
            'e',
            'e=EC.Event(e);e.stop();var o=this.getControl();if(o.on'
            + o + '){o.stopChange();var r=o.on'
            + o + '(e)!==false&&o.$'
            + o + '(e);o.stopChange(false);return r}'
        );
    }
})();

copy(ECEdit.prototype, {

    /**
     * 页面卸载时销毁控件的缺省处理
     * @protected
     */
    $dispose: function () {
        this._eInput = null;
        ECControl.prototype.$dispose.call(this);
    },

    /**
     * 控件获得焦点的缺省处理，输入框也需要获得焦点
     * @protected
     */
    $focus: function () {
        var el = this._eInput;

        /* 使INPUT获得焦点，但不触发获得焦点事件，防止递归调用 */
        el.onfocus = null;
        try {
            el.focus();
        }
        catch (e) {
        }
        el.onfocus = ECEdit.Input.onfocus;

        ECControl.prototype.$focus.call(this);
    },

    /**
     * 控件失去焦点的缺省处理，输入框也需要失去焦点
     * @protected
     */
    $blur: function () {
        var el = this._eInput;

        /* 使INPUT失去焦点，但不触发失去焦点事件，防止递归调用 */
        el.onblur = null;
        try {
            el.blur();
        }
        catch (e) {
        }
        el.onblur = ECEdit.Input.onblur;

        ECControl.prototype.$blur.call(this);
    },

    /**
     * 设置控件的宽度与高度，同步设置输入框的大小
     * @protected
     *
     * @param {Number} width 控件的宽度
     * @param {Number} height 控件的高度
     */
    $setSize: function (width, height) {
        var me = this;
        ECControl.prototype.$setSize.call(me, width, height);
        E.set(me._eInput, {width: me.getBodyWidth() + 'px', height: me.getBodyHeight() + 'px'});
    },

    /**
     * 改变控件的可操作状态，需要将输入框也设置成只读状态
     * @public
     *
     * @param {Boolean} status 控件是否可操作
     */
    setEnabled: function (status) {
        ECControl.prototype.setEnabled.call(this, status);
        this._eInput.readOnly = !status;
    },

    /**
     * 获取INPUT对象
     * @public
     *
     * @return {Element} INPUT对象
     */
    getInput: function () {
        return this._eInput;
    },

    /**
     * 获取INPUT对象名称，名称决定向后台提交数据时的数据项名
     * @public
     *
     * @return {String} INPUT对象名称
     */
    getName: function () {
        return this._eInput.name;
    },

    /**
     * 设置INPUT对象名称，名称决定向后台提交数据时的数据项名
     * @public
     *
     * @param {String} name INPUT对象名称
     */
    setName: function (name) {
        this._eInput.name = name;
    },

    /**
     * 获取INPUT对象的值
     * @public
     *
     * @return {String} INPUT对象的值
     */
    getValue: function () {
        return this._eInput.value;
    },

    /**
     * 设置INPUT对象的值，名称决定向后台提交数据时的数据项名
     * @public
     *
     * @param {String} value INPUT对象的值
     */
    setValue: function (value) {
        var me = this;
        me.stopChange();
        me._eInput.value = value;
        me.change();
        me.stopChange(false);
    },

    /**
     * 设置光标位置
     * @public
     *
     * @param {Number} pos 位置索引
     */
    setCaret: isIE ? function (pos) {
        var range = this._eInput.createTextRange();
        range.moveStart('character', pos);
        range.collapse(true);
        range.select();
    } : function (pos) {
        this._eInput.setSelectionRange(pos, pos);
    },

    /**
     * 拆分当次输入的值，在onkeydown与onkeypress中调用
     * @public
     *
     * @return {Array} [0]表示光标或选中区域左部的字符串，[1]表示光标或选中区域右部的字符串，[2]表示选中区域的字符串
     */
    split: isIE ? function () {
        var el = this._eInput, range = document.selection.createRange(), oldText = range.text;
        range.setEndPoint('StartToStart', el.createTextRange());
        var i = range.text.length, value = el.value;
        return [value.substring(0, i - oldText.length), value.substring(i), oldText];
    } : function () {
        var el = this._eInput, value = el.value, start = el.selectionStart, end = el.selectionEnd;
        return [value.substring(0, start), value.substring(end), value.substring(start, end)];
    },

    /**
     * 设置对value值改变的监控状态，在IE浏览器下，任何情况下改变INPUT的值都会调用onchange，而在事件中不应该再次触发事
     * 件，避免出现递归调用
     * @public
     *
     * @param {Boolean} status 只有值为true才锁定改变，否则触除对onchange的调用锁定
     */
    stopChange: isIE ? function (status) {
        this._eInput.onpropertychange = status !== false ? null : ECEdit.change;
    } : EC.blank
});

/*
ECCheckbox - 复选框控件，继承自ECText。复选框被选中时将添加一个在基本样式后附加checked尾缀的样式，失去选中时清除

复选框控件直接HTML初始化的例子:
<input eui="type:checkbox;checked:true" type="checkbox" name="test" value="test" checked="checked" />
也可以使用其它标签初始化:
<div eui="type:checkbox;checked:true;name:test">
    <!-- 如果ec中不指定name，也可以在input中指定 -->
    <input name="test" />
</div>

属性
_nStatus   - 复选框当前的状态，0--未选，1--全选，2--半选
_oSuperior - 复选框的上级管理者
_aInferior - 所有的下级复选框
*/

/**
 * 初始化复选框控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params checked--控件是否默认选中，superior--管理复选框的id
 */
function ECCheckbox(baseClass, el, params) {
    var me = this, superior = params.superior;
    input = el.tagName == 'INPUT' ? el : {};

    ECEdit.call(me, baseClass, el, params);
    E.set(me.getInput(), 'display', 'none');

    me.setChecked(params.checked || input.checked);

    superior && EC.connect(me, me.setSuperior, superior);
}

extend(ECCheckbox, ECEdit);

copy(ECCheckbox, {

    /**
     * 改变复选框状态
     * @private
     *
     * @param {Boolean} status 新的状态，0--未选，1--全选，2--半选
     * @return {Boolean} 状态是否发生了改变
     */
    change: function (status) {
        var me = this, func = ECCheckbox.alterClass, superior = me._oSuperior, oldStatus = me._nStatus;

        if (status !== oldStatus) {
            /* 状态发生改变时进行处理 */
            func.call(me, oldStatus, true);
            func.call(me, status);

            me._nStatus = status;
            me.getInput().disabled = !me.isChecked();

            /* 如果有上级复选框，刷新上级复选框的状态 */
            superior && superior.$paint();

            me.change();
            return true;
        }
    },

    /**
     * 改变复选框控件样式
     * @private
     *
     * @param {Number} status 需要操作的状态，0--未选，1--全选，2--半选
     * @param {Boolean} remove 操作是否为删除，如果为否表示加上样式
     */
    alterClass: function (status, remove) {
        this.setClass(this.getBaseClass() + ['', '-checked', '-part'][status], remove);
    }
});

copy(ECCheckbox.prototype, {

    /**
     * 刷新上级复选框控件，在支持半选的复选框中才会被调用
     * @protected
     */
    $paint: function () {
        var inferiors = this._aInferior;

        if (inferiors) {
            var status = inferiors[0]._nStatus, i = 1, o;

            if (status != 2) {
                for (; o = inferiors[i]; i++) {
                    /* 计算新的状态，如果与初始状态不同则设置为半选状态 */
                    if (status != o._nStatus) {
                        status = 2;
                        break;
                    }
                }
            }

            ECCheckbox.change.call(this, status);
        }
    },

    /**
     * 控件点击时默认反选当前值
     * @protected
     */
    $click: function () {
        this.setChecked(!this.isChecked());
    },

    /**
     * 按键默认处理事件，要处理空格键的自动反选
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $keydown: function(event) {
        if (event.which == 32) {
            this.$click();
            return false;
        }
    },

    /**
     * Opera下仅用keydown不能屏蔽事件，还需要在press中屏蔽
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $keypress: function(event) {
        if (event.which == 32) {
            return false;
        }
    },

    /**
     * 获取复选框控件选中状态
     * @public
     *
     * @return {Boolean} 是否选中
     */
    isChecked: function () {
        return this._nStatus == 1;
    },

    /**
     * 设置复选框控件选中状态
     * @public
     *
     * @param {Boolean} status 是否选中
     */
    setChecked: function (status) {
        var inferiors = this._aInferior, i = 0, o;

        if (ECCheckbox.change.call(this, status ? 1 : 0) && inferiors) {
            /* 如果有下级复选框，全部改为与当前复选框相同的状态 */
            for (; o = inferiors[i]; i++) {
                o.setChecked(status);
            }
        }
    },

    /**
     * 获取上级复选框
     * @public
     *
     * @return {ECCheckbox} 上级复选框对象
     */
    getSuperior: function () {
        return this._oSuperior;
    },

    /**
     * 获取全部的下级复选框
     * @public
     *
     * @return {Array} 下级复选框对象
     */
    getInferior: function () {
        return this._aInferior;
    },

    /**
     * 设置上级复选框
     * @public
     *
     * @param {ECCheckbox} superior 上级复选框对象
     */
    setSuperior: function (superior) {
        var me = this, oldSuperior = me._oSuperior;

        /* 已经设置过上级复选框，需要先释放 */
        if (oldSuperior) {
            oldSuperior._aInferior.remove(me);
            oldSuperior.$paint();
        }

        if (superior) {
            /* 如果上级复选框没有初始化下级复选框，需要初始化 */
            (superior._aInferior || (superior._aInferior = [])).push(me);
            superior.$paint();
        }

        me._oSuperior = superior;
    }
});

/*
ECRadio - 单选框控件，继承自ECControl。单选框控件需要使用setName分组后使用，一组内只有一个单选框能被选中，单选框被选中
时将添加一个在基本样式后附加checked后缀的样式，失去选中时清除

单选框控件直接HTML初始化的例子:
<input eui="type:radio" type="radio" name="test" checked="checked" /></div>
也可以使用其它标签初始化:
<div eui="type:radio;name:test;checked:true"></div>

属性
_sName  - 控件所属的组名称
_sValue - 当前单选框的值
*/

/**
 * 初始化单选框控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params checked--控件是否默认选中，name--控件所属组的名称，value--控件的值
 */
function ECRadio(baseClass, el, params) {
    var me = this, input = {};

    if (el.tagName == 'INPUT') {
        el = E.insertAfter(E(), input = el);
        E.appendTo(input);
        el.className = input.className;
    }

    ECControl.call(me, baseClass, el, params);

    me.setName(params.name || input.name);
    me.setValue(params.value || input.value || '');
    (params.checked || input.checked) && me.checked();
}

extend(ECRadio, ECControl);

copy(ECRadio, {

    /**
     * 单选框组选中的值
     * @public
     *
     * @return {String} 单选框的值
     */
    getValue: function () {
        return this._eInput.value;
    },

    /**
     * IE下刷新时数据强制回填的处理
     * @private
     */
    change: function () {
        if (event.propertyName == 'value') {
            var i = 0, me = this, value = me.value, group = ECRadio['EC-' + me.name], item;

            /* IE下处理回填结束后不再继续处理 */
            me.onpropertychange = null;
            for (; item = group[i]; i++) {
                if (item._sValue == value) {
                    item.checked();
                    break;
                }
            }
        }
    }
});

copy(ECRadio.prototype, {

    /**
     * 点击时的默认处理，选中当前控件
     * @protected
     */
    $mousedown: function () {
        this.checked();
    },

    /**
     * 获取与当前单选框位于同一个分组的全部单选框
     * @public
     *
     * @return {Array} 单选框控件组
     */
    getItems: function () {
        return ECRadio[this._sName];
    },

    /**
     * 获取单选框的值
     * @public
     *
     * @return {String} 单选框的值
     */
    getValue: function () {
        return this._sValue;
    },

    /**
     * 设置单选框的值
     * @public
     *
     * @param {String} value 单选框的值
     */
    setValue: function (value) {
        this._sValue = value;
    },

    /**
     * 获取单选框对应的组的名称
     * @public
     *
     * @return {String} 单选框组的名称，如果返回null表示没有关联到组
     */
    getName: function () {
        return this._sName;
    },

    /**
     * 设置单选框对应的组的名称
     * @public
     *
     * @param {String} name 单选框组的名称，如果没有值，不进行修改
     */
    setName: function (name) {
        newName = 'EC-' + name;
        var me = this, constructor = ECRadio, oldName = me._sName, group = constructor[oldName], el;
        if (newName && newName != oldName) {
            /* 如果控件已经属于一个组，需要先释放 */
            if (group) {
                el = group._eInput;

                /* 改变控件组的选中状态 */
                if (group._oCheck == me) {
                    group._eInput.value = '';
                    group._oCheck = null;
                }
                group.remove(me);

                group = group[0];
                /* 如果单选框组内有数据，改变Input标签的位置，否则移除Input标签 */
                E.appendTo(el, group && group.getBody());
            }

            group = constructor[newName];
            if (!group) {
                /* 没有单选框组先创建一个组 */
                group = constructor[newName] = [];
                group.getValue = constructor.getValue;
                el = group._eInput = E.set(E(me.getBody(), 'input'), 'display', 'none');
                el.name = name;
                isIE && (el.onpropertychange = constructor.change);
            }
            group.push(me);

            me._sName = newName;
        }
    },

    /**
     * 判断当前单选框是否被选中
     * @public
     *
     * @return {Boolean} 当前单选框的选中状态
     */
    isChecked: function () {
        var group = ECRadio[this._sName];
        return group && group._oCheck == this;
    },

    /**
     * 设置当前单选框为选中状态
     * @public
     */
    checked: function () {
        var me = this, group = ECRadio[me._sName];
        if (group) {
            /* 先清除之前被选中的单选框样式 */
            var checked = group._oCheck, el = group._eInput;
            checked && checked.setClass(checked.getBaseClass());

            group._oCheck = me;
            if (el.onpropertychange) {
                el.onpropertychange = null;
                el.value = me._sValue;
                el.onpropertychange = ECRadio.change;
            }
            else {
                el.value = me._sValue;
            }
            me.setClass(me.getBaseClass() + '-checked');
        }
    }
});

/*
ECSelect - 下拉框控件，继承自ECControl，内部包含ECItem、ECControl、ECPanel三个控件，分别表示下拉框的文本、下拉框的按钮
与下拉框的选项框。下拉框的文本内容的基本样式为下拉框控件基本样式附加text后缀，下拉框的按钮的基本样式为下拉框控件基本样
式附加button后缀，下拉框的选项框的基本样式为下拉框控件基本样式附加options后缀，下拉框的选项的基本样式为下拉框控件基本
样式附加item后缀。例如下拉框控件基本样式为select，文本基本样式为select-text，下拉按钮基本样式为select-button，选项框的
基本样式为select-options，选项的基本样式为ec-item。

下拉框控件直接HTML初始化的例子:
<select eui="type:select;option-size:3" name="test">
    <!-- 这里放选项内容 -->
    <option value="值">文本</option>
    ...
    <option value="值" selected>文本</option>
    ...
</select>

如果需要自定义特殊的选项效果，请按下列方法初始化:
<div ec="type:select;name:test;option-size:3">
    <!-- 如果ec中不指定name，也可以在input中指定 -->
    <input name="test" />
    <!-- 这里放选项内容 -->
    <li value="值">文本</li>
    ...
</div>

属性
_nOptionSize - 下接选择框可以用于选择的条目数量
_oText       - 下拉框的文本框
_oButton     - 下拉框的按钮
_oOptions    - 下拉选择框
_oSelect     - 当前选中的选项

事件
onchange     - 选项改变事件
*/

/**
 * 初始化下拉框控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params optionSize--下拉框最大允许显示的选项数量，默认为5
 */
function ECSelect(baseClass, el, params) {
    if (el.tagName == 'SELECT') {
        var i = 0, options = el.options, selectEl = el;
        el = E.insertAfter(E(), el);

        for (; o = options[i]; i++) {
            E.setAttr(E.setText(E(el), o.text), 'value', o.value);
            /* 保存当前下拉框的选中项 */
            o.selected && (params.value = o.value);
        }

        el.className = selectEl.className;
        E.appendTo(selectEl);

        /* 保存当前下拉框的名称 */
        params.name = selectEl.name;
    }

    var me = this,
        o = me._oOptions = EC.create('Panel', document.body, baseClass + '-options', E.insert(el), {hscroll: false});
    ECEdit.call(me, baseClass, el, params);
    E.offset(me.getBody());

    /* 将input对象移回原来的位置 */
    E.appendTo(E.set(me.getInput(), 'display', 'none'), el);

    /* 初始化下拉区域，下拉区域需要强制置顶 */
    o.hide();
    o.$setParent(me);
    E.set(o.getOuter(), {zIndex: 65536, position: 'absolute'});
    el = o.getBody();

    /* 初始化下拉区域最多显示的选项数量 */
    me._nOptionSize = params.optionSize || 5;

    me.onappend = EC.blank;

    /* 初始化显示的文本框 */
    o = me._oText = EC.create('Item', me, baseClass + '-text', null, {capture: false});
    E.set(o.getOuter(), {position: 'absolute', top: '0px', left: '0px'});

    /* 初始化下拉按钮 */
    o = me._oButton = EC.create('Control', me, baseClass + '-button', null, {capture: false});
    E.set(o.getOuter(), 'position', 'absolute');

    me.$setBody(el);
    isIE && (me.$change = ECSelect.change);

    delete me.onappend;

    ECItems.init(me);

    /* 设置默认值 */
    me.setValue(me.getValue());
}

extend(ECSelect, ECEdit);

copy(ECSelect, {

    /**
     * 值发生改变默认处理事件
     * @private
     */
    change: function () {
        var me = this, select = me._oSelect, value = me.getValue();

        delete me.$change;
        if (select && select._sValue != value) {
            /* 出现选中项与实际值不相等的原因是IE下input自动回填造成的，需要恢复数据 */
            me.setValue(value);
        }
    },

    /**
     * 移动当前的焦点选项
     * @private
     *
     * @param {Number} n 移动的数量，负数向前，正数向后
     * @param {Boolean} roll 是否允许循环移动，按键时允许，鼠标滚轮时不允许
     * @return {ECControl} 新的焦点选项
     */
    move: function (n, roll) {
        var me = this, items = me.getItems(), length = items.length, over = roll ? me.getOvered() : me._oSelect;

        if (length) {
            n = roll
                ? ((over ? items.indexOf(over) : n > 0 ? length - 1 : 0) + n + length) % length
                : items.indexOf(over) + n;

            if (n >= 0 && n < length) {
                over = items[n];
                over.$mouseover();
            }
        }

        return over;
    },

    Item: {

        /**
         * 设定选项的值，如果没有数据，将选项的文本作为值
         * @public
         *
         * @param {String} value 选项的值
         */
        setValue: function (value) {
            this._sValue = value === null ? E.getText(this.getBody()) : value;
        },

        /**
         * 下拉框选项的鼠标滚轮事件处理
         * @public
         *
         * @param {Event} Event 事件对象
         * @return {Boolean} false--停止冒泡
         */
        mousewheel: function (event) {
            this.getParent()._oOptions.mousewheel(event);
            return false;
        }
    }
});

copy(ECSelect.prototype, ECItems.Extend);

copy(ECSelect.prototype, {

    /**
     * 刷新下拉选项框
     * @protected
     */
    $paint: function () {
        var me = this, options = me._oOptions, scroll = options.getVScroll(), step = scroll.getStep(),
            pos = E.getPosition(me.getOuter()), items = me.getItems(), itemLength = items.length,
            selected = me._oSelect, optionSize = me._nOptionSize, top = pos.top,
            optionTop = top + me.getHeight(), optionHeight;

        if (options.isShow()) {
            /* 为了设置激活状态样式, 因此必须控制下拉框中的选项必须在滚动条以内 */
            me.setItemSize(options.getBodyWidth() - (itemLength > optionSize ? scroll.getWidth() : 0));

            /* 设置options框的高度，如果没有元素，至少有一个单位的高度 */
            options.setBodySize(0, (Math.min(itemLength, optionSize) || 1) * step);
            options.$paint();
            optionHeight = options.getHeight();

            /* 如果浏览器下部高度不够，将显示在控件的上部 */
            options.setPosition(
                pos.left,
                optionTop + optionHeight <= Brower().bottom ? optionTop : top - optionHeight
            );

            /* 如果有选项被选中，需要移动焦点到选项的位置 */
            selected && selected.$mouseover();

            scroll.setValue(step * items.indexOf(selected));
        }
    },

    /**
     * 下拉框控件区域按压开始时弹出下拉选项框
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $pressstart: function (event) {
        ECEdit.prototype.$pressstart.call(this, event);
        this._oOptions.show();
        this.$paint();
    },

    /**
     * 下拉框控件区域按压结束时强制拦截下一次点击操作
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $pressend: function (event) {
        ECEdit.prototype.$pressend.call(this, event);
        /* 拦截之后的点击，同时屏蔽所有的控件点击事件 */
        EC.forcibly(this);
        EC.mask(0);
    },

    /**
     * 滚轮事件，下拉框控件激活时使用滚轮直接改变选中的值
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $mousewheel: function (event) {
        if (!this._oOptions.isShow()) {
            this.select(ECSelect.move.call(this, event.wheelDelta < 0 ? 1 : -1));
            return false;
        }
    },

    /**
     * 按键事件，需要处理上下键与回车键
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $keydown: function (event) {
        var me = this, which = event.which, items = me.getItems(), over = me.getOvered();

        if (EC.getPressed() != me) {
            /* 当前不能存在鼠标操作，否则屏蔽按键 */
            if (!me._oOptions.isShow()) {
                /* 显示下拉选项框 */
                if (which == 40 || which == 13) {
                    me.$pressstart();
                    me.$pressend();
                    return false;
                }
            }
            else if (which == 40 || which == 38) {
                var length = items.length, optionSize = me._nOptionSize,
                    scroll = me._oOptions.getVScroll(), step = scroll.getStep(), value = scroll.getValue() / step,
                    index = items.indexOf(ECSelect.move.call(me, which == 40 ? 1 : - 1, true));
                scroll.move(
                    (index < value ? index : index >= value + optionSize ? index - optionSize + 1: value) - value
                );
                return false;
            }
            else if (which == 13 || which == 27) {
                /* 回车键选中，ESC键取消 */
                me._oOptions.hide();
                which == 13 && over && me.select(over);
                EC.mask();
                EC.restore();
            }
        }
    },

    /**
     * Opera下仅用keydown不能屏蔽事件，还需要在press中屏蔽
     * @protected
     *
     * @param {Event} event 事件对象
     */
    $keypress: function (event) {
        var which = EC.getKey();
        return which < 37 || which > 40 || which == 13;
    },

    /**
     * 初始化单个选项
     * @protected
     *
     * @param {ECControl} item 选项对象
     * @param {String} value 选项对应的值
     */
    $itemcreate: function (item, value) {
        var value = value || E.getAttr(item.getBase(), 'value');
        item.setSize(0, this._oText.getHeight(), true);
        copy(item, ECSelect.Item);
        item.setValue(value);
    },

    /**
     * 设置控件的宽度与高度，自动初始化滚动条状态
     * @protected
     *
     * @param {Number} width 控件的宽度
     * @param {Number} height 控件的高度
     */
    $setSize: function (width, height) {
        var me = this, o = me._oOptions;

        o.$paint = EC.blank;
        ECEdit.prototype.$setSize.call(me, width, height);
        height = me.getBodyHeight();

        /* 设置选项框 */
        o.setSize(width);
        o.getVScroll().setStep(height);
        me.setItemSize(0, height);
        delete o.$paint;

        /* 设置文本区域 */
        width = me.getBodyWidth() - height;
        me._oText.setSize(width, height, true);

        /* 设置下拉按钮 */
        o = me._oButton;
        o.setSize(height, height);
        o.setPosition(width, 0);
        me.$paint();
    },

    /**
     * 设置控件的值，默认选中值相等的项，如果找不到，选中空白
     * @public
     *
     * @param {String} value 需要选中的值
     */
    setValue: function (value) {
        /* 防止改变事件的重入 */
        var me = this, i = 0, items = me.getItems(), o;

        me.change = EC.blank;
        for (; o = items[i]; i++) {
            if (o._sValue == value) {
                me.select(o);
                break;
            }
        }

        /* 找不到满足条件的项，将选中的值清除 */
        o || me.select();
        delete me.change;
    },

    /**
     * 设置选择框最多同时显示的选项数量
     * @public
     *
     * @return {Array} 选择框的显示选项数量
     */
    setOptionSize: function (value) {
        this._nOptionSize = value;
        this.$paint();
    },

    /**
     * 获取当前被选中的选项
     * @public
     *
     * @return {Array} 当前被选中的选项
     */
    getSelected: function () {
        return this._oSelect;
    },

    /**
     * 选中选项
     * @public
     *
     * @param {Number|ECControl} item 选项的序号或者选项自身
     */
    select: function (item) {
        var me = this, text = me._oText, func = ECEdit.prototype.setValue;

        /* 将选项序号转换成选项 */
        isNumber(item) && (item = me.getItems()[item]);

        if (me._oSelect != item) {
            if (item) {
                var el = item.getBody(), value = item._sValue;
                text.setHTML(el.innerHTML);
                func.call(me, value !== void(0) ? value : E.getText(el));
            }
            else {
                text.setHTML('');
                func.call(me, '');
            }

            me._oSelect = item;
            me.change();
        }
    },

    /**
     * 界面点击强制拦截，如果点击在下拉选项区域，则选中当前项，否则直接隐藏下拉选项框，但不会改变控件激活状态
     * @public
     *
     * @param {Event} event 事件对象
     * @return {Boolean} 总是返回false，即不继续冒泡
     */
    onforcibly: function (event) {
        var me = this, target = event.getTarget();
        me._oOptions.hide();
        /* 检查点击是否在当前下拉框的选项上 */
        target && target.getParent() == me && me.select(target);
        EC.mask();
    }
});

/*
ECGrid - 网格控件，继承自ECControl。网格控件由许多子控件对象组成，每个子控件的宽度与高度均相等，用于处理一批分行分列排
列的控件，行列是统一编号，row行col列，编号从0开始至(row*col-1)结束。网格控件统一管理子控件的事件，操作时的this表示网格
中的子控件对象，在事件中获取网格控件对象使用this.getParent()。子控件的基本样式为网格控件基本样式附加item后缀。例如网格
控件基本样式为grid，子控件基本样式为grid-item。

网格控件直接HTML初始化的例子:
<div eui="type:grid;col:7;row:6">
    <!-- ul与li分别表示行列 -->
    <ul>
        <li>...</li>
        ...
    </ul>
    ...
</div>

类属性
Item    - 网格子控件的所有方法

属性
_nRow   - 子控件的行数
_nCol   - 子控件的列数
_aItem  - 子控件集合

子控件属性
_nIndex - 单一网格在整个网格控件中的编号
*/

/**
 * 初始化网格控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params col-网格的总列数，row--网格的总行数
 */
function ECGrid(baseClass, el, params) {
    /* 初始化标签内的子标签 */
    var me = this, items = me._aItem = [], rows = E.getChildren(el), rowLength = rows.length,
        row = me._nRow = params.row || rowLength, maxCol = 0, i = 0, defaultClass = me.getType();
    ECControl.call(me, baseClass, el, params);

    /* 查找一行中最多的列数，将节点先移除 */
    for (; i < rowLength; i++) {
        for (var j = 0, o = rows[i] = E.getChildren(E.appendTo(rows[i])), col; col = o[j]; j++) {
            E.appendTo(col);
        }
        maxCol = Math.max(maxCol, o.length);
    }

    col = me._nCol = params.col || maxCol;

    /* 加载所有的子控件原始Element对象 */
    for (i = 0; i < row; i++) {
        o = rows[i];
        if (o) {
            for (j = 0; j < col; j++) {
                items.push(o[j]);
            }
        }
    }

    /* 初始化所有子控件 */
    for (i = 0, j = col * row; i < j; i++) {
        o = items[i] = EC.create(
            'Control',
            me,
            baseClass + '-item',
            E.set(items[i] || E(el), 'float', 'left'),
            {type: defaultClass + '-item'}
        );
        copy(o, ECGrid.Item);

        /* 设置子控件在整个网格中的序号 */
        o._nIndex = i;
    }
}

extend(ECGrid, ECControl);

copy(ECGrid.prototype, {

    /**
     * 设置控件的宽度与高度，实际的宽度与高度会受到所有子控件的宽度和的影响
     * @protected
     *
     * @param {Number} width 控件的宽度
     * @param {Number} height 控件的高度
     */
    $setSize: function (width, height) {
        var me = this, items = me._aItem, col = me._nCol, row = me._nRow, i = 0, o,
            widthRevise = me.getWidthRevise(), heightRevise = me.getHeightRevise();

        /* 计算每一个子控件的宽度与高度 */
        if (items.length) {
            width = Math.floor(((width ? width : me.getWidth()) - widthRevise) / col);
            height = Math.floor(((height ? height : me.getHeight()) - heightRevise) / row);

            for (; o = items[i]; i++) {
                o.setSize(width, height);
            }

            /* 设置实际的宽度与高度 */
            ECControl.prototype.$setSize.call(me, width * col + widthRevise, height * row + heightRevise);
        }
    },

    /**
     * 获取指定的子控件
     * @public
     *
     * @param {Number} row 子控件的行号(从0开始计数)，如果col参数没有值，这个表示子控件的序号
     * @param {Number} col 子控件的列号(从0开始计数)
     * @return {ECControl} 子控件对象
     */
    getItem: function (row, col) {
        return this._aItem[col === void(0) ? row : row * this._nCol + col];
    }
});

/**
 * 初始化事件处理函数，以事件名命名，这些函数行为均是判断控件是否可操作/是否需要调用事件/是否需要执行缺省的事件处理，
 * 对应的缺省事件处理函数名以$开头后接事件名，处理函数以及缺省事件处理函数参数均为事件对象，仅执行一次。这些函数都需要
 * 提供给内部的子控件使用，因此需要关联ECGrid控件，相关的事件包括以下13个：
 * focus      - 控件激活事件
 * blur       - 控件失去激活事件
 * click      - 控件鼠标左键点击事件(即鼠标左键在控件区域按下再弹起)
 * mousedown  - 鼠标左键在控件区域按下事件
 * mouseover  - 鼠标光标移入控件区域事件
 * mousemove  - 鼠标光标在控件区域移动事件
 * mouseout   - 鼠标光标移出控件区域事件
 * mouseup    - 鼠标左键在控件区域弹起事件
 * pressstart - 控件被鼠标左键按压开始事件
 * pressover  - 控件被鼠标左键按压状态中，鼠标光标移入控件区域事件
 * pressmove  - 控件被鼠标左键按压状态中，鼠标光标在控件区域移动事件
 * pressout   - 控件被鼠标左键按压状态中，鼠标光标移出控件区域事件
 * pressend   - 控件被鼠标左键按压结束事件(不一定触发鼠标左键在控件区域弹起时的处理，此时鼠标光标可能不在控件区域)
 * @private
 */
(function () {
    for (var i = 0, functions = ECGrid.Item = {}, list = [
        'focus', 'blur', 'click',
        'mousedown', 'mouseover', 'mousemove', 'mouseout', 'mouseup',
        'pressstart', 'pressover', 'pressmove', 'pressout', 'pressend'
    ], o; o = list[i]; i++) {
        functions[o] = new Function(
            'e',
            'var o=this,p=o.getParent();o.isEnabled()&&(p.on'
                + o + '&&p.on' + o + '.call(o,e)===false||p.$' + o + '.call(o,e))'
        );
    }

    /**
     * 获取当前子控件在网格控件中的序号
     * @public
     *
     * @return {Number} 子控件的序号
     */
    functions.getIndex = function () {
        return this._nIndex;
    };
})();

/*
ECCalendar - 日历控件，继承自ECControl。内部包含两个ECGrid控件，分别是星期名称网格与日期网格。日期网格，第一行包含上个
月的最后几天的信息，最后一行包含下个月最前几天的信息。星期名称网格的基本样式为日历控件基本样式附加name后缀；日期网格的
基本样式为日历控件基本样式附加date后缀；日期网格子控件的基本样式为日历控件基本样式附加item后缀；上个月下个月对应的日期
子控件默认不可点击，参见ECControl.prototype.setEnabled的样式说明；当天的日期子控件基本样式为日期网格子控件基本样式附加
today后缀。例如控件的基本样式为calendar，星期名称网格基本样式为calendar-name，日期网格基本样式为calendar-date，日期网
格子控件的基本样式为calendar-item，当天日期子控件的基本样式为calendar-item-today

日历控件直接HTML初始化的例子:
<div eui="type:calendar;year:2009;month:11">
    <!-- 内部显示区域，用于不需要上下滚动页的日历上，或者是需要自定义样式，其中ul标签组重复6次 -->
    <ul>
        <li>...</li>
        <li>...</li>
        <li>...</li>
        <li>...</li>
        <li>...</li>
        <li>...</li>
        <li>...</li>
    </ul>
    ...
</div>

属性
_bMode      - 日历工作模式，如果为true表示是设置模式(默认)，即每一次新的选中设置都会清空之前的选中，false是增加模式
_nYear      - 年份
_nMonth     - 月份(0-11)
_oNames     - 星期名称网格
_oDate      - 日期网格
_oSelect    - 选中的日期，对应的月份是属性名，属性值是一个数组表示月份选中的日期

子控件属性
_nDay       - 从本月1号开始计算的天数，如果是上个月，是负数，如果是下个月，会大于当月最大的天数

事件
onchange    - 日期月份改变事件
ondateclick - 日期点击事件，参数是日期对象
*/

/**
 * 初始化日历控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params year--日历控件的年份，month--日历控件的月份(1-12)
 */
function ECCalendar(baseClass, el, params) {
    /* 分别插入日期网格与星期名称网格需要使用的层，星期名称网格初始化 */
    var me = this, dateElement = E.insert(el), i = 0,
        items = me._oNames
            = EC.create('Grid', true, baseClass + '-weekname', el.insertBefore(E(), dateElement), {col: 7, row: 1});
    ECControl.call(me, baseClass, el, params);

    for (; i < 7; i++) {
        items.getItem(i).setHTML(EC.WEEKNAMES[i]);
    }

    /* 日期网格初始化 */
    items = me._oDate = EC.create('Grid', true, baseClass + '-date', dateElement, {col: 7, row: 6, item: baseClass});
    items.onclick = ECCalendar.dateclick;

    me._oSelect = {};
    me._bMode = params.mode != 'add';
    me.setDate(params.year, params.month);
}

extend(ECCalendar, ECControl);

/**
 * 日期网格控件点击处理，将事件转发到日历控件的ondateclick事件上
 * @private
 */
ECCalendar.dateclick = function () {
    var calendar = this.getParent().getParent(), day = this._nDay;
    calendar.ondateclick && calendar.ondateclick(new Date(calendar._nYear, calendar._nMonth, day)) === false
        || calendar.select(day);
};

copy(ECCalendar.prototype, {

    /**
     * 设置控件的宽度与高度，实际的宽度与高度会受到所有子控件的宽度和的影响
     * @protected
     *
     * @param {Number} width 控件的宽度
     * @param {Number} height 控件的高度
     */
    $setSize: function (width, height) {
        var me = this, names = me._oNames, date = me._oDate;

        names.$setSize(width);
        date.$setSize(width);

        /* 设置实际的宽度与高度 */
        ECControl.prototype.$setSize.call(
            me,
            date.getWidth() + me.getWidthRevise(),
            names.getHeight() + date.getHeight() + me.getHeightRevise()
        );
    },

    /**
     * 获取日历控件的年份
     * @public
     *
     * @return {Number} 日历控件的年份
     */
    getYear: function () {
        return this._nYear;
    },

    /**
     * 获取日历控件的月份
     * @public
     *
     * @return {Number} 日历控件的月份(1-12)
     */
    getMonth: function () {
        return this._nMonth + 1;
    },

    /**
     * 设置日历控件时间
     * @public
     *
     * @param {Number} year 日历控件的年份
     * @param {Number} month 日历控件的月份(1-12)
     */
    setDate: function (year, month) {
        var me = this, today = new Date(), currYear = today.getFullYear(), currMonth = today.getMonth(),
            year = year || currYear, month = month ? month - 1 : currMonth,
            oldYear = me._nYear, oldMonth = me._nMonth;

        if (oldYear != year || oldMonth != month) {
            me._nYear = year;
            me._nMonth = month;

            /* 得到上个月的最后几天的信息，用于补齐当前月日历的上月信息位置 */
            var time = new Date(year, month, 0), day = -time.getDay(), lastDayOfLastMonth = time.getDate(),
                /* 得到当前月的天数 */
                time = new Date(year, month + 1, 0), lastDayOfCurrMonth = time.getDate(), date = me._oDate, i = 0,
                selected = me.getSelected();

            for (; o = date.getItem(i); i++, day++) {
                var flag = day > 0 && day <= lastDayOfCurrMonth, o;
                o.setEnabled(flag);
                o.setHTML(flag ? day : day <= 0 ? lastDayOfLastMonth + day : day - lastDayOfCurrMonth);
                o.alterClass('today', true);
                o.alterClass('selected', selected.indexOf(day) < 0);
                o._nDay = day;
            }

            /* 判断是否为当天 */
            year == currYear && month == currMonth
                && date.getItem(day - lastDayOfCurrMonth + today.getDate() - 1).alterClass('today');

            me.change();
        }
    },

    /**
     * 设置日历选中模式
     * @public
     *
     * @param {Boolean} status 日历选中模式，如果不为false表示单次选择模式，否则是累加模式
     */
    setMode: function (status) {
        this._bMode = status !== false;
    },

    /**
     * 获取当前月的选中信息
     * @public
     *
     * @return {Array} 如果是当前月的信息，是一个一维数组
     */
    getSelected: function () {
        var me = this, name = me._nYear + ',' + me._nMonth, selected = me._oSelect, value = selected[name];
        return value ? value : (selected[name] = []);
    },

    /**
     * 获取全部的选中信息
     * @public
     *
     * @return {Object} 使用属性名表示"年,月"，属性值是日期选中信息数组
     */
    getAllSelected: function () {
        return this._oSelect;
    },

    /**
     * 选中日历的若干天
     * @public
     *
     * @param {Number} start 选中的开始时间
     * @param {Number} end 选中的结束时间，如果省略，表示只选中一天
     */
    select: function (start, end) {
        var me = this, selected = me.getSelected(), i = 0, date = me._oDate, item;

        end = end || start;

        for (; i < 42; i++) {
            var o = date.getItem(i), day = o._nDay;

            if (day >= start && day <= end) {
                o.alterClass('selected');
            }
            else if (me._bMode && selected.indexOf(o._nDay) >= 0) {
                o.alterClass('selected', true);
            }
        }

        if (me._bMode) {
            selected = [];
            o = me._oSelect = {};
            o[me._nYear + ',' + me._nMonth] = selected;
        }

        for (i = start; i <= end; i++) {
            selected.indexOf(i) < 0 && selected.push(i);
        }
    },

    /**
     * 日历移动指定的月份数，生成新日历
     * @public
     *
     * @param {Number} offsetMonth 日历移动的月份数
     */
    move: function (offsetMonth) {
        var me = this, time = new Date(me._nYear, me._nMonth + offsetMonth, 1);
        me.setDate(time.getFullYear(), time.getMonth() + 1);
    }
});

/*
ECForm - 窗体控件，继承自ECControl，内部包含两个ECControl控件与一个ECPanel控件，分别对应标题栏，关闭按钮与内容区域，窗
体控件模拟了浏览器窗体的效果，在其中包含<iframe>标签的话可以在当前页面打开一个新的页面，避免了使用window.open在不同浏
览器下的兼容性不稳定的问题。标题栏基本样式为窗体控件基本样式附加title后缀；关闭按钮基本样式为层控件基本样式附加close后
缀；内容区域基本样式为层控件基本样式附加main后缀。例如窗体控件基本样式为form，标题栏的基本样式为form-title，关闭按钮的
基本样式为form-close，内容区域的基本样式为form-main

窗体控件直接HTML初始化的例子:
<div eui="type:form;hide:true">
    <!-- 标题可以没有 -->
    <label>窗体的标题</label>
    <!-- 这里放窗体的内容 -->
    ...
</div>

类属性
_aForm      - 窗体列表
_nBaseIndex - 窗体的基准层次

属性
_oTitle     - 标题栏
_oClose     - 关闭按钮
_oMain      - 中心区域
*/

/**
 * 初始化窗体控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params hide--窗体是否需要默认隐藏
 */
function ECForm(baseClass, el, params) {
    /* 生成标题控件与内容区域控件对应的Element对象 */
    var me = this, constructor = ECForm, main = E.insert(el), o = el.insertBefore(E(), main),
        label = E.getFirstChild(main, 'label'), forms = constructor._aForm;
    ECControl.call(me, baseClass, el, params);

    /* 初始化标题区域 */
    label && (o.innerHTML = E.appendTo(label).innerHTML);
    o = me._oTitle = EC.create('Control', true, baseClass + '-title', o);
    o.onmousedown = constructor.titledown;
    o.getBase().onmousedown = EC.custom;

    /* 初始化关闭按钮 */
    o = me._oClose = EC.create('Control', el, baseClass + '-close');
    o.onclick = constructor.closeclick;

    /* 初始化中心区域 */
    o = me._oMain = EC.create('Panel', true, baseClass + '-main', main);
    me.$setBody(o.getBody());

    /* 计算当前窗体显示的层级 */
    E.set(me.getOuter(), 'zIndex', forms.length + constructor._nBaseIndex);
    forms.push(me);

    params.hide && me.hide();
    E.offset(me.getBody());
}

extend(ECForm, ECControl);

copy(ECForm, {

    /* 当前全部初始化的窗体 */
    _aForm: [],

    /* 窗体zIndex样式的基准值 */
    _nBaseIndex: 256,

    /**
     * 鼠标按压事件处理，需要触发拖动，如果当前窗体未得到焦点则得到焦点
     * @private
     *
     * @param {Event} event 事件对象
     */
    titledown: function (event) {
        var form = this.getParent();

        form.contain(EC.getFocused()) || EC.focus(form);
        EC.drag(form, event);
    },

    /**
     * 窗体关闭按钮点击事件，关闭窗体
     * @private
     */
    closeclick: function () {
        this.getParent().hide();
    }
});

copy(ECForm.prototype, {

    /**
     * 激活当前窗体，并将窗体置于所有窗体的顶部
     * @protected
     */
    $focus: function () {
        var constructor = ECForm, forms = constructor._aForm, i = forms.indexOf(this), o;
        ECControl.prototype.$focus.call(this);

        /* 将当前窗体置顶 */
        forms.push(forms.splice(i, 1)[0]);
        for (; o = forms[i]; i++) {
            E.set(o.getOuter(), 'zIndex', i + constructor._nBaseIndex);
        }
    },

    /**
     * 设置控件的宽度与高度
     * @protected
     *
     * @param {Number} width 控件的宽度
     * @param {Number} height 控件的高度
     */
    $setSize: function (width, height) {
        var me = this, title = me._oTitle, main = me._oMain;

        ECControl.prototype.$setSize.call(me, width, height);
        width = me.getBodyWidth();

        title.setSize(width, E.ncss(me.getBase(), 'paddingTop'));

        main.setSize(width, me.getBodyHeight());
    },

    /**
     * 显示窗体时会将窗体置于所有窗体的顶部
     * @public
     */
    show: function () {
        ECControl.prototype.show.call(this);
        this.contain(EC.getFocused()) || EC.focus(this);
    },

    /**
     * 隐藏窗体时重新计算窗体在Z轴上的位置
     * @public
     */
    hide: function () {
        ECControl.prototype.hide.call(this);
        EC.mask();
    },

    /**
     * 窗体以独占方式打开，此时焦点不能切换
     * @public
     */
    showModal: function () {
        this.show();
        E.set(this.getOuter(), 'zIndex', 65536);
        EC.mask(0.05);
    },

    /**
     * 设置窗体标题
     * @public
     *
     * @param {String} text 窗体标题
     */
    setTitle: function (text) {
        this._oTitle.setHTML(text);
    }
});

/*
ECTree - 树控件，继承自ECControl。子控件区域的样式为基本样式附加items后缀；如果控件不包含子控件，为控件添加一个在基本
样式后附加empty后缀的样式；如果控件是上级树控件的第一个子树控件，为控件添加一个在基本样式后附加first后缀的样式；如果控
件是上级树控件最后一个子树控件，为控件添加一个在基本样式后附加last后缀的样式；如果控件的子控件被隐藏，为控件添加一个在
基本样式后附加fold后缀的样式

树控件直接HTML初始化的例子:
<div eui="type:tree;fold:true">
    <!-- 当前节点的文本，如果没有整个内容就是节点的文本 -->
    <label>节点的文本</label>
    <!-- 这里放子控件，如果需要fold某个子控件，将子控件的style="display:none"即可 -->
    <li>子控件文本</li>
    ...
</div>

属性
_sItemsDisplay - 隐藏时_eItems的状态，在显示时恢复
_eItems        - 子控件区域Element对象
_aItem         - 子控件集合
*/

/**
 * 初始化树控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params fold--子树是否收缩，默认为展开
 */
function ECTree(baseClass, el, params) {
    var me = this, label = E.getFirstChild(el, 'label'), i = 0, items, o;
    ECControl.call(me, baseClass, el, params);
    me.onpaint = EC.cancel;

    me._aItem = [];

    /* 检查是否存在label标签，如果是需要自动初始化树的子结点 */
    if (label) {
        /* 将label抽取出来作为当前树结点的文本，其它结点先移除 */
        items = E.insert(el);
        E.appendTo(label, el);

        /* 生成子结点的容器标签 */
        E.insertAfter(items, el);
        me._eItems = E.addClass(items, params.type + '-items ' + baseClass + '-items');

        /* 初始化子控件 */
        for (items = E.getChildren(items); o = items[i]; i++) {
            me.append(EC.create('Tree', null, null, o, params));
        }
    }

    /* 改变默认的展开状态 */
    if (!el.offsetWidth || params.fold) {
        E.set(el, 'display', '');
        me.fold(true);
    }
}

extend(ECTree, ECControl);

copy(ECTree.prototype, {

    /**
     * 页面卸载时释放关联引用
     * @protected
     */
    $dispose: function () {
        this._eItems = null;
        ECControl.prototype.$dispose.call(this);
    },

    /**
     * 刷新控件的样式
     * @protected
     */
    $paint: function () {
        var me = this;
        me.setClass(me.getBaseClass() + (me._aItem.length ? me._eItems.offsetWidth ? '' : '-fold' : '-empty'));
    },

    /**
     * 树控件禁止改变大小
     * @protected
     */
    $setSize: EC.blank,

    /**
     * 鼠标点击改变子控件展开与隐藏的状态
     * @protected
     */
    $click: function () {
        var el = this._eItems;
        el && this.fold(!!el.offsetWidth);
    },

    /**
     * 显示控件
     * @public
     *
     * @return {Boolean} 控件显示状态是否改变，即调用函数前是否隐藏
     */
    show: function () {
        var me = this, display = me._sItemsDisplay;
        if (ECControl.prototype.show.call(this) && display != void(0)) {
            me._eItems.style.display = display;
            me._sItemsDisplay = void(0);
        }
    },

    /**
     * 隐藏控件
     * @public
     *
     * @return {Boolean} 控件显示状态是否改变，即调用函数前是否显示
     */
    hide: function () {
        var me = this, o = me._eItems;
        o = o && o.style;
        if (ECControl.prototype.hide.call(me) && o) {
            me._sItemsDisplay = o.display;
            o.display = 'none';
        }
    },

    /**
     * 设置控件的父控件，如果父控件也是树控件，将当前控件挂接在父控件上
     * @public
     *
     * @param {String|Element|ECControl|ECTree} parent 父控件
     */
    setParent: function (parent) {
        var me = this, oldParent = me.getParent(), body = parent.getBody();

        if (oldParent instanceof ECTree) {
            /* 先将树结点从上级树控件中移除 */
            items = oldParent._aItem;
            items.remove(me);
            oldParent.$paint();
        }

        if (parent instanceof ECTree) {
            var el = parent._eItems || (parent._eItems = E()), items = parent._aItem;

            /* 新的父结点是树控件，需要将数据移到子结点容器中 */
            parent.$setBody(el);
            ECControl.prototype.setParent.call(me, parent);
            parent.$setBody(body);
            items.push(me);
            parent.$paint();
        }
        else {
            ECControl.prototype.setParent.call(me, parent);
        }

        el = me._eItems;
        /* 如果包含子结点容器，需要将子结点容器显示在树控件之后 */
        el && (parent ? E.insertAfter(el, me.getOuter()) : E.appendTo(el));
    },

    /**
     * 获取子树控件
     * @public
     *
     * @return {Array} 子控件序列
     */
    getItems: function () {
        return this._aItem;
    },

    /**
     * 显示/隐藏子控件区域，将触发所有父树控件的onfold事件
     * @public
     *
     * @param {Boolean} status 如果为false表示显示，其它情况下均为隐藏
     */
    fold: function (status) {
        status = status !== false;
        var me = this;

        if (me._aItem.length) {
            E.set(me._eItems, 'display', status ? 'none' : '');
            me.setClass(me.getBaseClass() + (status ? '-fold' : ''));

            /* 检查父对象是否为树控件，如果是，需要触发相应的onchange事件 */
            for (; me instanceof ECTree; me = me.getParent()) {
                me.change();
            }
        }
    },

    /**
     * 增加一个子树控件
     * @public
     *
     * @param {String|ECTree} item 如果是字符串，将使用当前控件的样式作为子控件基本样式初始化，参数作为子控件的文本
     * @return {ECTree} 子控件对象
     */
    append: function (item) {
        isString(item) ? (item = EC.create('Tree', this, this.getBaseClass()).setHTML(item)) : item.setParent(this);
        return item;
    }
});

/*
ECTable - 表格控件，继承自ECControl。内部包含四个区域。上部是表头区域，下部是数据区域；左部是锁定的区域，右边是活动区
域。区域的基本样式为控件基本样式附加area后缀，表头单元格的基本样式为控件基本样式附加head后缀的样式，数据单元格的基本
样式为控件基本样式附加item后缀的样式。当前移入的行，整行的控件都添加一个在基本样式后附加rowover后缀的样式。例如表格控
件基本样式为table，区域基本样式为table-area，表头单元格基本样式为table-head，数据单元格基本样式为table-item。

表格控件直接HTML初始化的例子:
<div eui="type:table">
    <!-- 当前节点的列定义，如果有特殊格式，需要使用width样式 -->
    <div>
        <label>标题</label>
        ...
    </div>
    <!-- 这里放单元格序列 -->
    <div>
        <li>单元格一</li>
        ...
    </div>
    ...
</div>

属性
_bAutoSave        - 表格编辑时是否自动保存，即在双击其它行时保存当前行的信息，默认自动保存
_eCell            - 鼠标按下时的单元格
_oHead            - 表头区域
_oData            - 表格数据区域
_oEdit            - 表格正在编辑的行
_aCol             - 表头的列控件对象
_aRow             - 表格数据行对象
_aRow[n]._aCol    - 这一行的所有TD对象
_aRow[n]._eOver   - 鼠标按入的单元格

事件
onsave            - update时保存事件，触发此事件时还没有保存
onscroll          - 滚动事件处理，滚动条滚动时触发。
onrowclick        - 行鼠标点击事件
onrowdown         - 行鼠标按下事件
onrowover         - 行鼠标移入事件
onrowmove         - 行鼠标移动事件
onrowout          - 行鼠标移出事件
onrowup           - 行鼠标弹起事件
oncellclick       - 单元格鼠标点击事件
oncelldown        - 单元格鼠标按下事件
oncellover        - 单元格鼠标移入事件
oncellmove        - 单元格鼠标移动事件
oncellout         - 单元格鼠标移出事件
oncellup          - 单元格鼠标弹起事件
_aCol[n].onupdate - 列发生更新事件，默认的更新行为是将单元格的内容设置到编辑控件中
*/

/**
 * 初始化表格控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params 与ECPanel的相同
 */
function ECTable(baseClass, el, params) {
    var me = this, i = 0, htmls = [], func = ECTable.blockmove;
    params.wheelDelta = 1;
    ECPanel.call(me, baseClass, el, params);

    /* 初始化表头区域 */
    var o = me._oHead = ECTable.create.call(me, baseClass, params.type);
    o.setHTML(
        '<table style="border-collapse:collapse;border-spacing:0px;table-layout:fixed"><tbody><tr>'
        + '</tr></tbody></table>'
    );
    o.setSize(0, E.ncss(el, 'paddingTop'));

    el = me.getBody();

    /* 设置滚动条操作 */
    (o = me.getVScroll()) && (o.getBlock().ondragmove = func);
    (o = me.getHScroll()) && (o.getBlock().ondragmove = func);

    /* 初始化数据数组 */
    me._aCol = [];
    me._aRow = [];

    /* 取消paint调用 */
    me.$paint = EC.blank;

    /* 初始化表头 */
    for (var rows = E.getChildren(el), data = E.getChildren(E.appendTo(rows.splice(0, 1)[0])); o = data[i]; i++) {
        me.addCol(o);
    }

    /* 初始化数据 */
    for (i = 0; data = rows[i]; i++) {
        /* 取行Element中的每一列 */
        data = E.getChildren(data);

        /* 生成每一行的HTML内容 */
        ECTable.getHTML.call(me, data, htmls);

        /* 保存单行文本数据 */
        rows[i] = data;
    }
    el.innerHTML = htmls.join('');

    /* 初始化所有的行 */
    for (el = el.firstChild, i = 0; data = rows[i]; i++, el = el.nextSibling) {
        ECTable.$init.call(me, el, data);
    }

    /* 恢复并调用paint */
    delete me.$paint;

    me._bAutoSave = params.autoSave !== false;
}

extend(ECTable, ECPanel);

copy(ECTable, {

    /**
     * 设置层的显示位置
     * @protected
     */
    $scroll: function () {
        var me = this, style = me.getBody().style, vscroll = me.getVScroll(), hscroll = me.getHScroll(),
            top = vscroll ? vscroll.getValue() : 0, left = hscroll ? hscroll.getValue() : 0;

        style.left = me._oHead.getBody().style.left = -left + 'px';
        style.top = -top + 'px';
    },

    /**
     * 初始化一行的数据
     * @protected
     *
     * @param {Element} row 一行的DIV对象
     * @param {Array} data 列对应的数据数组
     */
    $init: function (row, data) {
        for (
            var me = this, cols = me._aCol, i = 0, rowCols = E.getChildren(row.firstChild.firstChild.firstChild), col;
            col = rowCols[i];
            i++
        ) {
            col._sClass = col.className.split(/\s+/)[0];
            cols[i]._bEllipsis && E.set(col, 'textOverflow', 'ellipsis');
        }

        row = EC.$fastCreate(ECControl, row, {type: me.getType() + '-row'});
        E.set(row.getBody(), 'position', 'relative');
        row._aCol = rowCols;
        row._aData = data;
        me._aRow.push(row);

        /* 设置行控件事件代理 */
        copy(row, ECTable.Row);
    },

    /**
     * 创建并初始化一个区域
     * @private
     *
     * @param {String} baseClass 区域的基本样式
     * @param {String} defaultClass 控件缺省的样式
     * @param {Boolean} status 是否不需要一个内部的层用于滚动
     * @return {ECControl} 区域控件对象
     */
    create: function (baseClass, defaultClass, status) {
        var o = EC.create('Control', this.getBase(), baseClass + '-area', null, {type: defaultClass + '-area'});
            el = o.getBase();

        E.set(el, {overflow: 'hidden', position: 'absolute', top: '0px', left: '0px'});
        status || o.$setBody(E.set(E.insert(el), 'white-space:nowrap;position:absolute'));
        return o;
    },

    /**
     * 获取当前事件发生的列元素
     * @private
     *
     * @return {ECControl} 当前事件发生的列元素
     */
    getColTarget: function () {
        for (
            var table = this.getParent(), cols = table._aCol, i = 0, width = 0, hscroll = table.getHScroll(), col,
                x = table.getMouse().x - E.ncss(table._oHead.getBase(), 'borderLeftWidth')
                    + (hscroll ? hscroll.getValue() : 0);
            col = cols[i];
            i++
        ) {
            if (!col.getOuter().style.display) {
                width += col.getWidth();
                if (x < width) {
                    return this._aCol[i];
                }
            }
        }
    },

    /**
     * 设置列内所有单元格的样式
     * @private
     *
     * @param {String} name 样式的名称
     * @param {String} value 样式的值
     */
    setColStyle: function (name, value) {
        var table = this.getParent().getParent(), col = table._aCol.indexOf(this), i = 0, rows = table._aRow, row;

        /* 设置每一数据行中列的样式 */
        for (; row = rows[i]; i++) {
            E.set(row._aCol[col], name, value);
            row.clearCache();
        }

        table.$paint();
    },

    /**
     * 获取一个数组表示的HTML填充代码，用于生成一行的HTML内容
     * @private
     *
     * @param {Array} data 一行的标签元素或者是一行的String型数据，但在处理结束后，data中的数据都将是字符串
     * @param {Array} htmls 如果是整体初始化，也即是data为一行的标签元素时，这里是表格全体的HTML内容数组
     * @param {Boolean} status 是否为左上角区域(不需要一个内部的层用于滚动)
     * @return {ECControl} 区域控件对象
     */
    getHTML: function (data, htmls) {
        var me = this, baseClass = me.getClass(), defaultClass = me.getType(), cols = me._aCol, i = 0, col,
            tableHTML = htmls || [];

        /* 设置行的样式 */
        tableHTML.push('<div class="' + baseClass + '-row');
        baseClass != defaultClass && tableHTML.push(' ' + defaultClass + '-row');
        tableHTML.push('"><table style="border-collapse:collapse;border-spacing:0px;table-layout:fixed"><tbody><tr>');

        for (; col = cols[i]; i++) {
            var o = data[i], baseClass = col.getClass();
            htmls && (o = data[i] = o.innerHTML);

            /* 设置行的样式 */
            baseClass = baseClass.substring(0, baseClass.lastIndexOf('-'));
            tableHTML.push('<td ondblclick="ECTable.dblclick(this)" class="' + baseClass + '-item');
            baseClass != defaultClass && tableHTML.push(' ' + defaultClass + '-item');
            tableHTML.push(
                '" style="width:' + E.get(col.getOuter(), 'width') + ';border:0px">' + (o || '&nbsp;') + '</td>'
            );
        }

        tableHTML.push('</tr></tbody></table></div>');

        /* 如果htmls没有值，就要返回一个拼接好的HTML字符串 */
        if (!htmls) {
            return tableHTML.join('');
        }
    },

    /**
     * 单元格移出处理，将单元格的-over状态样式清除
     * @private
     *
     * @param {Element} col 发生事件的列元素
     * @param {Event} event 事件对象
     */
    cellout: function (col, event) {
        var func = this.getParent().oncellout;
        func && func.call(col, event) === false || E.removeClass(col, col._sClass + '-over');
    },

    Row: {

        /**
         * 进入单行编辑状态
         * @private
         */
        update: function () {
            var me = this, table = me.getParent(), cols = table._aCol, i = 0, col, el;
            if (me != table._oEdit) {
                table.finish(table._bAutoSave);
                table._oEdit = me;
            }

            for (; col = cols[i]; i++) {
                var editor = col._oEditor, rowCol = me._aCol[i];
                if (editor) {
                    /* 设置控件的大小与初始值 */
                    col.onupdate && col.onupdate() || editor.setValue(E.getText(rowCol));
                    el = E.set(E.insert(rowCol), 'overflow:hidden;white-space:nowrap');
                    editor.setParent(rowCol);
                    E.set(editor.getOuter(), 'top', E.get(rowCol, 'paddingTop'));
                    editor.setSize(el.offsetWidth, el.offsetHeight);
                    /* 不改变单元格原始的大小 */
                    E.set(el, 'visibility:hidden');
                }
            }
        },

        /**
         * 行点击处理，将点击事件转发到表格控件中
         * @private
         */
        onclick: function () {
            var func = this.getParent().onrowclick;
            func && func.call(this);
        },

        /**
         * 行鼠标按下处理，将鼠标按下事件转发到表格控件中，同时计算需要产生鼠标按下事件的单元格
         * @private
         *
         * @param {Event} event 事件对象
         * @return {Boolean} 是否继续执行默认的处理，为false取消，其它值继续
         */
        onmousedown: function (event) {
            var me = this, table = me.getParent(), col = table._eCell = ECTable.getColTarget.call(me),
                cellfunc = table.oncelldown, rowfunc = table.onrowdown;

            cellfunc && cellfunc.call(col, event);

            return rowfunc && rowfunc.call(me, event);
        },

        /**
         * 行鼠标移入处理，将鼠标移入事件转发到表格控件中
         * @private
         *
         * @param {Event} event 事件对象
         * @return {Boolean} 是否继续执行默认的处理，为false取消，其它值继续
         */
        onmouseover: function (event) {
            var func = this.getParent().onrowover;
            return func && func.call(this, event);
        },

        /**
         * 行鼠标移动处理，将鼠标移动事件转发到表格控件中，同时计算需要产生鼠标移入/移出/移动事件的单元格
         * @private
         *
         * @param {Event} event 事件对象
         * @return {Boolean} 是否继续执行默认的处理，为false取消，其它值继续
         */
        onmousemove: function (event) {
            var me = this, table = me.getParent(), over = me._eOver, constructor = ECTable,
                col = me._eOver = constructor.getColTarget.call(me), rowfunc = table.onrowmove,
                cellout = table.oncellout, cellmove = table.oncellmove;

            if (over != col) {
                /* 产生了单元格移入/移出操作 */
                over && constructor.cellout.call(me, over, event);
                cellout && cellout.call(col, event) === false || E.addClass(col, col._sClass + '-over');
            }

            cellmove && cellmove.call(col, event);
            return rowfunc && rowfunc.call(me, event);
        },

        /**
         * 行鼠标移出处理，将鼠标移出事件转发到表格控件中，同时计算需要产生鼠标移出事件的单元格
         * @private
         *
         * @param {Event} event 事件对象
         * @return {Boolean} 是否继续执行默认的处理，为false取消，其它值继续
         */
        onmouseout: function (event) {
            var me = this, func = me.getParent().onrowout, over = me._eOver;

            /* 行移出需要移出单元格 */
            over && ECTable.cellout.call(me, over, event);
            me._eOver = null;

            return func && func.call(me, event);
        },

        /**
         * 行鼠标弹起处理，将鼠标弹起事件转发到表格控件中，同时计算需要产生鼠标弹起事件的单元格
         * @private
         *
         * @param {Event} event 事件对象
         * @return {Boolean} 是否继续执行默认的处理，为false取消，其它值继续
         */
        onmouseup: function (event) {
            var me = this, table = me.getParent(), col = ECTable.getColTarget.call(me),
                cellfunc = table.oncellup, rowfunc = table.onrowup;

            cellfunc && cellfunc.call(col, event);

            return rowfunc && rowfunc.call(me, event);
        },

        /**
         * 行鼠标按压结束处理，释放单元格的鼠标按下状态
         * @private
         */
        onpressend: function () {
            var me = this, table = me.getParent(), col = ECTable.getColTarget.call(me), func = table.oncellclick;

            table._eCell == col && func && func.call(col);
            table._eCell = null;
        }
    },

    Col: {

        /**
         * 设置列的表头大小
         * @public
         *
         * @param {Number} width 表格列的宽度
         */
        setSize: function (width) {
            var me = this, ellipsis = me._bEllipsis;
            ellipsis && me.setEllipsis();

            ECControl.prototype.setSize.call(me, width);
            ECTable.setColStyle.call(me, 'width', E.get(me.getOuter(), 'width'));

            ellipsis && me.setEllipsis(ellipsis);
        },

        /**
         * 设置列的编辑控件
         * @public
         *
         * @param {ECEdit} editor 当前表格列用于编辑的控件
         */
        setEditor: function (editor) {
            /* 处于编辑状态不能设置 */
            if (!this.getParent().getParent()._oEdit) {
                E.set(editor.getOuter(), 'position', 'absolute');
                this._oEditor = editor;
            }
        },

        /**
         * 设置省略状态，即当前列过长时，显示省略号
         * @public
         *
         * @param {Boolean} status 是否设置省略状态，如果为true表示需要设置，否则是取消
         */
        setEllipsis: function (status) {
            this._bEllipsis = status;
            status = status ? 'ellipsis' : '';

            E.set(this.getBody(), 'textOverflow', status);
            ECTable.setColStyle.call(this, 'textOverflow', status);
        },

        /**
         * 显示一列
         * @public
         *
         * @return {Boolean} 状态是否改变，即之前是否为隐藏状态
         */
        show: function () {
            if (ECControl.prototype.show.call(this)) {
                ECTable.setColStyle.call(this, 'display', '');
                return true;
            }
        },

        /**
         * 隐藏一列
         * @public
         *
         * @return {Boolean} 状态是否改变，即之前是否为显示状态
         */
        hide: function () {
            if (ECControl.prototype.hide.call(this)) {
                ECTable.setColStyle.call(this, 'display', 'none');
                return true;
            }
        }
    },

    /**
     * 单元格双击处理，如果当前列设置了编辑对象，将进入编辑模式
     * @public
     *
     * @param {Element} col 当前单元格的TD对象
     */
    dblclick: function (col) {
        var row = EC.findParent(col);
        row.getParent()._aCol[row._aCol.indexOf(col)]._oEditor && row.update();
    },

    /**
     * 滚动条滑动块拖动处理
     * @private
     */
    blockmove: function (event, x, y) {
        var scroll = this.getParent(), parent = scroll.getParent();

        parent.$scroll = ECTable.$scroll;
        scroll.$blockmove.call(this, event, x, y);
        delete parent.$scroll;
    }
});

copy(ECTable.prototype, {

    /**
     * 刷新表格，重新计算表格内部区域的位置与大小
     * @protected
     */
    $paint: function () {
        var me = this, head = me._oHead, vscroll = me.getVScroll(), hscroll = me.getHScroll(), col,
            bodyWidth = me.getBodyWidth(), bodyHeight = me.getBodyHeight(), dataWidth = 0, dataHeight = 0, i = 0,
            cols = me._aCol, colCount = cols.length, rows = me._aRow, rowCount = rows.length, el = head.getBody();

        /* 避免在进行大小设定时，重复刷新 */
        me.$paint = EC.blank;

        if (colCount) {
            /* 计算数据区域的宽度 */
            for (; i < colCount; i++) {
                col = cols[i];
                !col.getOuter().style.display && (dataWidth += col.getWidth());
            }

            E.set(me.getBody(), 'width', dataWidth + 'px');
            E.set(el, 'width', dataWidth + 'px');

            for (i = 0; i < rowCount; i++) {
                dataHeight += rows[i].getHeight();
                rows[i].getBody().firstChild.style.width = dataWidth + 'px';
            }

            /* 计算高度自动扩展 */
            me.setBodySize(
                hscroll ? 0 : dataWidth
                    + E.getWidthRevise(el.parentNode)
                    + (bodyHeight < dataHeight && vscroll ? vscroll.getWidth() : 0),
                vscroll ? 0 : dataHeight + (bodyWidth < dataWidth && hscroll ? hscroll.getHeight() : 0)
            );

            if (hscroll) {
                dataWidth = me.getBodyWidth();
                head.setSize(dataWidth);
            }
            else {
                head.setBodySize(dataWidth);
            }

            ECPanel.prototype.$paint.call(me);

            /* 如果显示滚动条，需要改变左下，右上两个区域的宽与高 */
            vscroll && vscroll.isShow() && hscroll && head.setSize(dataWidth - vscroll.getWidth());
        }
        else {
            me.hide();
        }

        delete me.$paint;
    },

    /**
     * 主数据区域的滚动处理
     * @protected
     */
    $scroll: function () {
        var me = this, vscroll = me.getVScroll(), hscroll = me.getHScroll(),
            top = me.getScrollTop(), left = me.getScrollLeft(), rows = me._aRow, rowCount = rows.length,
            cols = me._aCol, colCount = cols.length, value, el = me.getBody(),
            oldTop = -E.ncss(me.getBody(), 'top'), oldLeft = -E.ncss(me.getBody(), 'left');

        if (!me.onscroll || me.onscroll() !== false) {
            me.$scroll = ECTable.$scroll;
            me.$scroll(me);

            if (top > 0 && top < vscroll.getTotal()) {
                /* 每次向上下滚动一个完整的行 */
                if (top > oldTop) {
                    for (var i = 1, o = rows[0].getHeight(); i < rowCount; i++) {
                        /* 计算下移的新行位置 */
                        value = o + rows[i].getHeight();
                        if (top <= value) {
                            /* 如果是原来的行，强制往下移动一行 */
                            if (o <= oldTop) {
                                o = value;
                            }
                            vscroll.setValue(o);
                            delete me.$scroll;
                            return;
                        }
                        o = value;
                    }
                }
                else if (top < oldTop) {
                    for (i = rowCount - 1, o = el.offsetHeight; i >= 0; i--) {
                        /* 计算上移的新行位置 */
                        o -= rows[i].getHeight();
                        if (top >= o) {
                            vscroll.setValue(o);
                            delete me.$scroll;
                            return;
                        }
                    }
                }
            }

            if (left > 0 && left < hscroll.getTotal()) {
                /* 每次向左右滚动一个完整的列 */
                if (left > oldLeft) {
                    for (i = 1, o = cols[0].getWidth(); i < colCount; i++) {
                        /* 计算右移的新列位置 */
                        value = o + cols[i].getWidth();
                        if (left <= value) {
                            /* 如果是原来的行，强制往右移动一列 */
                            if (o <= oldLeft) {
                                o = value;
                            }
                            hscroll.setValue(o);
                            delete me.$scroll;
                            return;
                        }
                        o = value;
                    }
                }
                else if (left < oldLeft) {
                    for (i = colCount - 1, o = el.offsetWidth; i >= 0; i--) {
                        /* 计算左移的新列位置 */
                        o -= cols[i].getWidth();
                        if (left >= o) {
                            hscroll.setValue(o);
                            delete me.$scroll;
                            return;
                        }
                    }
                }
            }

            delete me.$scroll;
        }
    },

    /**
     * 获取一列的表头控件
     * @public
     *
     * @param {Number} col 列数，从0开始
     * @return {ECControl} 表头的列控件
     */
    getCol: function (col) {
        return this._aCol[col];
    },

    /**
     * 获取一个单元格的TD元素
     * @public
     *
     * @param {Number} row 表格的行数，从0开始
     * @param {Number} col 表格的列数，从0开始
     * @return {Element} 单元格TD
     */
    getCell: function (row, col) {
        row = this._aRow[row];
        return row && row._aCol[col];
    },

    /**
     * 获取表格当前是否处于自动保存状态
     * @public
     *
     * @return {Boolean} 是否处于自动保存状态
     */
    isAutoSave: function () {
        return this._bAutoSave;
    },

    /**
     * 设置表格是否自动保存
     * @public
     *
     * @param {Boolean} status 表格是否自动保存
     */
    setAutoSave: function (status) {
        this._bAutoSave = status;
    },

    /**
     * 新增一列
     * @public
     *
     * @param {String|Element} {width|el} 列宽，也可能是Element对象，表示要以这个对象为基础初始化
     * @param {String} text 列头部文字
     * @param {String} baseClass 列的基本样式
     */
    addCol: function (width, text, baseClass) {
        if (width.constructor != String) {
            o = width;
            width = o.style.width;
            text = o.innerHTML;
            baseClass = o.className.split(/\s+/)[0];
        }

        /* 重载的调用接口为function (el) */
        var me = this, rows = me._aRow, cols = me._aCol, head = me._oHead, i = 0,
            o = E(head.getBody().firstChild.firstChild.firstChild, 'td');

        o.innerHTML = text;

        /* 在表头增加一列 */
        o = EC.create(
            'Control',
            true,
            (baseClass || me.getClass()) + '-head',
            o,
            {type: me.getType() + '-head'}
        );
        E.set(o.getOuter(), {
            width: (width.endWith('%') ? Math.floor(me.getWidth() * width.number() / 100) : width.number()) + 'px',
            'float': 'left'
        });
        o.setSize(o.getWidth(), head.getBodyHeight());

        /* 设置表头列的缺省属性 */
        copy(o, ECTable.Col);
        o.setHTML(text);
        cols.push(o);

        /* 设置缺省的数据 */
        for (; o = rows[i]; i++) {
            E.set(E(o.getBody().firstChild.firstChild.firstChild, 'td'), 'width:' + width + 'px');
        }

        me.$paint();
    },

    /**
     * 新增一行
     * @public
     *
     * @param {Array} data 一行的原始数据序列
     */
    addRow: function (data) {
        var me = this, defaultClass = me.getType() + '-item', el = E.create(ECTable.getHTML.call(me, data));

        me.getBody().appendChild(el);

        /* 初始化行内一列的数据 */
        ECTable.$init.call(me, el, data);

        me.$paint();
    },

    /**
     * 新增若干行
     * @public
     *
     * @param {Array} data 若干行的原始数据序列(二维数组)，或是Element对象序列
     */
    addRows: function (data) {
        var me = this, size = data.length, i = 0;
        if (size) {
            me.$paint = EC.blank;

            for (; i < size; i++) {
                me.addRow(data[i]);
            }

            delete me.$paint;
            me.$paint();
        }
    },

    /**
     * 移除一列
     * @public
     *
     * @param {Number} col 列号，从0开始计数
     */
    removeCol: function (col) {
        var me = this, o = me._aCol.splice(col, 1)[0], i = 0, rows = me._aRow, row;

        if (o) {
            /* 移除列的表头 */
            E.appendTo(o.getOuter());

            /* 移除每一数据行中的列 */
            for (; row = rows[i]; i++) {
                E.appendTo(row._aCol.splice(col, 1)[0]);
                row.clearCache();
            }

            me.$paint();
        }
    },

    /**
     * 移除一行
     * @public
     *
     * @param {Number} row 行号，从0开始计数
     */
    removeRow: function (row) {
        var row = this._aRow.splice(row, 1)[0];

        if (row) {
            E.appendTo(row.getOuter());
            this.$paint();
        }
    },

    /**
     * 移除所有的行
     * @public
     */
    removeRows: function () {
        var rows = this._aRow, i = 0, rowCount = rows.length;
        this._aRow = [];

        for (; i < rowCount; i++) {
            E.appendTo(rows[i].getOuter());
        }

        this.$paint();
    },

    /**
     * 取消单行编辑状态，根据参数决定是否保存编辑的值
     * @private
     *
     * @param {Boolean} status 是否保存编辑的值，为true的时候保存
     */
    finish: function (status) {
        var me = this, cols = me._aCol, i = 0, edit = me._oEdit, col;
        if (edit) {
            /* 当前行处于编辑状态才能取消 */
            me._oEdit = null;

            for (; col = cols[i]; i++) {
                var o = col._oEditor, rowCol = edit._aCol[i];
                if (o) {
                    if (status && (!me.onsave || me.onsave !== false)) {
                        E.setText(rowCol, o.getValue());
                        col._bEllipsis && E.set(rowCol, 'textOverflow', 'ellipsis');
                    }
                    else {
                        E.setText(rowCol, E.getText(rowCol.firstChild));
                    }
                }
            }

            status && me.$paint();
        }
    }
});

/*
ECPopup - 弹出菜单控件，继承自ECControl。弹出式菜单不会改变当前已经激活的对象。弹出菜单选项的基本样式为控件基本样式附
加item后缀，如果菜单项包含子弹出菜单，为菜单项控件添加一个在基本样式后附加complex后缀的样式。例如弹出菜单控件基本样式
为popup，选项基本样式为ec-item

弹出菜单控件直接HTML初始化的例子:
<div eui="type:popup;name:test">
    <!-- 这里放选项内容 -->
    <li>菜单项</li>
    ...
    <!-- 包含子菜单项的菜单项 -->
    <li>
        <label>菜单项</label>
        <!-- 这里放子菜单项 -->
        <li>子菜单项</li>
        ...
    </li>
    ...
</div>

类属性
_oFocus     - 当前拥有焦点的弹出菜单控件

属性
_oSuperior  - 上一级被激活的弹出菜单控件
_oInferior  - 下一级被激活的弹出菜单控件

子菜单项属性
_oPopup     - 是否包含下级弹出菜单
*/

/**
 * 初始化弹出菜单控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params
 */
function ECPopup(baseClass, el, params) {
    var me = this, i = 0, items;
    ECControl.call(me, baseClass, el, params);
    el = me.getOuter();
    me.onpaint = EC.cancel;

    /* 初始化菜单项 */
    me.setParent(document.body);
    E.set(el, {display: 'none', position: 'absolute'});

    ECItems.init(me);

    /* 弹出菜单不允许改变父对象 */
    me.setParent = EC.blank;

    /* 初始化子弹出菜单 */
    for (items = me.getItems(); me = items[i]; i++) {
        me._oPopup && me.setClass(me.getBaseClass() + '-complex');
    }
}

extend(ECPopup, ECControl);

copy(ECPopup, {

    /**
     * 创建子弹出菜单并初始化
     * @private
     *
     * @param {ECControl} item 需要创建子弹出菜单的菜单项
     * @param {Element} el 关联的Element对象
     */
    create: function (item, el) {
        var popup = item._oPopup = EC.create('Popup', null, item.getParent().getClass(), el);
        popup.$setParent(item);
        return popup;
    },

    Item: {

        /**
         * 菜单项移入
         * @protected
         */
        $mouseover: function () {
            var me = this, inferior = me._oPopup, popup = me.getParent(), superior = popup.getSuperior(),
                o = popup._oInferior, pos = E.getPosition(me.getOuter()), range = Brower();

            /* 改变菜单项控件的显示状态 */
            ECItem.superclass.prototype.$mouseover.call(me);
            ECItem.prototype.$mouseover.call(me);

            if (o != inferior) {
                /* 隐藏之前显示的下级弹出菜单控件 */
                o && o.hide();

                if (inferior && inferior.getItems().length) {

                    /* 计算子菜单应该显示的位置 */
                    var x = pos.left, width = inferior.getWidth();
                    o = x + me.getWidth() - 4;
                    x -= width - 4;

                    /* 优先计算延用之前的弹出顺序的应该的位置 */
                    if (superior && superior.getX() > popup.getX() && x > range.left || o + width > range.right) {
                        o = x;
                    }

                    /* 显示新的子弹出菜单 */
                    inferior.setPosition(o, pos.top - 4);
                    inferior.show();
                }
            }
        },

        /**
         * 菜单项移出
         * @protected
         */
        $mouseout: function () {
            if (!this.getChildItems().length) {
                ECItem.superclass.prototype.$mouseout.call(this);
            }
        },

        /**
         * 获取子弹出菜单项
         * @public
         *
         * @return {Array} 子弹出菜单项，如果没有返回空数组
         */
        getChildItems: function () {
            var subPopup = this._oPopup;
            return subPopup ? subPopup.getItems() : [];
        },

        /**
         * 为菜单项创建子菜单项，会自动生成子弹出菜单
         * @public
         *
         * @param {String} innerHTML 菜单项文本
         */
        append: function (innerHTML) {
            return (this._oPopup || ECPopup.create(this)).append(innerHTML);
        }
    }
});

copy(ECPopup.prototype, ECItems.Extend);

copy(ECPopup.prototype, {

    /**
     * 弹出菜单状态改变后刷新，如果有父弹出菜单，需要改变对应的基本样式
     * @protected
     */
    $paint: function () {
        var me = this, item = me.getParent(), items = me.getItems(), length = items.length;
        item && item.setClass(item.getBaseClass() + (length ? '-complex' : ''));
        item = items[0];
        me.setBodySize(0, item ? item.getHeight() * length : 0);
    },

    /**
     * 失去激活，关闭所有的弹出菜单
     * @protected
     */
    $blur: function () {
        for (var popup = this, o; o = popup._oSuperior; popup = o);
        popup.hide();
    },

    /**
     * 菜单项初始化
     * @protected
     *
     * @param {ECControl} item 菜单项对象
     */
    $itemcreate: function (item) {
        var o = this, el = item.getBody();

        copy(item, ECPopup.Item);

        var label = E.getFirstChild(el, 'label');
        if (label) {
            /* 拥有子弹出菜单，需要初始化 */
            o = E.insert(el);
            E.appendTo(label, el);
            ECPopup.create(item, o);
            item.setHTML(label.innerHTML);
        }
    },

    /**
     * 获取上级弹出菜单控件
     * @public
     *
     * @return {ECPopup} 父弹出菜单控件
     */
    getSuperior: function () {
        var item = this.getParent();
        return item && item.getParent();
    },

    /**
     * 显示弹出菜单，如果菜单位于屏幕之外，自动转移到屏幕内，同时保存弹出菜单的层级关系
     * @public
     */
    show: function () {
        ECControl.prototype.show.call(this);
        var me = this, range = Brower(), constructor = ECPopup, focus = constructor._oFocus,
            el = me.getOuter(), pos = E.getPosition(el);

        /* 限制弹出菜单不能超出屏幕 */
        me.setPosition(
            Math.min(Math.max(pos.left, range.left), range.right - me.getWidth()),
            Math.min(Math.max(pos.top, range.top), range.bottom - me.getHeight())
        );

        if (focus) {
            /* 如果之前存在已弹出的菜单 */
            E.set(el, 'zIndex', E.ncss(focus.getOuter(), 'zIndex') + 1);
            me._oSuperior = focus;
            focus._oInferior = me;
        }
        else {
            /* 第一个弹出菜单，需要屏蔽鼠标点击 */
            E.set(el, 'zIndex', 65536);
            EC.forcibly(me);
        }

        constructor._oFocus = me;
    },

    /**
     * 隐藏弹出菜单，需要隐藏所有下级的弹出菜单
     * @public
     */
    hide: function () {
        var me = this, over = me.getOvered(), superior = me._oSuperior, inferior = me._oInferior;

        if (ECControl.prototype.hide.call(me)) {
            /* 已经移入的菜单选项需要移出 */
            over && ECItem.superclass.prototype.$mouseout.call(over);
            inferior && inferior.hide();

            ECPopup._oFocus = superior;
            superior ? (superior._oInferior = null) : EC.restore();
        }
    },

    /**
     * 强制拦截鼠标点击的处理，如果没有点击在弹出菜单上，关闭所有弹出菜单。如果点出在菜单项上，一定会触发onmousedown事
     * 件，只有菜单项没有子弹出菜单时才会触发onclick事件
     * @public
     */
    onforcibly: function (event) {
        var target = event.getTarget(), constructor = ECPopup;

        if (target && target.getParent() instanceof constructor) {
            /* 点中了一个菜单项 */
            target.mousedown();
            target.getChildItems().length || target.click();
        }

        constructor._oFocus.$blur();
        return false;
    }
});

/*
ECExchange - 交换框控件，继承自ECPanel。交换框中的选项支持多选，如果被选中，为选项控件添加一个在基本样式后附加select后
缀的样式。例如弹出菜单控件基本样式为exchange，选项基本样式为exchange-item，选项选中添加样式exchange-item-select

交换框控件直接HTML初始化的例子:
<div eui="type:popup;name:test">
    <!-- 这里放选项内容 -->
    <li>选项</li>
    ...
</div>

属性
_sName  - 交换框内所有input的名称
_o

选项属性
_eInput - 选项对应的input，form提交时使用
*/

/**
 * 初始化交换框控件
 * @public
 *
 * @param {String} baseClass 基本的样式名称
 * @param {Element} el 关联的Element对象
 * @param {Object} params 参数
 */
function ECExchange(baseClass, el, params) {
    var me = this;
    params.hscroll = false;
    me._sName = params.name || '';

    me.onappend = EC.blank;
    ECPanel.call(me, baseClass, el, params);
    delete me.onappend;

    ECItems.init(me);
}

extend(ECExchange, ECPanel);

copy(ECExchange, {

    Item: {

        /**
         * 选项鼠标按下事件处理
         * @protected
         *
         * @param {Event} event 鼠标按下事件
         */
        $mousedown: function (event) {
            EC.zoom('exchange', event);
        },

        /**
         * 设置当前控件的父控件，需要改变控件的名称
         * @public
         *
         * @param {ECControl|String|Element} parent 父控件/父Element的ID/父Element对象
         */
        setParent: function (parent) {
            var me = this;
            ECItem.prototype.setParent.call(me, parent);

            parent = me.getParent();
            parent && (me._eInput.name = parent._sName);
        },

        /**
         * 设置选中状态
         * @public
         *
         * @param {Boolean|void(0)} status 是否选中，如果不传值，表示反选
         */
        setSelected: function (status) {
            status === void(0) && (status = !this._bSelect);
            this.alterClass('selected', !status);
            this._bSelect = status;
        },

        /**
         * 当前鼠标移动到的记录号
         * @public
         */
        getIndex: function () {
            var me = this, math = Math, parent = me.getParent(), step = parent.getVScroll().getStep(),
                o = parent.getMouse().y, oldTop = me._nTop;

            me._nTop = o;

            if (o > parent.getHeight()) {
                if (o < oldTop) {
                    /* 鼠标回退不需要滚动 */
                    o = 0;
                }
                else {
                    /* 超出控件范围，3像素点对应一个选项 */
                    o = math.floor((o - math.max(0, oldTop)) / 3);
                    /* 如果不滚动，需要恢复原始的移动距离 */
                    o ? parent.getVScroll().move(o) : (me._nTop = oldTop);
                }
                o += me._nLastIndex;
            }
            else if (o < 0) {
                if (o > oldTop) {
                    /* 鼠标回退不需要滚动 */
                    o = 0;
                }
                else {
                    /* 超出控件范围，3像素点对应一个选项 */
                    o = math.ceil((o - math.min(0, oldTop)) / 3);
                    /* 如果不滚动，需要恢复原始的移动距离 */
                    o ? parent.getVScroll().move(o) : (me._nTop = oldTop);
                }
                o += me._nLastIndex;
            }
            else {
                o = math.floor((parent.getScrollTop() + o) / step);
            }

            return math.max(0, math.min(o, parent.getItems().length - 1));
        },

        /**
         * 选择框选中开始
         * @public
         */
        onselectstart: function () {
            this._nStartIndex = this._nLastIndex = this.getIndex();
            this.alterClass('selected');
        },

        /**
         * 选择框选中处理
         * @public
         *
         * @param {Event} event 鼠标按下事件
         */
        onselect: function () {
            var me = this, startIndex = me._nStartIndex, lastIndex = me._nLastIndex, index = me.getIndex(),
                items = me.getParent().getItems(), fromCancel = 0, toCancel = -1, fromSelect = 0, toSelect = -1;

            if (index > lastIndex) {
                if (index < startIndex) {
                    /* index与lastIndex都在负方向 */
                    fromCancel = lastIndex;
                    toCancel = index - 1;
                }
                else if (lastIndex < startIndex) {
                    /* index与lastIndex位于起始选项两边 */
                    fromCancel = lastIndex;
                    toCancel = startIndex - 1;
                    fromSelect = startIndex + 1;
                    toSelect = index;
                }
                else {
                    /* index与lastIndex都在正方向 */
                    fromSelect = lastIndex + 1;
                    toSelect = index;
                }
            }
            else if (index < lastIndex) {
                if (index > startIndex) {
                    /* index与lastIndex都在正方向 */
                    fromCancel = index + 1;
                    toCancel = lastIndex;
                }
                else if (lastIndex > startIndex) {
                    /* index与lastIndex位于起始选项两边 */
                    fromCancel = startIndex + 1;
                    toCancel = lastIndex;
                    fromSelect = index;
                    toSelect = startIndex - 1;
                }
                else {
                    /* index与lastIndex都在负方向 */
                    fromSelect = index;
                    toSelect = lastIndex - 1;
                }
            }

            me._nLastIndex = index;

            /* 恢复之前的选择状态 */
            for (; fromCancel <= toCancel; fromCancel++) {
                me = items[fromCancel];
                me.alterClass('selected', !me._bSelect);
            }

            /* 选择框内的全部假选中 */
            for (; fromSelect <= toSelect; fromSelect++) {
                items[fromSelect].alterClass('selected');
            }
        },

        /**
         * 选择框选中结束
         * @public
         */
        onselectend: function () {
            var me = this, startIndex = me._nStartIndex, index = me.getIndex(), items = me.getParent().getItems(),
                fromIndex = Math.min(startIndex, index), toIndex = Math.max(startIndex, index);

            if (startIndex == index) {
                /* 点击的当前条目，进行反选 */
                me.setSelected();
            }
            else {
                /* 否则选择框内的全部选中 */
                for (; fromIndex <= toIndex; fromIndex++) {
                    items[fromIndex].setSelected(true);
                }
            }
        }
    }
});

copy(ECExchange.prototype, ECItems.Extend);

copy(ECExchange.prototype, {

    /**
     * 交换框控件刷新
     * @public
     */
    $paint: function () {
        var me = this, item = me.getItems()[0], vscroll = me.getVScroll();

        item && vscroll.setStep(item.getHeight());
        me.setItemSize(me.getBodyWidth() - (me.getBody().offsetHeight > me.getBodyHeight() ? vscroll.getWidth() : 0));

        ECPanel.prototype.$paint.call(me);
    },

    /**
     * 选项初始化
     * @protected
     *
     * @param {ECControl} item 选项对象
     * @param {String} value 选项的值
     */
    $itemcreate: function (item, value) {
        copy(item, ECExchange.Item);

        var el = item.getBody(), input = item._eInput = E.set(E(el, 'input'), 'display:none');
        input.name = this._sName;
        input.value = value || E.getAttr(el, 'value') || '';

        item.setSelected(E.getAttr(el, 'selected'));
    },

    /**
     * 设置待选项提交用的名称
     * @public
     *
     * @param {String} name 提交用的名称
     */
    setName: function (name) {
        for (var i = 0, list = this._oItems, o; o = list[i]; i++) {
            /* 需要将下属所有的输入框名称全部改变 */
            o._eInput.name = name;
        }
        this._sName = name;
    },

    /**
     * 将选中的项移动到指定的交换框内
     * @public
     *
     * @param {ECExchange} exchange 交换框对象
     */
    moveTo: function (exchange) {
        if (exchange instanceof ECExchange) {

            var me = this, i = 0, list = me.getItems().slice(0), o;

            /* 避免每一次都刷新，完成后一次刷新 */
            me.$paint = exchange.$paint = EC.blank;

            for (; o = list[i]; i++) {
                if (o._bSelect) {
                    o.setParent(exchange);
                    o.setSelected();
                }
            }

            delete me.$paint;
            delete exchange.$paint;

            me.$paint();
            exchange.$paint();
        }
    }
});

