(function () {
    //构造方法
    var CCDataView = function (buff, options) {
        var that = this;
        if (buff instanceof Array) {
            this.buff = new Uint8Array(buff);
        } else if (buff instanceof ArrayBuffer) {
            this.buff = new Uint8Array(buff);
        }
        this.options = options;
        this.pos = 0;
        this.lim = buff.length || buff.byteLength;
    };
    CCDataView.prototype = {
        buffer: function () {
            return this.buff
        },
        position: function (p) {
            if (p != undefined && typeof p == 'number') {
                this.pos = p;
            } else {
                return this.pos;
            }
        },
        limit: function (l) {
            if (l != undefined && typeof l == 'number') {
                this.lim = l;
            } else {
                return this.lim;
            }
        },
        isArrayMatchAt: function (p, a) {
            var ismatch = true;
            for (var i = 0; i < a.length; i++) {
                if (a[i] != this.buff[p + i]) {
                    ismatch = false;
                    break;
                }
            }
            return ismatch;
        },
        isMatchsAt: function (p, as) {
            var ismatchs = false;
            for (var i = 0; i < as.length; i++) {
                if (this.isArrayMatchAt(p, as[i])) {
                    ismatchs = true;
                    break;
                }
            }
            return ismatchs;
        },
        matchsAt: function (p, as) {
            for (var i = 0; i < as.length; i++) {
                if (this.isArrayMatchAt(p, as[i])) {
                    return [true, as[i]];
                }
            }
            return [false];
        },
        arrayMatch: function (as, d) {
            var index = 0;
            if (!d) { //正向
                for (var i = this.pos; i < this.lim; i++) {
                    if (this.isMatchsAt(i, as)) {
                        return index;
                    } else {
                        index++;
                    }
                }
            } else { //逆向
                var mr;
                for (var i = this.lim - 1; i >= this.pos; i--) {
                    mr = this.matchsAt(i, as);
                    if (mr[0]) {
                        return index - mr[1].length;
                    } else {
                        index++;
                    }
                }
            }
            return index;
        },
        getIntArray: function (l, d) {
            var a;
            if (!d) { //正序
                if (l instanceof Array) {
                    l = this.arrayMatch(l);
                } else if (l == undefined || typeof l != 'number') {
                    l = this.lim - this.pos;
                }
                a = new Uint8Array(this.buff.buffer, this.pos, l);
                this.pos += l;
            } else { //逆序
                if (l instanceof Array) {
                    l = this.arrayMatch(l, d);
                } else if (l == undefined || typeof l != 'number') {
                    l = this.lim - this.pos;
                }
                a = new Uint8Array(this.buff.buffer, this.lim - l, l);
                this.lim -= l;
            }
            return a;
        }
    };
    window.CCDataView = CCDataView;
})();
(function () {
    var CCBitmap = function (bm, a, options) {
        this.buff = a;
        this.options = options;
        this.L = 0;
        this.bm = bm;
        this.im = {};
        for (var i = 0, p = 0; i < this.bm.length; i++) {
            this.im[bm[i].name] = bm[i];
            this.bm[i].L = this.bm[i].L || 1;
            this.L += this.bm[i].L;
            if (i == 0) {
                this.bm[i].p = 0;
            } else {
                p += this.bm[i - 1].L;
                this.bm[i].p = p;
            }
            this.bm[i].start = {
                "index": Math.floor(this.bm[i].p / 8),
                "offset": this.bm[i].p % 8
            };
            this.bm[i].end = {
                "index": Math.floor((this.bm[i].p + this.bm[i].L) / 8),
                "offset": (this.bm[i].p + this.bm[i].L) % 8
            };
            //计算占用buff长度
            if (this.bm[i].end.offset != 0) {
                this.bm[i].bsl = this.bm[i].end.index -
                    this.bm[i].start.index + 1;
            } else {
                this.bm[i].bsl = this.bm[i].end.index -
                    this.bm[i].start.index;
            }
        }
        //初始化
        if (!a) {
            //var l = Math.ceil(this.L / 8);
            this.buff = new Uint8Array(new ArrayBuffer(Math.ceil(this.L / 8)));
            //for (var i = 0; i < l; i++) {
            //  this.buff[i] = 0;
            //}
        }
    };
    CCBitmap.prototype = {
        buffer: function () {
            return this.buff
        },
        getByteMash: function (n, right) {
            if (right) {
                return (1 << n) - 1;
            } else {
                return ~((1 << (8 - n)) - 1) & 0xff;
            }
        },
        getByteUnMash: function (n, right) {
            if (right) {
                return ~((1 << n) - 1) & 0xff;
            } else {
                return (1 << (8 - n)) - 1;
            }
        },
        //info: function () {
        //    result = JSON.stringify(this, function (k, v) {
        //        if (k != 'buff') {
        //            return v;
        //        } else {
        //            return v.map(item = > item + "::" + item.toString(2))
        //        }
        //    }, 2);
        //},
        toObject: function () {
            var rs = {};
            for (var k in this.im) {
                if ('_' != k) {
                    rs[k] = this.getItemValue(k);
                }
            }
            return rs;
        },
        getItemValue: function (name) {
            var b = this.im[name];
            var rs = [];
            //缺少
            var lack = b.start.offset;
            //剩余
            var lave = 8 - lack;
            var endLave = 8 - b.end.offset;
            var bt = 0x00;
            if (b.bsl < 2) {
                if (b.end.offset != 0) {
                    bt = (this.buff[b.start.index] & this.getByteUnMash(endLave)) >> lack;
                } else {
                    bt = this.buff[b.start.index] >> lack;
                }
            } else {
                if (b.end.offset != 0) {
                    bt = this.buff[b.start.index] >> lack;
                }
                for (var i = 1; i < b.end.index - 1; i++) {
                    bt |= this.buff[i] << lave;
                    rs.push(bt & 0xff);
                    //保存剩余的bits
                    bt = this.buff[i] >> lave;
                }
                bt |= (this.buff[b.end.index] & this.getByteUnMash(endLave)) << lave;
            }
            rs.push(bt & 0xff);
            return rs;
        },
        setItemValue: function (name, value) {
            var b = this.im[name];
            //缺少
            var lack = b.start.offset;
            //剩余
            var lave = 8 - lack;
            var cursor = 0;
            var bt = value[cursor++];
            if (b.bsl < 2) {
                //先清除原来的值
                this.buff[b.start.index] &=
                    //this.getByteMash(lack,true)|
                    //this.getByteMash(lave)
                    ~(this.getByteMash(b.L, true) << lack) & 0xff;
                if (lack != 0) {
                    this.buff[b.start.index] |= (bt << lack) & 0xff;
                } else {
                    this.buff[b.start.index] |= bt & 0xff;
                }
            } else if (b.bsl < 3) {
                //先清除原来的值
                this.buff[b.start.index] &= this.getByteUnMash(lave);
                this.buff[b.start.index] |= (bt << lack) & 0xff;
                //先清除原来的值
                this.buff[b.end.index] &= this.getByteUnMash(b.end.offset, true);
                this.buff[b.end.index] |= (bt >> lave) & 0xff;
            } else {
                if (lave != 0) {
                    //先清除原来的值
                    this.buff[b.start.index] &= this.getByteUnMash(lave);
                    this.buff[b.start.index] |= (bt << lack) & 0xff;
                    //保存剩余bits
                    bt >>= lave;
                }
                for (var i = 1; i < b.end.index - 1; i++) {
                    this.buff[i] = bt & 0xff;

                    bt = value[cursor++];
                    this.buff[i] |= (bt << lack) & 0xff;

                    bt >>= lave;
                }
                this.buff[b.end.index] &= this.getByteUnMash(lave, true);
                this.buff[b.end.index] |= bt & 0xff;
            }
        }
    };
    window.CCBitmap = CCBitmap;
})();
(function () {
    var CCBitsyntax = function (des, options) {
        this.options = options || {};
        this.sps = []; //分隔符列表
        this.allItems = {};
        this.bitems = this.parser(des);
        this.fromLib = Object.assign({
            "I": function (a) {
                return intArrayToInteger(a);
            },
            "U8": function (a) {
                return intArrayToString(a);
            },
            "Hex": function (a) {
                return bytesToHex(a);
            },
            "HEX": function (a) {
                return bytesToHEX(a);
            }
        }, this.options.fromLib);
        this.toLib = Object.assign({
            "I": function (o, l) {
                return integerToIntArray(o, l);
            },
            "U8": function (s) {
                return stringToIntArray(s);
            },
            "Hex": function (s) {
                return hexToBytes(s);
            },
            "HEX": function (s) {
                return hexToBytes(s);
            }
        }, this.options.toLib);
    };
    CCBitsyntax.prototype = {
        parser: function (byntax) {
            var sb = '';
            var inArea = 0;
            var rl = [];
            for (var i = 0; i < byntax.length; i++) {
                switch (byntax[i]) {
                    case ',':
                        if (inArea != 0) {
                            sb += byntax[i];
                        } else {
                            rl.push(this.analyze(sb));
                            sb = '';
                        }
                        break;
                    case '"':
                    case '\'':
                        inArea = -inArea;
                        sb += byntax[i];
                        break;
                    case '(':
                    case '{':
                    case '[':
                    case '<':
                        inArea++;
                        sb += byntax[i];
                        break;
                    case ')':
                    case '}':
                    case ']':
                    case '>':
                        inArea--;
                        sb += byntax[i];
                        break;
                    case '\\':
                        sb += byntax[i++];
                        break;
                    default:
                        sb += byntax[i];
                        break;
                }
            }
            rl.push(this.analyze(sb));
            sb = '';
            return rl;
        },
        analyze: function (el) {
            var rm = {types: [], separatorDataIndex: this.sps.length, named: true};
            if ((el[0] == '\'' && el[el.length - 1] == '\'') ||
                (el[0] == '"' && el[el.length - 1] == '"')) {
                rm.type = 'SEPARATOR';
                //实际中应该使用stringToUint8Array
                rm.separatorData = stringToIntArray(el.substr(1, el.length - 2));
                rm.L = rm.separatorData.length;
                rm.name = Math.random().toString(16).substr(2);
                rm.named = false;
                this.sps.push(rm.separatorData);
                this.allItems[rm.name] = rm;
                return rm;
            } else if (el[0] == '<' && el[el.length - 1] == '>') {
                rm.type = 'MIXED';
                rm.name = Math.random().toString(16).substr(2);
                rm.named = false;
                this.parserMixed(el, rm);
                this.allItems[rm.name] = rm;
                return rm;
            } else if (el[0] == '(' && el[el.length - 1] == ')') {
                rm.type = 'RANGE';
                rm.name = Math.random().toString(16).substr(2);
                rm.named = false;
                rm.range = this.parserRange(el.substr(1, el.length - (1 << 1)));
                var range = [];
                rm.rg = [];
                for (var i = 0; i < rm.range.length; i++) {
                    if (rm.range[i].type != 'SEPARATOR') {
                        rm.rg.push(rm.range[i]);
                    } else {
                        range.push(rm.range[i].separatorData);
                    }
                }
                rm.range = range;
                this.allItems[rm.name] = rm;
                return rm;
            }
            var sb = '';
            var lastChar = 0;
            var inArea = 0;
            for (var i = 0; i < el.length; i++) {
                switch (el[i]) {
                    case ':':
                        if (!inArea) {
                            rm.name = sb;
                            sb = '';
                            lastChar = el[i];
                        } else {
                            sb += el[i];
                        }
                        break;
                    case '/':
                        if (!inArea) {
                            if (rm.name || rm.name == '') {
                                rm.L = stringToInt(sb);
                            } else {
                                rm.name = sb;
                            }
                            sb = '';
                            lastChar = el[i];
                        } else {
                            sb += el[i];
                        }
                        break;
                    case '-':
                        if (!inArea) {
                            if (sb[0] == '[' && sb[sb.length - 1] == ']') {
                                //
                            } else if (sb[0] == '{' && sb[sb.length - 1] == '}') {
                                //
                            } else if (sb[0] == '<' && sb[sb.length - 1] == '>') {
                                //
                            } else if (sb[0] == '(' && sb[sb.length - 1] == ')') {
                                //
                                rm.range = this.parserRange(sb.substr(1, sb.length - (1 << 1)));
                                for (var j = 0; j < rm.range.length; j++) {
                                    rm.range[j] = rm.range[j].separatorData;
                                }
                            } else {
                                rm.types.push(sb);
                            }
                            sb = '';
                        } else {
                            sb += el[i];
                        }
                        break;
                    case '(':
                    case '{':
                    case '[':
                    case '<':
                        inArea++;
                        sb += el[i];
                        break;
                    case ')':
                    case '}':
                    case ']':
                    case '>':
                        inArea--;
                        sb += el[i];
                        break;
                    case '\\':
                        sb += el[i++];
                        break;
                    default:
                        sb += el[i];
                        break;
                }
            }
            if (lastChar == '/') {
                if (sb[0] == '[' && sb[sb.length - 1] == ']') {
                    rm.type = 'LIST';
                    rm.list = this.parser(sb.substr(1, sb.length - 2));
                } else if (sb[0] == '{' && sb[sb.length - 1] == '}') {
                    rm.type = 'BITMAP';
                    rm.bitmap = this.parser(sb.substr(1, sb.length - 2));
                } else if (sb[0] == '<' && sb[sb.length - 1] == '>') {
                    rm.type = 'MIXED';
                    this.parserMixed(sb, rm);
                } else if (sb[0] == '(' && sb[sb.length - 1] == ')') {
                    rm.range = this.parserRange(sb.substr(1, sb.length - (1 << 1)));
                    for (var j = 0; j < rm.range.length; j++) {
                        rm.range[j] = rm.range[j].separatorData;
                    }
                } else {
                    rm.types.push(sb);
                }
            } else if (lastChar == ':') {
                rm.L = stringToInt(sb);
            } else {
                if (sb[0] == '(' && sb[sb.length - 1] == '}') {
                    rm.type = 'EXCHANGE';
                    rm.exchange = this.parserExchange(sb.substr(1, sb.length - 2));
                } else {
                    rm.name = sb;
                }
            }
            sb = '';
            if (!rm.name) {
                rm.name = Math.random().toString(16).substr(2);
                rm.named = false;
            } else if (rm.name == '_') {
                rm.type = 'PLACEHOLDER';
            }
            this.allItems[rm.name] = rm;
            return rm;
        },
        parserRange: function (el) {
            var sb = '';
            var inArea = 0;
            var rl = [];
            for (var i = 0; i < el.length; i++) {
                switch (el[i]) {
                    case '|':
                        if (inArea != 0) {
                            sb += el[i];
                        } else {
                            rl.push(this.analyze(sb));
                            sb = '';
                        }
                        break;
                    case '"':
                    case '\'':
                        inArea = -inArea;
                        sb += el[i];
                        break;
                    case '(':
                    case '{':
                    case '[':
                    case '<':
                        inArea++;
                        sb += el[i];
                        break;
                    case ')':
                    case '}':
                    case ']':
                    case '>':
                        inArea--;
                        sb += el[i];
                        break;
                    case '\\':
                        sb += el[i++];
                        break;
                    default:
                        sb += el[i];
                        break;
                }
            }
            rl.push(this.analyze(sb));
            sb = '';
            return rl;
        },
        parserMixed: function (el, rm) {
            if (el[1] == '?') {
                rm.dispensable = true;
                el = el.substr(2, el.length - (2 << 1));
            } else {
                rm.dispensable = false;
                el = el.substr(1, el.length - (1 << 1));
            }
            rm.mixed = this.parser(el);

        },
        parserExchange: function (el) {
            var rl = [];
            var index = el.indexOf('){');
            rl.push(el.substr(0, index));
            el = el.substr(index + 2, el.length - 2);

            var inArea = 0;
            var sb = '';
            var exs = [];
            for (var i = 0; i < el.length; i++) {
                switch (el[i]) {
                    case ':':
                        if (!inArea) {
                            exs.push(stringToInt(sb));
                            sb = '';
                        } else {
                            sb += el[i];
                        }
                        break;
                    case ',':
                        if (!inArea) {
                            exs.push(this.parser(sb.substr(1, sb.length - 2)));
                            sb = '';
                        } else {
                            sb += el[i];
                        }
                        break;
                    case '"':
                    case '\'':
                        inArea = -inArea;
                        sb += el[i];
                        break;
                    case '(':
                    case '{':
                    case '[':
                    case '<':
                        inArea++;
                        sb += el[i];
                        break;
                    case ')':
                    case '}':
                    case ']':
                    case '>':
                        inArea--;
                        sb += el[i];
                        break;
                    case '\\':
                        sb += el[i++];
                        break;
                    default:
                        sb += el[i];
                        break;
                }
            }
            exs.push(this.parser(sb.substr(1, sb.length - 2)));
            sb = '';
            rl.push(exs);
            return rl;
        },
        convertToObject: function (a, des) {
            var rs = {};
            var dv;
            if (a instanceof CCDataView) {
                dv = a;
            } else {
                dv = new CCDataView(a);
            }
            var bitems;
            if (des) {
                if (typeof des == 'string') {
                    bitems = this.parser(des);
                } else {
                    bitems = des;
                }
            } else {
                bitems = this.bitems;
            }
            var r = this.doConvertTo(bitems, dv, rs);
            if (r[0]) {
                for (var i = bitems.length - 1; i >= r[1]; i--) {
                    this.doConvert(bitems[i], dv, rs, true);
                }
            }
            return rs;
        },
        doConvertTo: function (bitems, dv, rs, revert) {
            if (revert) {
                for (var i = bitems.length - 1; i > -1; i--) {
                    var r = this.doConvert(bitems[i], dv, rs, revert);
                    if (r[0]) {
                        return [r[0], i];
                    }
                }
            } else {
                for (var i = 0; i < bitems.length; i++) {
                    var r = this.doConvert(bitems[i], dv, rs, revert);
                    if (r[0]) {
                        return [r[0], i];
                    }
                }
            }
            return [];
        },
        doConvert: function (bitem, dv, rs, revert) {
            if (bitem.L != undefined) {
                switch (bitem.type) {
                    case 'SEPARATOR':
                        if (revert) {
                            dv.limit(dv.limit() - bitem.L);
                        } else {
                            dv.position(dv.position() + bitem.L);
                        }
                        break;
                    case 'LIST':
                        var datas = [];
                        var itemIsArray = false;
                        if (bitem.list.length == 1) {
                            itemIsArray = !bitem.list[0].named;
                        }
                        switch (typeof bitem.L) {
                            case 'number':
                                if (itemIsArray) {
                                    for (var j = 0; j < bitem.L; j++) {
                                        datas.push(dv.getIntArray(bitem.list[0].L));
                                    }
                                } else {
                                    for (var j = 0; j < bitem.L; j++) {
                                        datas.push(this.convertToObject(dv, bitem.list));
                                    }
                                }
                                break;
                            case 'string':
                                if (itemIsArray) {
                                    for (var j = 0; j < rs[bitem.L]; j++) {
                                        datas.push(dv.getIntArray(bitem.list[0].L));
                                    }
                                } else {
                                    for (var j = 0; j < rs[bitem.L]; j++) {
                                        datas.push(this.convertToObject(dv, bitem.list));
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        rs[bitem.name] = datas;
                        break;
                    case 'BITMAP':
                        var bm;
                        var bms;
                        var bmv;
                        if (typeof bitem.L == 'number') {
                            bm = new CCBitmap(bitem.bitmap, dv.getIntArray(bitem.L, revert));
                        } else {
                            bm = new CCBitmap(bitem.bitmap, dv.getIntArray(rs[bitem.L], revert));
                        }
                        bms = bm.bm;
                        bmv = bm.toObject();
                        for (var j = 0; j < bms.length; j++) {
                            if ('_' != bms[j].name) {
                                bmv[bms[j].name] = convertByTypes(this.fromLib, bms[j].types, bmv[bms[j].name]);
                            }
                        }
                        if (bitem.named) {
                            rs[bitem.name] = bmv;
                        } else {
                            rs = Object.assign(rs, bmv);
                        }
                        break;
                    case 'PLACEHOLDER': //只适合读取用来跳过数据读取
                        if (typeof bitem.L == 'number') {
                            dv.position(dv.position() + bitem.L);
                        } else {
                            dv.position(dv.position() + rs[bitem.L]);
                        }
                        break;
                    default:
                        var v;
                        if (typeof bitem.L == 'number') {
                            v = dv.getIntArray(bitem.L, revert);
                        } else {
                            v = dv.getIntArray(rs[bitem.L], revert);
                        }
                        if (bitem.types && bitem.types.length) {
                            for (var j = 0, tp; tp = bitem.types[j]; j++) {
                                v = this.fromLib[tp](v);
                            }
                        }
                        rs[bitem.name] = v;
                        break;
                }
            } else {
                switch (bitem.type) {
                    case 'EXCHANGE':
                        var exc = bitem.exchange;
                        var r = [];
                        if (typeof exc[0] == 'string') {
                            var refCondition = rs[exc[0]];
                            var convertFlag = -1;
                            for (var j = 0; j < exc[1].length; j++) {
                                if (refCondition === exc[1][j]) {
                                    //rs=Object.assign(rs,convertToObject(dv,exc[1][++j]));
                                    r = this.doConvertTo(exc[1][++j], dv, rs, revert);
                                    convertFlag--;
                                    break;
                                } else if (exc[1][j] === '') {
                                    convertFlag = ++j;
                                }
                            }
                            if (convertFlag > -1) {
                                //rs=Object.assign(rs,convertToObject(dv,exc[1][convertFlag]));
                                r = this.doConvertTo(exc[1][convertFlag], dv, rs, revert);
                            }
                        }
                        return r;
                        break;
                    case 'MIXED':
                        switch (bitem.mixed[0].type) {
                            case 'SEPARATOR':
                                var matchsResult = dv.matchsAt(dv.position(), [bitem.mixed[0].separatorData]);
                                if (matchsResult[0]) {
                                    if (bitem.named) {
                                        var oo = {};
                                        var rr = this.doConvertTo(bitem.mixed, dv, oo, revert);
                                        rs[bitem.name] = oo;
                                        return rr;
                                    } else {
                                        return this.doConvertTo(bitem.mixed, dv, rs, revert);
                                    }
                                }
                                break;
                                break;
                            default:
                                if (bitem.mixed[0].range) {
                                    var matchsResult = dv.matchsAt(dv.position(), bitem.mixed[0].range);
                                    if (matchsResult[0]) {
                                        if (bitem.named) {
                                            var oo = {};
                                            var rr = this.doConvertTo(bitem.mixed, dv, oo, revert);
                                            rs[bitem.name] = oo;
                                            return rr;
                                        } else {
                                            return this.doConvertTo(bitem.mixed, dv, rs, revert);
                                        }
                                    }
                                } else if (!bitem.dispensable) {
                                    if (bitem.named) {
                                        var oo = {};
                                        var rr = this.doConvertTo(bitem.mixed, dv, oo, revert);
                                        rs[bitem.name] = oo;
                                        return rr;
                                    } else {
                                        return this.doConvertTo(bitem.mixed, dv, rs, revert);
                                    }
                                }
                                ;
                                break;
                        }
                        break;
                    case 'RANGE':
                        var matchsResult = dv.matchsAt(dv.position(), bitem.range);
                        if (!matchsResult[0] && bitem.rg && bitem.rg.length) {
                            //rs=Object.assign(rs,convertToObject(dv,bitem.rg));
                            return this.doConvertTo(bitem.rg, dv, rs, revert);
                        } else {
                            dv.position(dv.position() + matchsResult[1].length);
                        }
                        break;
                    default:
                        if (bitem.range) {
                            var matchsResult = dv.matchsAt(dv.position(), bitem.range);
                            if (matchsResult[0]) {
                                if (revert) {
                                    dv.limit(dv.limit() - matchsResult[1].length);
                                } else {
                                    dv.position(dv.position() + matchsResult[1].length);
                                }
                                if (bitem.types && bitem.types.length) {
                                    var v = matchsResult[1];
                                    for (var j = 0, tp; tp = bitem.types[j]; j++) {
                                        v = this.fromLib[tp](v);
                                    }
                                    rs[bitem.name] = v;
                                } else {
                                    rs[bitem.name] = matchsResult[1];
                                }
                            }
                        } else {
                            var separatorDatas = this.sps.slice(bitem.separatorDataIndex);
                            if (separatorDatas.length) {
                                if (bitem.type != 'PLACEHOLDER') {
                                    var v = dv.getIntArray(separatorDatas, revert);
                                    if (bitem.types && bitem.types.length) {
                                        for (var j = 0, tp; tp = bitem.types[j]; j++) {
                                            v = this.fromLib[tp](v);
                                        }
                                    }
                                    rs[bitem.name] = v;
                                } else {
                                    if (revert) {
                                        dv.limit(dv.limit() - dv.arrayMatch(separatorDatas, revert));
                                    } else {
                                        dv.position(dv.position() + dv.arrayMatch(separatorDatas, revert));
                                    }
                                }
                            } else {
                                if (revert) {
                                    if (bitem.type != 'PLACEHOLDER') {
                                        var v = dv.getIntArray(null, revert);
                                        if (bitem.types && bitem.types.length) {
                                            for (var j = 0, tp; tp = bitem.types[j]; j++) {
                                                v = this.fromLib[tp](v);
                                            }
                                        }
                                        rs[bitem.name] = v;
                                    } else {
                                        dv.limit(dv.position());
                                    }
                                }
                                return [true];
                            }
                        }
                        break;
                }
            }
            return [];
        },
        convertToIntArray: function (o, des) {
            var rm = {};
            var bitems;
            if (des) {
                if (typeof des == 'string') {
                    bitems = this.parser(des);
                } else {
                    bitems = des;
                }
            } else {
                bitems = this.bitems;
            }
            for (var i = 0; i < bitems.length; i++) {
                var bitem = bitems[i];
                switch (bitem.type) {
                    case 'SEPARATOR':
                        rm[bitem.name] = bitem.separatorData;
                        break;
                    case 'LIST':
                        var datas = o[bitem.name];
                        var da = [];
                        var itemIsArray = false;
                        if (bitem.list.length == 1) {
                            itemIsArray = !bitem.list[0].named;
                        }
                        if (typeof bitem.L == 'number') {
                            if (itemIsArray) {
                                for (var j = 0; j < bitem.L; j++) {
                                    da = da.concat(datas[j]);
                                }
                            } else {
                                for (var j = 0; j < bitem.L; j++) {
                                    da = da.concat(this.convertToIntArray(datas[j], bitem.list));
                                }
                            }
                        } else {
                            if (itemIsArray) {
                                for (var j = 0; j < datas.length; j++) {
                                    da = da.concat(datas[j]);
                                }
                            } else {
                                for (var j = 0; j < datas.length; j++) {
                                    da = da.concat(this.convertToIntArray(datas[j], bitem.list));
                                }
                            }
                            if (bitem.L) {
                                var refBitem = this.allItems[bitem.L];
                                var tv = datas.length;
                                for (var j = refBitem.types.length - 1, tp; tp = refBitem.types[j]; j--) {
                                    tv = this.toLib[tp](tv, refBitem.L);
                                }
                                rm[bitem.L] = tv;
                                //别的地方可能需要使用
                                o[bitem.L] = datas.length;
                            }
                        }
                        rm[bitem.name] = da;
                        break;
                    case 'EXCHANGE':
                        var exc = bitem.exchange;
                        if (typeof exc[0] == 'string') {
                            var refCondition = o[exc[0]];
                            var convertFlag = -1;
                            for (var j = 0; j < exc[1].length; j++) {
                                if (refCondition === exc[1][j]) {
                                    rm[bitem.name] = this.convertToIntArray(o, exc[1][++j]);
                                    convertFlag--;
                                    break;
                                } else if (exc[1][j] === '') {
                                    convertFlag = ++j;
                                }
                            }
                            if (convertFlag > -1) {
                                rm[bitem.name] = this.convertToIntArray(o, exc[1][convertFlag]);
                            }
                        }
                        break;
                    case 'BITMAP':
                        var valueMap = o[bitem.name];
                        var bitmap = new CCBitmap(bitem.bitmap);
                        var bms = bitmap.bm;
                        for (var j = 0; j < bms.length; j++) {
                            bitmap.setItemValue(bms[j].name, this.toLib[bms[j].types[0]](valueMap[bms[j].name]));
                        }
                        if (typeof bitem.L == 'number') {
                            rm[bitem.name] = bitmap.buffer();
                        } else {
                            var bs = bitmap.buffer();
                            rm[bitem.name] = bs;
                            if (bitem.L) {
                                var refBitem = this.allItems[bitem.L];
                                var tv = bs.length;
                                for (var j = refBitem.types.length - 1, tp; tp = refBitem.types[j]; j--) {
                                    tv = this.toLib[tp](tv, refBitem.L);
                                }
                                rm[bitem.L] = tv;
                                o[bitem.L] = bs.length;
                            }
                        }
                        break;
                    case 'MIXED':
                        if (bitem.named) {
                            if (o[bitem.name]) {
                                rm[bitem.name] = this.convertToIntArray(o[bitem.name], bitem.mixed);
                            }
                        } else {
                            for (var mi = 0; mi < bitem.mixed.length; mi++) {
                                if (o[bitem.mixed[mi].name]) {
                                    rm[bitem.name] = this.convertToIntArray(o, bitem.mixed);
                                    break;
                                }
                            }
                        }
                        if (!bitem.dispensable) {
                            //TODO
                        }
                        break;
                    case "RANGE":
                        if (bitem.rg[0] && o[bitem.rg[0].name]) {
                            var tv = o[bitem.rg[0].name];
                            for (var j = bitem.rg[0].types.length - 1, tp; tp = bitem.rg[0].types[j]; j--) {
                                tv = this.toLib[tp](tv);
                            }
                            rm[bitem.name] = tv;
                        } else {
                            rm[bitem.name] = bitem.range[0];
                        }
                        break;
                    default:
                        if (typeof bitem.L == 'number') {
                            var tv = o[bitem.name];
                            for (var j = bitem.types.length - 1, tp; tp = bitem.types[j]; j--) {
                                tv = this.toLib[tp](tv, bitem.L);
                            }
                            rm[bitem.name] = tv;
                        } else {
                            var tv = o[bitem.name];
                            for (var j = bitem.types.length - 1, tp; tp = bitem.types[j]; j--) {
                                tv = this.toLib[tp](tv);
                            }
                            rm[bitem.name] = tv;
                            if (bitem.L) {
                                var refBitem = this.allItems[bitem.L];
                                tv = tv.length;
                                for (var j = refBitem.types.length - 1, tp; tp = refBitem.types[j]; j--) {
                                    tv = this.toLib[tp](tv, refBitem.L);
                                }
                                rm[bitem.L] = tv;
                                o[bitem.L] = tv.length;
                            }
                        }
                        break;
                }
            }
            var rs = [];
            for (var p in rm) {
                rs = rs.concat(rm[p]);
            }
            return rs;
        }
    };
    window.CCBitsyntax = CCBitsyntax;
    // 将字符串格式化为UTF8编码的字节
    function stringToIntArray(str) {
        var out = [], p = 0;
        for (var i = 0; i < str.length; i++) {
            var c = str.charCodeAt(i);
            if (c < 128) {
                out[p++] = c;
            } else if (c < 2048) {
                out[p++] = (c >> 6) | 192;
                out[p++] = (c & 63) | 128;
            } else if (
                ((c & 0xFC00) == 0xD800) && (i + 1) < str.length &&
                ((str.charCodeAt(i + 1) & 0xFC00) == 0xDC00)) {
                // Surrogate Pair
                c = 0x10000 + ((c & 0x03FF) << 10) + (str.charCodeAt(++i) & 0x03FF);
                out[p++] = (c >> 18) | 240;
                out[p++] = ((c >> 12) & 63) | 128;
                out[p++] = ((c >> 6) & 63) | 128;
                out[p++] = (c & 63) | 128;
            } else {
                out[p++] = (c >> 12) | 224;
                out[p++] = ((c >> 6) & 63) | 128;
                out[p++] = (c & 63) | 128;
            }
        }
        return out;
    }

    function stringToUint8Array(str) {
        var rs = [];
        var sb;
        var code = encodeURI(str);
        for (var i = 0; i < code.length; i++) {
            switch (code[i]) {
                case '%':
                    sb = '';
                    sb += code[++i];
                    sb += code[++i];
                    rs.push(parseInt(sb, 16));
                    break;
                default:
                    rs.push(code[i].charCodeAt(0));
                    break;
            }
        }
        return new Uint8Array(rs);
    }

    function intArrayToString(bytes) {
        var out = [], pos = 0, c = 0;
        while (pos < bytes.length) {
            var c1 = bytes[pos++];
            if (c1 < 128) {
                out[c++] = String.fromCharCode(c1);
            } else if (c1 > 191 && c1 < 224) {
                var c2 = bytes[pos++];
                out[c++] = String.fromCharCode((c1 & 31) << 6 | c2 & 63);
            } else if (c1 > 239 && c1 < 365) {
                // Surrogate Pair
                var c2 = bytes[pos++];
                var c3 = bytes[pos++];
                var c4 = bytes[pos++];
                var u = ((c1 & 7) << 18 | (c2 & 63) << 12 | (c3 & 63) << 6 | c4 & 63) -
                    0x10000;
                out[c++] = String.fromCharCode(0xD800 + (u >> 10));
                out[c++] = String.fromCharCode(0xDC00 + (u & 1023));
            } else {
                var c2 = bytes[pos++];
                var c3 = bytes[pos++];
                out[c++] =
                    String.fromCharCode((c1 & 15) << 12 | (c2 & 63) << 6 | c3 & 63);
            }
        }
        return out.join('');
    }

    var csm=[];
    var CSM=[];
    (function () {
        var cs="0123456789abcdef";
        var CS="0123456789ABCDEF";
        var n=0;
        for(var i=0;i<16;i++){
            for(var j=0;j<16;j++,n++){
                csm[n]=cs[i]+cs[j];
                CSM[n]=CS[i]+CS[j];
            }
        }
    })();
    function bytesToHex(a) {
        var s='';
        for(var i=0;i<a.length;i++){
            s+=csm[a[i]&0xff];
        }
        return s;
    }
    function bytesToHEX(a) {
        var s='';
        for(var i=0;i<a.length;i++){
            s+=CSM[a[i]&0xff];
        }
        return s;
    }
    function hexToBytes(hex){
        let bytes=[];
        for(let i=0,j=0;i<hex.length();i++) {
            switch (hex.charAt(i)) {
                case '1':bytes[j]|=0x10>>((i&1)<<2);break;
                case '2':bytes[j]|=0x20>>((i&1)<<2);break;
                case '3':bytes[j]|=0x30>>((i&1)<<2);break;
                case '4':bytes[j]|=0x40>>((i&1)<<2);break;
                case '5':bytes[j]|=0x50>>((i&1)<<2);break;
                case '6':bytes[j]|=0x60>>((i&1)<<2);break;
                case '7':bytes[j]|=0x70>>((i&1)<<2);break;
                case '8':bytes[j]|=0x80>>((i&1)<<2);break;
                case '9':bytes[j]|=0x90>>((i&1)<<2);break;
                case 'a':case 'A':bytes[j]|=0xA0>>((i&1)<<2);break;
                case 'b':case 'B':bytes[j]|=0xB0>>((i&1)<<2);break;
                case 'c':case 'C':bytes[j]|=0xC0>>((i&1)<<2);break;
                case 'd':case 'D':bytes[j]|=0xD0>>((i&1)<<2);break;
                case 'e':case 'E':bytes[j]|=0xE0>>((i&1)<<2);break;
                case 'f':case 'F':bytes[j]|=0xF0>>((i&1)<<2);break;
                default:break;
            }
            j+=i&1;
        }
        return bytes;
    }
    //Number转数组
    function integerToIntArray(n, l, d) {
        n = n || 0;
        var s = n.toString(16);
        if (s.length % 2 != 0) {
            s = '0' + s;
        }
        var rs = [];
        if (l > (s.length >> 1)) {
            if (d) { //BigEnding
                for (var i = (s.length >> 1) - 1; i > -1; i--) {
                    rs.push(parseInt(s[i << 1] + s[(i << 1) + 1], 16));
                }
                for (var i = (s.length >> 1); i < l; i++) {
                    rs.push(0);
                }
            } else {
                for (var i = 0; i < (l - (s.length >> 1)); i++) {
                    rs.push(0);
                }
                for (var i = 0; i < (s.length >> 1); i++) {
                    rs.push(parseInt(s[i << 1] + s[(i << 1) + 1], 16));
                }
            }
        } else if (l > 0) {
            if (d) { //BigEnd
                for (var i = l - 1; i > -1; i--) {
                    rs.push(parseInt(s[i << 1] + s[(i << 1) + 1], 16));
                }
            } else {
                for (var i = 0; i < l; i++) {
                    rs.push(parseInt(s[i << 1] + s[(i << 1) + 1], 16));
                }
            }
        } else {
            if (d) {
                for (var i = s.length - 1; i > -1; i--) {
                    rs.push(parseInt(s[i - 1] + s[i--], 16));
                }
            } else {
                for (var i = 0; i < s.length; i++) {
                    rs.push(parseInt(s[i++] + s[i], 16));
                }
            }
        }
        return rs;
    }

    function intArrayToInteger(a, d) {
        if (d) {
            a = a.reverse();
        }
        var sn = '';
        for (var i = 0; i < a.length; i++) {
            var s = a[i].toString(16);
            if (s.length < 2) {
                sn += '0' + s;
            } else {
                sn += s;
            }
        }
        return parseInt(sn, 16);
    }

    function stringToInt(str) {
        var rs = parseInt(str, 10);
        if (Number.isInteger(rs)) {
            return rs;
        } else {
            return str;
        }
    }

    function convertByTypes(lib, types, value, d) {
        if (!d) {
            for (var i = 0, tp; tp = types[i]; i++) {
                value = lib[tp](value);
            }
        } else {
            for (var i = types.length - 1, tp; tp = types[i]; i--) {
                value = lib[tp](value);
            }
        }
        return value;
    }
})();
